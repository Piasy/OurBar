package com.piasy.kmpp

import kotlin.math.min

/**
 * Created by Piasy{github.com/Piasy} on 2019-11-19.
 */
object Logging {
  fun info(
    tag: String,
    content: String
  ) {
    // MarsXLog limit single log length to 4096
    if (content.length < 4000) {
      LoggingImpl.info(tag, content)
    } else {
      var write = 0
      while (write < content.length) {
        val endIndex = min(content.length, write + 4000)
        LoggingImpl.info(tag, content.substring(write, endIndex))
        write = endIndex
      }
    }
  }

  fun error(
    tag: String,
    content: String
  ) {
    // MarsXLog limit single log length to 4096
    if (content.length < 4000) {
      LoggingImpl.error(tag, content)
    } else {
      var write = 0
      while (write < content.length) {
        val endIndex = min(content.length, write + 4000)
        LoggingImpl.error(tag, content.substring(write, endIndex))
        write = endIndex
      }
    }
  }
}

expect object LoggingImpl {
  fun info(
    tag: String,
    content: String
  )

  fun error(
    tag: String,
    content: String
  )
}
