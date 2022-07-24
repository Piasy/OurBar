package com.piasy.kmpp

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/23.
 */
actual object BtDownloaderFactory {
  actual fun createDownloader(context: Any, dir: String, listener: DownloadListener): BtDownloader {
    return IOSBtDownloader(listener)
  }
}

internal class IOSBtDownloader(
  private val listener: DownloadListener
) : BtDownloader {

  override fun changeDownloadDir(dir: String) {
  }

  override fun downloadMagnet(magnet: String) {
    Logging.info(TAG, "downloadMagnet $magnet")
  }

  override fun downloadTorrent(torrent: String) {
    Logging.info(TAG, "downloadTorrent $torrent")
  }

  override fun postStatus() {
    listener.onDownloadStatus(
      "66ed614c637d976709531019a383c39241ff349c",
      DownloadStatus(0, 0, 0, 0, 0, 0, 0F, 0, 0, 0)
    )
  }

  override fun stop() {
  }

  companion object {
    private const val TAG = "BtDownloader"
  }
}
