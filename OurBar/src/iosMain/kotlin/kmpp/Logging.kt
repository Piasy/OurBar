package com.piasy.kmpp

import platform.Foundation.NSLog

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/23.
 */

actual object LoggingImpl {
  actual fun info(
    tag: String,
    content: String
  ) {
    NSLog("$tag $content")
  }

  actual fun error(
    tag: String,
    content: String
  ) {
    NSLog("$tag $content")
  }
}
