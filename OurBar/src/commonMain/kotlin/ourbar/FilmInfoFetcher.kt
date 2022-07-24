package com.piasy.ourbar

import com.piasy.ourbar.data.FilmInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/17.
 */
class FilmInfoFetcher(
  httpFactory: HttpClientEngineFactory<HttpClientEngineConfig>,
  timeoutMs: Long = 8000
) {
  private val http = HttpClient(httpFactory) {
    install(HttpTimeout) {
      requestTimeoutMillis = timeoutMs
    }

    install(Logging) {
      logger = object : Logger {
        override fun log(message: String) {
          com.piasy.kmpp.Logging.info("Http", message)
        }
      }
      level = LogLevel.INFO
    }
  }

  suspend fun fetch(url: String): FilmInfo {
    val response = kotlin.runCatching {
      http.get(url).bodyAsText().replace("\n", "")
    }
    val html = response.getOrDefault("")

    val regex =
      """(^.*v:itemreviewed">)(.*?)(<.*year">\()(.*?)(\).*?mainpic.*?src=")(.*?)(".*v:runtime.*?>)(.*?)(</span>)(.*?)(<br/>.*v:average">)(.*?)(<.*$)""".toRegex()
    val res = regex.find(html)

    var name = ""
    var year = ""
    var mainPic = ""
    var duration = ""
    var rating = ""

    if (res != null && res.groups.size == 14) {
      name = res.groups[2]?.value ?: ""
      year = res.groups[4]?.value ?: ""
      mainPic = res.groups[6]?.value ?: ""
      duration = (res.groups[8]?.value ?: "") + (res.groups[10]?.value ?: "")
      rating = res.groups[12]?.value ?: ""
    }

    return FilmInfo(name, year, mainPic, duration, rating)
  }
}
