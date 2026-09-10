package org.zotero.android.uicomponents.math

import android.graphics.Color as AndroidColor
import android.util.LruCache
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream

private val renderedMath = object : LruCache<String, String>(2 * 1024 * 1024) {
    override fun sizeOf(key: String, value: String) = (key.length + value.length) * 2
}
private const val ORIGIN = "https://appassets.androidplatform.net/math/"

@Composable
internal fun MathText(text: String, color: Color, style: TextStyle, modifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE) {
    val segments = remember(text) { MathContent.segments(text) }
    if (segments.none { it.tex != null } || text.length > 50000) {
        Text(text, modifier, color = color, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
        return
    }
    val density = LocalDensity.current
    val fontSize = style.fontSize.value.takeIf { it.isFinite() } ?: 16f
    val fontPx = fontSize * density.fontScale
    val linePx = fontPx * 1.5f
    val preview = maxLines != Int.MAX_VALUE
    val hex = "#%06x".format(color.toArgb() and 0xffffff)
    BoxWithConstraints(modifier.fillMaxWidth().semantics { contentDescription = text }) {
        val width = maxWidth.value
        val cacheKey = "$width/$fontPx/$hex/$maxLines/$text"
        var height by remember(cacheKey) { mutableFloatStateOf(linePx) }
        val cached = remember(cacheKey) { renderedMath.get(cacheKey) }
        val html = remember(cacheKey) { mathPage(segments, cached, fontPx, hex, if (preview) maxLines else 0) }
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(height.dp),
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
                    settings.javaScriptEnabled = true
                    settings.allowFileAccess = false
                    settings.allowContentAccess = false
                    settings.blockNetworkLoads = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    settings.defaultTextEncodingName = "UTF-8"
                    settings.textZoom = 100
                    // Preview WebViews delegate all taps/drags to the containing Compose row.
                    if (preview) setOnTouchListener { _, _ -> true }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
                        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                            if (request.url.toString() == ORIGIN + "tex-svg.js") {
                                return WebResourceResponse("application/javascript", "UTF-8", context.assets.open("math/tex-svg.js"))
                            }
                            return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                        }
                        override fun onPageFinished(view: WebView, url: String?) {
                            view.evaluateJavascript("window.finishMath && window.finishMath()", null)
                        }
                    }
                }
            },
            update = { view ->
                if (view.tag != cacheKey) {
                    view.tag = cacheKey
                    view.removeJavascriptInterface("MathResult")
                    view.addJavascriptInterface(object {
                        @JavascriptInterface fun ready(cssHeight: Double, rendered: String) {
                            view.post {
                                if (view.tag == cacheKey && cssHeight.isFinite()) {
                                    height = cssHeight.toFloat().coerceIn(linePx, 30000f)
                                    if (rendered.length < 250000) renderedMath.put(cacheKey, rendered)
                                }
                            }
                        }
                    }, "MathResult")
                    view.loadDataWithBaseURL(ORIGIN, html, "text/html", "UTF-8", null)
                }
            },
            onRelease = { view ->
                view.tag = null
                view.stopLoading()
                view.removeJavascriptInterface("MathResult")
                view.destroy()
            },
        )
        if (preview) Spacer(Modifier.matchParentSize().pointerInput(Unit) {
            awaitEachGesture { awaitFirstDown(requireUnconsumed = false); waitForUpOrCancellation() }
        })
    }
}

private fun jsString(value: String) = JSONObject.quote(value).replace("<", "\\u003c").replace(">", "\\u003e")
private fun mathPage(segments: List<MathSegment>, cached: String?, font: Float, color: String, lines: Int): String {
    val data = JSONArray().apply { segments.forEach { segment -> put(JSONObject().apply {
        put("source", segment.source); put("tex", segment.tex ?: JSONObject.NULL); put("display", segment.display)
    }) } }.toString().replace("<", "\\u003c").replace(">", "\\u003e")
    return """<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
<meta http-equiv="Content-Security-Policy" content="default-src 'none'; script-src 'unsafe-inline' https://appassets.androidplatform.net; style-src 'unsafe-inline'; img-src 'none'; connect-src 'none'">
<style>html,body{margin:0;padding:0;background:transparent;color:$color;font:${font}px/1.5 sans-serif;overflow-wrap:anywhere}#content{width:100%}.math{display:inline-block;max-width:100%;vertical-align:baseline}.block{display:block;overflow-x:auto;overflow-y:hidden;padding:4px 0}mjx-container{margin:0!important}svg{max-width:100%}.block svg{max-width:none}#content>span{white-space:pre-wrap}</style>
<script>window.MathJax={startup:{typeset:false},tex:{packages:['base','ams'],maxBuffer:10000,maxMacros:1000},svg:{fontCache:'none'},options:{enableMenu:false}};</script>
${if (cached == null) "<script src=\"${ORIGIN}tex-svg.js\"></script>" else ""}
</head><body><div id="content"></div><script>
var parts=$data, saved=${cached?.let(::jsString) ?: "null"}, root=document.getElementById('content');
var finished=false;
function appendPlain(text){(text.match(/\s+|[^\s]+/g)||[]).forEach(function(token){var s=document.createElement('span');s.textContent=token;root.appendChild(s)})}
function report(){
 var limit=$lines, cap=$font*1.5*limit;
 if(limit && root.getBoundingClientRect().height>cap+1){
   var dots=document.createElement('span');dots.textContent='…';root.appendChild(dots);
   while(root.childNodes.length>1 && root.getBoundingClientRect().height>cap+1){root.removeChild(dots.previousSibling)}
 }
 MathResult.ready(Math.ceil(root.getBoundingClientRect().height),root.innerHTML);
}
window.finishMath=function(){if(finished)return;finished=true;
 if(saved!==null){root.innerHTML=saved;report();return}
 var fallback=function(){parts.forEach(function(p){appendPlain(p.source)});report()};
 var render=function(){
  parts.forEach(function(p){
   if(p.tex===null){appendPlain(p.source);return}
   var span=document.createElement('span');span.className=p.display && !$lines?'block':'math';
   try{
    var node=MathJax.tex2svg(p.tex,{display:p.display && !$lines});
    if(node.querySelector('[data-mjx-error]'))throw Error('Invalid math');
    Array.prototype.forEach.call(node.querySelectorAll('script,foreignObject,image,a,mjx-assistive-mml'),function(n){n.parentNode.removeChild(n)});
    Array.prototype.forEach.call(node.querySelectorAll('*'),function(n){Array.prototype.slice.call(n.attributes).forEach(function(a){if(/^on|href/i.test(a.name))n.removeAttribute(a.name)})});
    span.appendChild(node);
   }catch(e){span.textContent=p.source}
   root.appendChild(span);
  });report();
 };
 if(window.MathJax && MathJax.startup && MathJax.startup.promise)MathJax.startup.promise.then(render).catch(fallback);
 else fallback();
};
</script></body></html>"""
}
