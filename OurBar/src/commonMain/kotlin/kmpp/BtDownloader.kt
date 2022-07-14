package com.piasy.kmpp

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/9.
 */

/**
 * `changeDownloadDir` should be called before `download*`.
 */
data class DownloadStatus(
  val timestamp: Long,
  val state: Int,
  val seederCount: Int,
  val peerCount: Int,
  val downloadRate: Int,
  val uploadRate: Int,
  val progress: Float,
  val bytesDownloaded: Long,
  val bytesWanted: Long,
  val allTimeUploaded: Long,
) {
  companion object {
    const val STATE_CHECKING_FILES = 0
    const val STATE_DOWNLOADING_METADATA = 1
    const val STATE_DOWNLOADING = 2
    const val STATE_FINISHED = 3
    const val STATE_SEEDING = 4
    const val STATE_CHECKING_RESUME_DATA = 5
  }
}

interface DownloadListener {
  fun onDownloadStatus(hash: String, status: DownloadStatus)

  fun onTorrentSaved(hash: String, torrent: String)
}

interface BtDownloader {
  fun changeDownloadDir(dir: String)

  fun downloadMagnet(magnet: String)
  fun downloadTorrent(torrent: String)

  fun postStatus()

  fun stop()
}

expect object BtDownloaderFactory {
  fun createDownloader(context: Any, dir: String, listener: DownloadListener): BtDownloader
}
