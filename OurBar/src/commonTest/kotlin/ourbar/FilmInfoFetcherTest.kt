package com.piasy.ourbar

import com.piasy.kmpp.Http
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/17.
 */
class FilmInfoFetcherTest {

  @Test
  fun testFetch() {
    val url = "https://movie.douban.com/subject/1295644/"

    val http = mockk<Http>()
    every { http.get(url) } returns TEST_HTML

    val fetcher = FilmInfoFetcher(http)
    val info = fetcher.fetch(url)

    verify(exactly = 1) {
      http.get(url)
    }

    println(info)
    assertEquals("驴得水", info.name)
    assertEquals("2016", info.year)
    assertEquals("https://img2.doubanio.com/view/photo/s_ratio_poster/public/p2393044761.jpg", info.mainPic)
    assertEquals("111分钟", info.duration)
    assertEquals("8.3", info.rating)
  }
}
