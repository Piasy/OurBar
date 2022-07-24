package com.piasy.ourbar

import io.ktor.client.engine.config
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondBadRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/17.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FilmInfoFetcherTest {

  @BeforeTest
  fun setup() {
    mockLogging()
  }

  @Test
  fun testFetchSuccess() = runTest {
    val url = "https://movie.douban.com/subject/1295644/"

    val http = MockHttp(TEST_HTML, StandardTestDispatcher(testScheduler))

    val fetcher = FilmInfoFetcher(http.http)
    val info = fetcher.fetch(url)

    println(info)

    assertEquals(1, http.requests.size)
    assertEquals(url, http.requests[0])

    assertEquals("驴得水", info.name)
    assertEquals("2016", info.year)
    assertEquals(
      "https://img2.doubanio.com/view/photo/s_ratio_poster/public/p2393044761.jpg",
      info.mainPic
    )
    assertEquals("111分钟", info.duration)
    assertEquals("8.3", info.rating)
  }

  @Test
  fun testFetchTimeout() = runTest {
    val url = "https://movie.douban.com/subject/1295644/"

    val mockEngine = MockEngine.config {
      addHandler {
        delay(200)
        respondBadRequest()
      }
    }

    val fetcher = FilmInfoFetcher(mockEngine, 100)
    val info = fetcher.fetch(url)

    println(info)

    assertEquals("", info.name)
    assertEquals("", info.year)
    assertEquals("", info.mainPic)
    assertEquals("", info.duration)
    assertEquals("", info.rating)
  }
}
