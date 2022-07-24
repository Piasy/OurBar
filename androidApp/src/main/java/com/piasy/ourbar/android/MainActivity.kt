package com.piasy.ourbar.android

import android.Manifest.permission
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.piasy.kmpp.DownloadStatus
import com.piasy.kmpp.Logging
import com.piasy.kmpp.initializeMarsXLog
import com.piasy.ourbar.DownloadManager
import com.piasy.ourbar.DownloadManagerListener
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity(), DownloadManagerListener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    checkPermissionWithPermissionCheck()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    onRequestPermissionsResult(requestCode, grantResults)
  }

  @OptIn(DelicateCoroutinesApi::class)
  @NeedsPermission(
    permission.WRITE_EXTERNAL_STORAGE,
  )
  fun checkPermission() {
    initializeMarsXLog(
      applicationContext,
      "${applicationContext.getExternalFilesDir(null)?.absolutePath}/log",
      "ourbar",
      true
    )

//    Thread {
//      val fetcher = FilmInfoFetcher()
//      println(fetcher.fetch("https://movie.douban.com/subject/25921812/"))
//    }.start()

    val downloadManager = DownloadManager(
      applicationContext,
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
      CoroutineScope(newSingleThreadContext("OBWorker")),
      OkHttp,
      this
    )

    downloadManager.add(
      "magnet:?xt=urn:btih:66ed614c637d976709531019a383c39241ff349c&dn=驴得水.Mr.Donkey.2016.Special.1080p.WEBRip.x264.AAC[SeeHD特别版]&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=udp://open.stealth.si:80/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker1.bt.moack.co.kr:80/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.pomf.se:80/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.lelux.fi:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://tracker.altrosky.nl:6969/announce&tr=udp://tracker.0x.tf:6969/announce&tr=udp://mts.tvbit.co:6969/announce&tr=udp://movies.zsw.ca:6969/announce&tr=http://tracker.bt4g.com:2095/announce",
      "https://movie.douban.com/subject/25921812/"
    )
  }

  override fun onDownloadStatus(status: HashMap<String, DownloadStatus>) {
    Logging.info("XXPXX", "onDownloadStatus $status")
  }
}
