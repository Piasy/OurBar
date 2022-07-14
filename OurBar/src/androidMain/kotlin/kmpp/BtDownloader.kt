package com.piasy.kmpp

import android.content.Context
import android.net.Uri
import com.frostwire.jlibtorrent.TorrentHandle
import com.masterwok.simpletorrentandroid.TorrentSession
import com.masterwok.simpletorrentandroid.TorrentSessionOptions
import com.masterwok.simpletorrentandroid.contracts.TorrentSessionListener
import com.masterwok.simpletorrentandroid.models.TorrentSessionStatus
import java.io.File
import java.io.FileOutputStream
import java.util.Arrays

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/9.
 */
actual object BtDownloaderFactory {
  actual fun createDownloader(context: Any, dir: String, listener: DownloadListener): BtDownloader {
    return AndroidBtDownloader(context as Context, dir, listener)
  }
}

internal fun TorrentSessionStatus.toDownloadStatus() = DownloadStatus(
  System.currentTimeMillis(), state.ordinal, seederCount, peerCount, downloadRate, uploadRate,
  progress, bytesDownloaded, bytesWanted, allTimeUploaded
)

internal class AndroidBtDownloader(
  private val context: Context,
  private var dir: String,
  private val listener: DownloadListener
) : BtDownloader,
  TorrentSessionListener {
  private var session: TorrentSession
  private val torrents = HashMap<String, String>()

  init {
    session = createSession(dir)
  }

  private fun createSession(dir: String): TorrentSession {
    val options = TorrentSessionOptions(
      downloadLocation = File(dir),
      onlyDownloadLargestFile = true,
      enableLogging = false,
      shouldStream = false
    )
    val session = TorrentSession(options)
    session.listener = this
    return session
  }

  override fun changeDownloadDir(dir: String) {
    this.dir = dir
    session = createSession(dir)
  }

  override fun downloadMagnet(magnet: String) {
    Logging.info(TAG, "downloadMagnet $magnet")
    session.start(context, Uri.parse(magnet))
  }

  override fun downloadTorrent(torrent: String) {
    Logging.info(TAG, "downloadTorrent $torrent")
    session.start(context, Uri.fromFile(File(torrent)))
  }

  override fun postStatus() {
    session.sessionStatus()
  }

  override fun stop() {
    session.stop()
  }

  override fun onAddTorrent(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
    val bencode = torrentHandle.torrentFile()?.bencode() ?: return
    val hash = torrentHandle.infoHash().toHex()
    if (torrents.contains(hash)) {
      return
    }

    val torrent = File(dir, "$hash.torrent")
    if (!torrent.exists()) {
      try {
        val fos = FileOutputStream(torrent)
        fos.write(bencode)
        fos.close()

        torrents[hash] = torrent.absolutePath
        listener.onTorrentSaved(torrentHandle.infoHash().toHex(), torrent.absolutePath)
      } catch (e: Exception) {
        Logging.error(TAG, "onAddTorrent fail to save torrent: ${e.localizedMessage}")
      }
    }
  }

  override fun onTorrentStats(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
    // onAddTorrent may be called without torrentFile,
    // and never get called when change to downloading state,
    // so we check every time when get stats.
    onAddTorrent(torrentHandle, torrentSessionStatus)

    listener.onDownloadStatus(
      torrentHandle.status().infoHash().toHex(),
      torrentSessionStatus.toDownloadStatus()
    )
  }

  override fun onMetadataFailed(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onMetadataReceived(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onPieceFinished(
    torrentHandle: TorrentHandle, torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onTorrentDeleteFailed(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onTorrentDeleted(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onTorrentError(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onTorrentFinished(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onTorrentPaused(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onTorrentRemoved(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  override fun onTorrentResumed(
    torrentHandle: TorrentHandle,
    torrentSessionStatus: TorrentSessionStatus
  ) {
  }

  companion object {
    private const val TAG = "BtDownloader"
  }
}
