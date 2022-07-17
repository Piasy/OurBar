package com.piasy.ourbar

import com.piasy.kmpp.Http
import com.piasy.ourbar.data.FilmInfo

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/17.
 */
class FilmInfoFetcher(private val http: Http) {
  fun fetch(url: String): FilmInfo {
    val html = http.get(url).replace("\n", "")

    val regex = """(^.*v:itemreviewed">)(.*?)(<.*year">\()(.*?)(\).*?mainpic.*?src=")(.*?)(".*v:runtime.*?>)(.*?)(</span>)(.*?)(<br/>.*v:average">)(.*?)(<.*$)""".toRegex()
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
