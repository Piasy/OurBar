package com.piasy.ourbar.data

import kotlinx.serialization.Serializable

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/13.
 */
@Serializable
data class DownloadInfo(
  val hash: String,
  val torrent: String,
  val magnet: String,
) {
  fun onTorrentSaved(torrent: String): DownloadInfo {
    return DownloadInfo(hash, torrent, magnet)
  }

  companion object {
    fun create(magnet: String): DownloadInfo? {
      // magnet:?xt=urn:btih:f2266c7f3a32480613a6de403280497fe8e50f7c&dn=....
      val parts1 = magnet.split("xt=")
      if (parts1.size == 2) {
        val parts2 = parts1[1].split("&")
        if (parts2.size > 1) {
          val parts3 = parts2[0].split(":")
          if (parts3.size == 3 && parts3[0] == "urn") {
            return DownloadInfo(parts3[2], "", magnet)
          }
        }
      }

      return null
    }
  }
}
