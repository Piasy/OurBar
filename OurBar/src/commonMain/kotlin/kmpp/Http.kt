package com.piasy.kmpp

/**
 * Created by Piasy{github.com/Piasy} on 2019-11-30.
 */
const val HTTP_CONTENT_TYPE_FORM = "application/x-www-form-urlencoded"
const val HTTP_CONTENT_TYPE_JSON = "application/json"

interface Http {
  fun get(url: String): String

  fun post(
    url: String,
    contentType: String,
    body: String
  ): String

  fun patch(
    url: String,
    contentType: String,
    body: String
  ): String

  fun delete(url: String): String

  fun getAsync(url: String, callback: (String) -> Unit) {
    callback("")
  }

  fun postAsync(
    url: String,
    contentType: String,
    body: String,
    callback: (String) -> Unit
  ) {
    callback("")
  }

  fun deleteAsync(url: String, callback: (String) -> Unit) {
    callback("")
  }

  companion object {
    const val TAG = "Http"
  }
}

expect object HttpFactory {
  fun createHttp(timeoutMs: Long): Http
}
