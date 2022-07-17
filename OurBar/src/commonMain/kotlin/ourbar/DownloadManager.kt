package com.piasy.ourbar

import com.piasy.kmpp.BtDownloader
import com.piasy.kmpp.BtDownloaderFactory
import com.piasy.kmpp.DownloadListener
import com.piasy.kmpp.DownloadStatus
import com.piasy.kmpp.HttpFactory
import com.piasy.kmpp.KVStorageFactory
import com.piasy.kmpp.Logging
import com.piasy.ourbar.data.DownloadInfo
import kotlinx.serialization.json.Json

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/13.
 */
interface DownloadManagerListener {
  fun onDownloadStatus(status: HashMap<String, DownloadStatus>)
}

class DownloadManager(
  context: Any,
  defaultDir: String,
  private val listener: DownloadManagerListener
) :
  DownloadListener {
  private val queue = OurBarWorkerTaskQueue()
  private val storage = KVStorageFactory.createKVStorage(context)
  private val json = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
  }
  private val downloader: BtDownloader
  private val filmInfoFetcher = FilmInfoFetcher(HttpFactory.createHttp(8000))

  init {
    queue.updateDownloadState(DownloadState.initState())

    val dir = storage.get(KEY_DOWNLOAD_DIR)
    Logging.info(TAG, "init with dir: $dir")
    downloader = BtDownloaderFactory.createDownloader(context, dir ?: defaultDir, this)

    start()
  }

  fun changeDownloadDir(dir: String) {
    queue.execute {
      Logging.info(TAG, "changeDownloadDir $dir")
      storage.set(KEY_DOWNLOAD_DIR, dir)
      downloader.changeDownloadDir(dir)
    }
  }

  private fun start() {
    queue.execute {
      Logging.info(TAG, "start")
      val downloads = storage.get(KEY_DOWNLOADS)
      Logging.info(TAG, "previous downloads: $downloads")

      if (downloads != null) {
        for (hash in downloads.split(",")) {
          val infoStr = storage.get(hash) ?: continue
          val info = json.decodeFromString(DownloadInfo.serializer(), infoStr)
          when {
            info.torrent != "" -> downloader.downloadTorrent(info.torrent)
            info.magnet != "" -> downloader.downloadMagnet(info.magnet)
          }
        }
      }

      getDownloadStatus()
    }
  }

  fun add(magnet: String, detail: String) {
    queue.execute {
      Logging.info(TAG, "add $magnet, $detail")
      val info = DownloadInfo.create(magnet) ?: return@execute

      val downloads = storage.get(KEY_DOWNLOADS)
      if (downloads == null) {
        doAdd(info, detail, downloads)
      } else {
        val set = HashSet(downloads.split(","))
        if (!set.contains(info.hash)) {
          doAdd(info, detail, downloads)
        } else {
          val info2Str = storage.get(info.hash)
          Logging.info(TAG, "add existed $info2Str, magnet $magnet")
          if (info2Str == null) {
            return@execute
          }
          val info2 = json.decodeFromString(DownloadInfo.serializer(), info2Str)
          if (info2.filmInfo.name == "") {
            Logging.info(TAG, "retry get film info")
            storage.set(
              info.hash,
              json.encodeToString(
                DownloadInfo.serializer(),
                info2.setInfo(filmInfoFetcher.fetch(detail))
              )
            )
          }
        }
      }
    }
  }

  private fun doAdd(info: DownloadInfo, detail: String, downloads: String?) {
    if (downloads == null) {
      storage.set(KEY_DOWNLOADS, info.hash)
    } else {
      val set = HashSet(downloads.split(","))
      set.add(info.hash)
      storage.set(KEY_DOWNLOADS, set.joinToString(","))
    }

    storage.set(
      info.hash,
      json.encodeToString(DownloadInfo.serializer(), info.setInfo(filmInfoFetcher.fetch(detail)))
    )

    downloader.downloadMagnet(info.magnet)
  }

  fun remove(hash: String) {
    queue.execute {
      Logging.info(TAG, "remove $hash")
    }
  }

  fun stop() {
    queue.execute {
      Logging.info(TAG, "stop")
      downloader.stop()
      queue.shutdown()
    }
  }

  private fun getDownloadStatus() {
    downloader.postStatus()

    queue.executeAfter(GET_STATUS_INTERVAL_MS) {
      getDownloadStatus()
    }
  }

  override fun onDownloadStatus(hash: String, status: DownloadStatus) {
    queue.execute {
      val state = queue.updateDownloadState(queue.downloadState().updateStatus(hash, status))
      listener.onDownloadStatus(state.status)
    }
  }

  override fun onTorrentSaved(hash: String, torrent: String) {
    queue.execute {
      Logging.info(TAG, "onTorrentSaved $hash, $torrent")
      val infoStr = storage.get(hash) ?: return@execute
      val info = json.decodeFromString(DownloadInfo.serializer(), infoStr)
      val updatedInfo = json.encodeToString(DownloadInfo.serializer(), info.onTorrentSaved(torrent))
      storage.set(hash, updatedInfo)
    }
  }

  companion object {
    private const val TAG = "DownloadManager"

    internal const val KEY_DOWNLOADS = "downloads"
    internal const val KEY_DOWNLOAD_DIR = "download_dir"

    private const val GET_STATUS_INTERVAL_MS = 1000L
  }
}
