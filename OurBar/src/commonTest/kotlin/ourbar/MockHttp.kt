package com.piasy.ourbar

import io.ktor.client.engine.config
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/24.
 */
class MockHttp(resp: String, testDispatcher: CoroutineDispatcher) {
  val requests = ArrayList<String>()

  val http = MockEngine.config {
    dispatcher = testDispatcher

    addHandler { request ->
      println("${currentCoroutineContext()} MockHttp request $request")
      requests.add(request.url.toString())

      respondOk(resp)
    }
  }
}
