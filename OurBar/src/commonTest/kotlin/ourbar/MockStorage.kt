package com.piasy.ourbar

import com.piasy.kmpp.KVStorage
import com.piasy.kmpp.KVStorageFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/24.
 */
class MockStorage(
  private var downloads: String? = null,
  hash: String? = null,
  torrent: String? = null,
  magnet: String? = null,
  info: String? = null,
) {
  val storage = mockk<KVStorage>()

  init {
    every { storage.get(DownloadManager.KEY_DOWNLOAD_DIR) } returns null
    every { storage.get(DownloadManager.KEY_DOWNLOADS) } answers {
      println("MockStorage get downloads: $downloads")
      downloads
    }
    every { storage.set(any(), any()) } answers {
      val key = firstArg<String>()
      val value = secondArg<String>()
      println("MockStorage set $key => $value")
      if (key == DownloadManager.KEY_DOWNLOADS) {
        downloads = value
      }
    }

    if (hash != null) {
      val mockInfo = info ?: if (torrent != null) {
        "{\"hash\":\"$hash\",\"torrent\":\"$torrent\",\"magnet\":\"$magnet\",\"filmInfo\":{\"name\":\"name value\",\"year\":\"year value\",\"mainPic\":\"mainPic value\",\"duration\":\"duration value\",\"rating\":\"rating value\"}}"
      } else {
        null
      }

      if (mockInfo != null) {
        every { storage.get(hash) } returns mockInfo
      }
    }

    mockkObject(KVStorageFactory)
    every { KVStorageFactory.createKVStorage(any()) } returns storage
  }
}
