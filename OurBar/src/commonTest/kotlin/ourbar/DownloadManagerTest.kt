package com.piasy.ourbar

import com.piasy.kmpp.BtDownloader
import com.piasy.kmpp.BtDownloaderFactory
import com.piasy.kmpp.Http
import com.piasy.kmpp.HttpFactory
import com.piasy.kmpp.KVStorage
import com.piasy.kmpp.KVStorageFactory
import com.piasy.kmpp.LoggingImpl
import com.piasy.kmpp.WorkerTaskQueue
import com.piasy.kmpp.WorkerTaskQueueFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/13.
 */
internal class DownloadManagerTest {

  private val detail = "https://movie.douban.com/subject/25921812/"
  private val hash = "f2266c7f3a32480613a6de403280497fe8e50f7c"
  private val magnet =
    "magnet:?xt=urn:btih:$hash&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce"

  @BeforeTest
  fun setup() {
    mockkStatic(LoggingImpl::class)
    every { LoggingImpl.info(any(), any()) } returns Unit
    every { LoggingImpl.error(any(), any()) } returns Unit

    val queue = object : WorkerTaskQueue<HashMap<String, Any>> {
      private var state = HashMap<String, Any>()

      override fun state(): HashMap<String, Any> {
        return state
      }

      override fun updateState(newState: HashMap<String, Any>): HashMap<String, Any> {
        state = newState
        return state
      }

      override fun execute(task: () -> Unit) {
        task()
      }

      override fun executeAfter(millis: Long, task: () -> Unit) {
      }

      override fun shutdown() {
      }
    }

    mockkObject(WorkerTaskQueueFactory)
    every { WorkerTaskQueueFactory.createQueue<HashMap<String, Any>>(any()) } returns queue
  }

  private fun mockHttp(): Http {
    val http = mockk<Http>()
    every { http.get(any()) } returns TEST_HTML
    mockkObject(HttpFactory)
    every { HttpFactory.createHttp(any()) } returns http
    return http
  }

  private fun mockDownloader(): BtDownloader {
    val downloader = mockk<BtDownloader>()
    every { downloader.postStatus() } returns Unit
    every { downloader.downloadMagnet(any()) } returns Unit
    every { downloader.downloadTorrent(any()) } returns Unit

    mockkObject(BtDownloaderFactory)
    every { BtDownloaderFactory.createDownloader(any(), any(), any()) } returns downloader

    return downloader
  }

  private fun mockStorage(
    downloads: String? = null,
    hash: String? = null,
    torrent: String? = null,
    info: String? = null,
  ): KVStorage {
    val storage = mockk<KVStorage>()
    every { storage.get(DownloadManager.KEY_DOWNLOAD_DIR) } returns null
    every { storage.get(DownloadManager.KEY_DOWNLOADS) } returns downloads
    every { storage.set(any(), any()) } returns Unit

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

    return storage
  }

  @Test
  fun addFromScratch() {
    val downloader = mockDownloader()
    val storage = mockStorage()
    val http = mockHttp()

    val downloadManager = DownloadManager(true, "/tmp", mockk())
    downloadManager.add(magnet, detail)

    verifySequence {
      // init
      storage.get(DownloadManager.KEY_DOWNLOAD_DIR)
      // start
      storage.get(DownloadManager.KEY_DOWNLOADS)

      // add
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.set(DownloadManager.KEY_DOWNLOADS, hash)
      storage.set(hash, any())
    }

    verify(exactly = 1) { http.get(detail) }
    verify { downloader.postStatus() }
    verify(exactly = 1) { downloader.downloadMagnet(magnet) }
  }

  @Test
  fun addExistingMagnet() {
    val downloader = mockDownloader()
    val storage = mockStorage(hash, hash, "")
    val http = mockHttp()

    val downloadManager = DownloadManager(true, "/tmp", mockk())
    downloadManager.add(magnet, detail)

    verifySequence {
      // init
      storage.get(DownloadManager.KEY_DOWNLOAD_DIR)
      // start
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.get(hash)

      // add
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.get(hash)
    }

    verify(exactly = 0) { http.get(detail) }
    verify { downloader.postStatus() }
    verify(exactly = 1) { downloader.downloadMagnet(magnet) }
  }

  @Test
  fun addExistingTorrent() {
    val downloader = mockDownloader()
    val http = mockHttp()

    val torrent = "/storage/emulated/0/Download/f2266c7f3a32480613a6de403280497fe8e50f7c.torrent"
    val storage = mockStorage(hash, hash, torrent)

    val downloadManager = DownloadManager(true, "/tmp", mockk())
    downloadManager.add(magnet, detail)

    verifySequence {
      // init
      storage.get(DownloadManager.KEY_DOWNLOAD_DIR)
      // start
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.get(hash)

      // add
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.get(hash)
    }

    verify(exactly = 0) { http.get(detail) }
    verify { downloader.postStatus() }
    verify(exactly = 1) { downloader.downloadTorrent(torrent) }
  }

  @Test
  fun addRetryGetInfo() {
    val downloader = mockDownloader()
    val http = mockHttp()
    val storage = mockStorage(hash, hash, "", "{\"hash\":\"$hash\",\"torrent\":\"\",\"magnet\":\"$magnet\",\"filmInfo\":{\"name\":\"\",\"year\":\"\",\"mainPic\":\"\",\"duration\":\"\",\"rating\":\"\"}}")

    val downloadManager = DownloadManager(true, "/tmp", mockk())
    downloadManager.add(magnet, detail)

    verifySequence {
      // init
      storage.get(DownloadManager.KEY_DOWNLOAD_DIR)
      // start
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.get(hash)

      // add
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.get(hash)
      storage.set(hash, any())
    }

    verify(exactly = 1) { http.get(detail) }
    verify { downloader.postStatus() }
    verify(exactly = 1) { downloader.downloadMagnet(magnet) }
  }
}
