package com.piasy.ourbar

import com.piasy.kmpp.BtDownloader
import com.piasy.kmpp.BtDownloaderFactory
import com.piasy.kmpp.DownloadListener
import com.piasy.kmpp.DownloadStatus
import com.piasy.kmpp.KVStorageFactory
import com.piasy.kmpp.Logging
import com.piasy.ourbar.data.DownloadInfo
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/13.
 */
interface DownloadManagerListener {
  fun onDownloadStatus(status: HashMap<String, DownloadStatus>)
}

class DownloadManager constructor(
  context: Any,
  defaultDir: String,
  private val scope: CoroutineScope,
  http: HttpClientEngineFactory<HttpClientEngineConfig>,
  private val listener: DownloadManagerListener,
  private val getStatus: Boolean = true,
) : DownloadListener {
  private val storage = KVStorageFactory.createKVStorage(context)
  private val json = Json {
    encodeDefaults = false
    ignoreUnknownKeys = true
  }
  private val downloader: BtDownloader
  private val filmInfoFetcher = FilmInfoFetcher(http)

  private val status = HashMap<String, DownloadStatus>()

  init {
    val dir = storage.get(KEY_DOWNLOAD_DIR)
    Logging.info(TAG, "init with dir: $dir")
    downloader = BtDownloaderFactory.createDownloader(context, dir ?: defaultDir, this)

    start()
  }

  fun changeDownloadDir(dir: String) {
    scope.launch {
      Logging.info(TAG, "changeDownloadDir $dir")
      storage.set(KEY_DOWNLOAD_DIR, dir)
      downloader.changeDownloadDir(dir)
    }
  }

  private fun start() {
    scope.launch {
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

      if (getStatus) {
        getDownloadStatus()
      }
    }
  }

  fun add(magnet: String, detail: String) {
    scope.launch {
      Logging.info(TAG, "add $magnet, $detail")
      val info = DownloadInfo.create(magnet) ?: return@launch

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

          val hasInfo = if (info2Str == null) {
            false
          } else {
            val info2 = json.decodeFromString(DownloadInfo.serializer(), info2Str)
            info2.filmInfo.name != ""
          }
          if (hasInfo) {
            return@launch
          }
          Logging.info(TAG, "retry get film info")

          val info3 = filmInfoFetcher.fetch(detail)
          // when we get info3, the download may be removed, check it
          if (!hasDownload(info.hash)) {
            return@launch
          }
          storage.set(
            info.hash,
            json.encodeToString(DownloadInfo.serializer(), info.setInfo(info3))
          )
        }
      }
    }
  }

  private suspend fun doAdd(info: DownloadInfo, detail: String, downloads: String?) {
    if (downloads == null) {
      storage.set(KEY_DOWNLOADS, info.hash)
    } else {
      val set = HashSet(downloads.split(","))
      set.add(info.hash)
      storage.set(KEY_DOWNLOADS, set.joinToString(","))
    }

    val filmInfo = filmInfoFetcher.fetch(detail)
    println("XXPXX doAdd filmInfo $filmInfo")
    // when we get filmInfo, the download may be removed, check it
    if (!hasDownload(info.hash)) {
      return
    }
    storage.set(
      info.hash,
      json.encodeToString(DownloadInfo.serializer(), info.setInfo(filmInfo))
    )

    downloader.downloadMagnet(info.magnet)
  }

  private fun hasDownload(hash: String): Boolean {
    val downloads = storage.get(KEY_DOWNLOADS) ?: return false
    return HashSet(downloads.split(",")).contains(hash)
  }

  fun remove(hash: String) {
    scope.launch {
      Logging.info(TAG, "remove $hash")
    }
  }

  fun stop() {
    scope.launch {
      Logging.info(TAG, "stop")
      downloader.stop()
    }
  }

  private fun getDownloadStatus() {
    downloader.postStatus()

    scope.launch {
      delay(GET_STATUS_INTERVAL_MS)
      getDownloadStatus()
    }
  }

  override fun onDownloadStatus(hash: String, downloadStatus: DownloadStatus) {
    scope.launch {
      status[hash] = downloadStatus
      listener.onDownloadStatus(status)
    }
  }

  override fun onTorrentSaved(hash: String, torrent: String) {
    scope.launch {
      Logging.info(TAG, "onTorrentSaved $hash, $torrent")
      val infoStr = storage.get(hash) ?: return@launch
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
