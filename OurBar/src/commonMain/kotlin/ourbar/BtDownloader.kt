package com.piasy.ourbar

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/9.
 */
interface BtDownloader {
}

expect object BtDownloaderFactory {
  fun createDownloader(): BtDownloader
}
