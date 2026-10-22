package org.zotero.android.covers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext
import kotlin.math.min
import kotlin.math.roundToInt

/** Shared by visible rows. Rendering is serialized and both caches have fixed size limits. */
internal object ItemCoverLoader {
    private const val WIDTH = 256
    private const val HEIGHT = 384
    private const val DISK_LIMIT = 32L * 1024 * 1024
    private val mutex = Mutex()
    private val memory = object : LruCache<String, Bitmap>(8 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = (value.allocationByteCount / 1024).coerceAtLeast(1)
    }
    private val failures = LruCache<String, Boolean>(128)

    suspend fun load(source: ItemCoverSource, cacheDirectory: File): Bitmap? = withContext(Dispatchers.IO) {
        mutex.withLock {
            coroutineContext.ensureActive()
            if (!source.isCurrent()) return@withLock null
            val key = source.cacheKey
            memory.get(key)?.let { return@withLock it }
            if (failures.get(key) == true) return@withLock null
            val directory = File(cacheDirectory, "Zotero/item-covers-v1")
            val digest = MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
                .joinToString("") { "%02x".format(it) }
            val cachedFile = File(directory, "$digest.png")
            try {
                val bitmap = (if (cachedFile.isFile) decodeImage { options ->
                    BitmapFactory.decodeFile(cachedFile.path, options)
                } else null) ?: render(source)
                coroutineContext.ensureActive()
                if (!source.isCurrent()) return@withLock null
                if (bitmap == null) {
                    failures.put(key, true)
                    return@withLock null
                }
                memory.put(key, bitmap)
                // A full/unwritable cache must not prevent displaying a generated cover.
                runCatching {
                    directory.mkdirs()
                    if (!cachedFile.isFile) {
                        val temporary = File(directory, "$digest.tmp")
                        try {
                            temporary.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                            temporary.renameTo(cachedFile)
                        } finally {
                            temporary.delete()
                        }
                    }
                    cachedFile.setLastModified(System.currentTimeMillis())
                    trimDisk(directory)
                }
                bitmap
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                failures.put(key, true)
                null
            } catch (_: OutOfMemoryError) {
                failures.put(key, true)
                null
            }
        }
    }

    private fun render(source: ItemCoverSource): Bitmap? {
        val file = File(source.path)
        return when (source.kind) {
            ItemCoverSource.Kind.PDF -> ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    if (renderer.pageCount == 0) return null
                    renderer.openPage(0).use { page ->
                        val (width, height) = fitSize(page.width, page.height)
                        createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                            bitmap.eraseColor(Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        }
                    }
                }
            }
            ItemCoverSource.Kind.EPUB -> EpubCoverReader.read(file)?.let { bytes ->
                decodeImage { options -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) }
            }
            ItemCoverSource.Kind.IMAGE -> decodeImage { options -> BitmapFactory.decodeFile(file.path, options) }
        }
    }

    private fun decodeImage(decode: (BitmapFactory.Options) -> Bitmap?): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true; inSampleSize = 1 }
        decode(options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null
        while (options.outWidth / options.inSampleSize > WIDTH * 2 || options.outHeight / options.inSampleSize > HEIGHT * 2) {
            options.inSampleSize *= 2
        }
        options.inJustDecodeBounds = false
        val bitmap = decode(options) ?: return null
        val (width, height) = fitSize(bitmap.width, bitmap.height)
        return bitmap.scale(width, height, true).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }

    private fun fitSize(width: Int, height: Int): Pair<Int, Int> {
        val scale = min(1f, min(WIDTH.toFloat() / width, HEIGHT.toFloat() / height))
        return (width * scale).roundToInt().coerceAtLeast(1) to (height * scale).roundToInt().coerceAtLeast(1)
    }

    private fun trimDisk(directory: File) {
        val files = directory.listFiles()?.filter { it.extension == "png" } ?: return
        var total = files.sumOf { it.length() }
        for (file in files.sortedBy { it.lastModified() }) {
            if (total <= DISK_LIMIT) break
            val size = file.length()
            if (file.delete()) total -= size
        }
    }
}
