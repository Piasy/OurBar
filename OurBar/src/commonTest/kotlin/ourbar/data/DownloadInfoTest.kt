package ourbar.data

import com.piasy.ourbar.data.DownloadInfo
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/13.
 */
internal class DownloadInfoTest {

  @Test
  fun createFromMagnet() {
    val magnet =
      "magnet:?xt=urn:btih:f2266c7f3a32480613a6de403280497fe8e50f7c&dn=西部世界第四季第2集.2022.HD1080P.AAC.H264.CHS-ENG.BTSJ5&tr=udp://tracker.opentrackr.org:1337/announce&tr=udp://open.tracker.cl:1337/announce&tr=udp://9.rarbg.com:2810/announce&tr=udp://tracker.openbittorrent.com:6969/announce&tr=http://tracker.openbittorrent.com:80/announce&tr=udp://opentracker.i2p.rocks:6969/announce&tr=https://opentracker.i2p.rocks:443/announce&tr=udp://www.torrent.eu.org:451/announce&tr=udp://tracker.torrent.eu.org:451/announce&tr=udp://open.stealth.si:80/announce&tr=udp://ipv4.tracker.harry.lu:80/announce&tr=udp://exodus.desync.com:6969/announce&tr=udp://tracker.tiny-vps.com:6969/announce&tr=udp://tracker.moeking.me:6969/announce&tr=udp://tracker.dler.org:6969/announce&tr=udp://explodie.org:6969/announce&tr=udp://bt.oiyo.tk:6969/announce&tr=https://tracker.nanoha.org:443/announce&tr=https://tracker.lilithraws.org:443/announce&tr=https://tracker.lelux.fi:443/announce&tr=http://tracker.bt4g.com:2095/announce"
    val info = DownloadInfo.create(magnet)
    assertEquals("f2266c7f3a32480613a6de403280497fe8e50f7c", info?.hash)
  }
}
