package com.piasy.ourbar

import com.piasy.kmpp.DownloadStatus
import com.piasy.kmpp.Logging
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/23.
 */
class OurBarTest : DownloadManagerListener {

  @OptIn(ExperimentalCoroutinesApi::class) fun test() {
    val downloadManager = DownloadManager(
      0, "",
      CoroutineScope(newSingleThreadContext("OBWorker")),
      Darwin,
      this
    )

    downloadManager.add(
      "magnet:?xt=urn:btih:66ed614c637d976709531019a383c39241ff349c&dn=驴得水.Mr.Donkey.2016.Special.1080p.WEBRip.x264.AAC[SeeHD特别版]&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=udp://open.stealth.si:80/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker1.bt.moack.co.kr:80/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.pomf.se:80/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.lelux.fi:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://tracker.altrosky.nl:6969/announce&tr=udp://tracker.0x.tf:6969/announce&tr=udp://mts.tvbit.co:6969/announce&tr=udp://movies.zsw.ca:6969/announce&tr=http://tracker.bt4g.com:2095/announce",
      "https://movie.douban.com/subject/25921812/"
    )
  }

  override fun onDownloadStatus(status: HashMap<String, DownloadStatus>) {
    Logging.info("OurBarTest", "onDownloadStatus $status")
  }
}
