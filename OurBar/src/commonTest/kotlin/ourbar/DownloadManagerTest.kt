package com.piasy.ourbar

import com.piasy.kmpp.BtDownloader
import com.piasy.kmpp.BtDownloaderFactory
import com.piasy.kmpp.KVStorage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/13.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadManagerTest {

  private val detail = "https://movie.douban.com/subject/25921812/"
  private val hash = "f2266c7f3a32480613a6de403280497fe8e50f7c"
  private val magnet =
    "magnet:?xt=urn:btih:$hash&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce"

  @BeforeTest
  fun setup() {
    mockLogging()
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
    return MockStorage(downloads, hash, torrent, magnet, info).storage
  }

  @Test
  fun addFromScratch() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)

    val downloader = mockDownloader()
    val storage = mockStorage()
    val http = MockHttp(TEST_HTML, dispatcher)
    val listener = mockk<DownloadManagerListener>()

    val downloadManager = DownloadManager(
      true, "/tmp", CoroutineScope(dispatcher), http.http, listener, false
    )
    downloadManager.add(magnet, detail)
    advanceUntilIdle()

    verifySequence {
      // init
      storage.get(DownloadManager.KEY_DOWNLOAD_DIR)
      // start
      storage.get(DownloadManager.KEY_DOWNLOADS)

      // add
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.set(DownloadManager.KEY_DOWNLOADS, hash)
      storage.get(DownloadManager.KEY_DOWNLOADS) // check downloads after fetch
      storage.set(hash, any())
    }

    assertEquals(1, http.requests.size)
    assertEquals(detail, http.requests[0])

    verify(exactly = 1) { downloader.downloadMagnet(magnet) }
  }

  @Test
  fun addExistingMagnet() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)

    val downloader = mockDownloader()
    val storage = mockStorage(hash, hash, "")
    val http = MockHttp(TEST_HTML, dispatcher)
    val listener = mockk<DownloadManagerListener>()

    val downloadManager = DownloadManager(
      true, "/tmp", CoroutineScope(dispatcher), http.http, listener, false
    )
    downloadManager.add(magnet, detail)
    advanceUntilIdle()

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

    assertEquals(0, http.requests.size)

    verify(exactly = 1) { downloader.downloadMagnet(magnet) }
  }

  @Test
  fun addExistingTorrent() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)

    val downloader = mockDownloader()
    val http = MockHttp(TEST_HTML, dispatcher)

    val torrent = "/storage/emulated/0/Download/f2266c7f3a32480613a6de403280497fe8e50f7c.torrent"
    val storage = mockStorage(hash, hash, torrent)
    val listener = mockk<DownloadManagerListener>()

    val downloadManager = DownloadManager(
      true, "/tmp", CoroutineScope(dispatcher), http.http, listener, false
    )
    downloadManager.add(magnet, detail)
    advanceUntilIdle()

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

    assertEquals(0, http.requests.size)

    verify(exactly = 1) { downloader.downloadTorrent(torrent) }
  }

  @Test
  fun addRetryGetInfo() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)

    val downloader = mockDownloader()
    val http = MockHttp(TEST_HTML, dispatcher)
    val storage = mockStorage(
      hash,
      hash,
      "",
      "{\"hash\":\"$hash\",\"torrent\":\"\",\"magnet\":\"$magnet\",\"filmInfo\":{\"name\":\"\",\"year\":\"\",\"mainPic\":\"\",\"duration\":\"\",\"rating\":\"\"}}"
    )
    val listener = mockk<DownloadManagerListener>()

    val downloadManager = DownloadManager(
      true, "/tmp", CoroutineScope(dispatcher), http.http, listener, false
    )
    downloadManager.add(magnet, detail)
    advanceUntilIdle()

    verifySequence {
      // init
      storage.get(DownloadManager.KEY_DOWNLOAD_DIR)
      // start
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.get(hash)

      // add
      storage.get(DownloadManager.KEY_DOWNLOADS)
      storage.get(hash)
      storage.get(DownloadManager.KEY_DOWNLOADS) // check downloads after fetch
      storage.set(hash, any())
    }

    assertEquals(1, http.requests.size)
    assertEquals(detail, http.requests[0])

    verify(exactly = 1) { downloader.downloadMagnet(magnet) }
  }
}
