package org.zotero.android.api

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NativeLoginTest {
    @Test fun credentialsAreExchangedForAKeyUsingTheExistingApiContract() = runBlocking {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request()
            assertEquals("/keys", request.url.encodedPath)
            assertEquals("POST", request.method)
            val body = Buffer().apply { request.body!!.writeTo(this) }.readUtf8()
            val json = Gson().fromJson(body, com.google.gson.JsonObject::class.java)
            assertEquals("example", json["username"].asString)
            assertTrue(json["access"].asJsonObject["user"].asJsonObject["library"].asBoolean)
            Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(201).message("Created")
                .body("""{"key":"test-key","userID":123,"username":"example"}""".toResponseBody()).build()
        }.build()
        val api = Retrofit.Builder().baseUrl("https://api.zotero.org/").client(client)
            .addConverterFactory(GsonConverterFactory.create()).build().create(AuthApi::class.java)
        val request = NativeLoginRequest("example", "test-password")
        assertFalse(request.toString().contains("test-password"))
        val result = api.signIn(request)
        assertTrue(result.body()!!.isValid())
        assertEquals(123L, result.body()!!.userID)
    }
    @Test fun malformedSuccessCannotInitializeAnAccount() {
        assertFalse(NativeLoginResponse("", 1, "name").isValid())
        assertFalse(NativeLoginResponse("key", null, "name").isValid())
        assertFalse(NativeLoginResponse("key", 1, null).isValid())
    }
}
