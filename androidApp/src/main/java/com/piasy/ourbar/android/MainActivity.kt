package com.piasy.ourbar.android

import android.Manifest.permission
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.piasy.kmpp.initializeMarsXLog
import com.piasy.ourbar.DownloadManager
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class MainActivity : AppCompatActivity() {

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

    val downloadManager = DownloadManager(
      applicationContext,
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    )

    downloadManager.add(
      "magnet:?xt=urn:btih:f2266c7f3a32480613a6de403280497fe8e50f7c&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce",
      ""
    )
  }
}
