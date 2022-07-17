package com.piasy.kmpp

import android.text.TextUtils
import com.piasy.kmpp.Http.Companion.TAG
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by Piasy{github.com/Piasy} on 2019-11-30.
 */
actual object HttpFactory {
  actual fun createHttp(
    timeoutMs: Long,
  ): Http {
    return AndroidOkHttp(timeoutMs)
  }
}

private class AndroidOkHttp(
  timeoutMs: Long,
) : Http {
  private val okHttp: OkHttpClient

  init {
    val logging = HttpLoggingInterceptor { message -> Logging.info(TAG, message) }
    logging.level = HttpLoggingInterceptor.Level.BASIC

    val builder = OkHttpClient.Builder()
      .addInterceptor(logging)
      .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
      .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)

    okHttp = builder.build()
  }

  override fun get(url: String): String {
    Logging.info(TAG, "get $url")
    val request = Request.Builder()
      .url(url)
      .get()
      .build()
    try {
      val response = okHttp.newCall(request)
        .execute()
      val responseBody = response.body
      if (responseBody == null) {
        Logging.error(TAG, "get $url fail, null body")
        return ""
      }
      val resp = responseBody.string()
      Logging.info(TAG, "get $url success, ${resp.length}")
      return resp
    } catch (e: IOException) {
      Logging.error(TAG, "get $url fail: ${e.message}")
      return ""
    }
  }

  override fun post(
    url: String,
    contentType: String,
    body: String
  ): String {
    Logging.info(TAG, "post $url, $contentType, $body")
    val request = Request.Builder()
      .post(
        (if (TextUtils.isEmpty(body)) "" else body).toRequestBody(contentType.toMediaTypeOrNull())
      )
      .url(url)
      .build()
    try {
      val response = okHttp.newCall(request)
        .execute()
      val responseBody = response.body
      if (responseBody == null) {
        Logging.error(TAG, "post $url fail, null body")
        return ""
      }
      val resp = responseBody.string()
      Logging.info(TAG, "post $url success, $resp")
      return resp
    } catch (e: IOException) {
      Logging.error(TAG, "post $url fail: ${e.message}")
      return ""
    }
  }

  override fun patch(
    url: String,
    contentType: String,
    body: String
  ): String {
    Logging.info(TAG, "patch $url, $contentType, $body")
    val request = Request.Builder()
      .patch(
        (if (TextUtils.isEmpty(body)) "" else body).toRequestBody(contentType.toMediaTypeOrNull())
      )
      .url(url)
      .build()
    try {
      val response = okHttp.newCall(request)
        .execute()
      val responseBody = response.body
      if (responseBody == null) {
        Logging.error(TAG, "patch $url fail, null body")
        return ""
      }
      val resp = responseBody.string()
      Logging.info(TAG, "patch $url success, $resp")
      return resp
    } catch (e: IOException) {
      Logging.error(TAG, "patch $url fail: ${e.message}")
      return ""
    }
  }

  override fun delete(url: String): String {
    Logging.info(TAG, "delete $url")
    val request = Request.Builder()
      .url(url)
      .delete()
      .build()
    try {
      val response = okHttp.newCall(request)
        .execute()
      val responseBody = response.body
      if (responseBody == null) {
        Logging.error(TAG, "delete $url fail, null body")
        return ""
      }
      val resp = responseBody.string()
      Logging.info(TAG, "delete $url success, $resp")
      return resp
    } catch (e: IOException) {
      Logging.error(TAG, "delete $url fail: ${e.message}")
      return ""
    }
  }
}
