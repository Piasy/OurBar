package com.piasy.ourbar

import com.piasy.kmpp.BtDownloader
import com.piasy.kmpp.BtDownloaderFactory
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

  @BeforeTest
  fun setup() {
    mockkStatic(LoggingImpl::class)
    every { LoggingImpl.info(any(), any()) } returns Unit
    every { LoggingImpl.error(any(), any()) } returns Unit

    val queue = object : WorkerTaskQueue<Boolean> {
      override fun state(): Boolean {
        return true
      }

      override fun updateState(newState: Boolean): Boolean {
        return true
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
    every { WorkerTaskQueueFactory.createQueue<Boolean>(any()) } returns queue
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
    info: String? = null
  ): KVStorage {
    val storage = mockk<KVStorage>()
    every { storage.get(DownloadManager.KEY_DOWNLOAD_DIR) } returns null
    every { storage.get(DownloadManager.KEY_DOWNLOADS) } returns downloads
    every { storage.set(any(), any()) } returns Unit

    if (hash != null && info != null) {
      every { storage.get(hash) } returns info
    }

    mockkObject(KVStorageFactory)
    every { KVStorageFactory.createKVStorage(any()) } returns storage

    return storage
  }

  @Test
  fun addFromScratch() {
    val downloader = mockDownloader()
    val storage = mockStorage()

    val downloadManager = DownloadManager(true, "/tmp")
    val hash = "f2266c7f3a32480613a6de403280497fe8e50f7c"
    val magnet =
      "magnet:?xt=urn:btih:$hash&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce"
    downloadManager.add(magnet, "")

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

    verify { downloader.postStatus() }
    verify(exactly = 1) { downloader.downloadMagnet(magnet) }
  }

  @Test
  fun addExistingMagnet() {
    val downloader = mockDownloader()

    val hash = "f2266c7f3a32480613a6de403280497fe8e50f7c"
    val info =
      "{\"hash\":\"f2266c7f3a32480613a6de403280497fe8e50f7c\",\"torrent\":\"\",\"magnet\":\"magnet:?xt=urn:btih:f2266c7f3a32480613a6de403280497fe8e50f7c&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce\"}"
    val storage = mockStorage(hash, hash, info)

    val downloadManager = DownloadManager(true, "/tmp")
    val magnet =
      "magnet:?xt=urn:btih:$hash&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce"
    downloadManager.add(magnet, "")

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

    verify { downloader.postStatus() }
    verify(exactly = 1) { downloader.downloadMagnet(magnet) }
  }

  @Test
  fun addExistingTorrent() {
    val downloader = mockDownloader()

    val hash = "f2266c7f3a32480613a6de403280497fe8e50f7c"
    val torrent = "/storage/emulated/0/Download/f2266c7f3a32480613a6de403280497fe8e50f7c.torrent"
    val info =
      "{\"hash\":\"f2266c7f3a32480613a6de403280497fe8e50f7c\",\"torrent\":\"$torrent\",\"magnet\":\"magnet:?xt=urn:btih:f2266c7f3a32480613a6de403280497fe8e50f7c&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce\"}"
    val storage = mockStorage(hash, hash, info)

    val downloadManager = DownloadManager(true, "/tmp")
    val magnet =
      "magnet:?xt=urn:btih:$hash&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce"
    downloadManager.add(magnet, "")

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

    verify { downloader.postStatus() }
    verify(exactly = 1) { downloader.downloadTorrent(torrent) }
  }
}
