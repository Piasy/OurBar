package com.piasy.kmpp

import android.content.Context
import com.tencent.mars.xlog.Log
import com.tencent.mars.xlog.Xlog

/**
 * Created by Piasy{github.com/Piasy} on 2019-11-19.
 */

private var sInitialized = false

fun initializeMarsXLog(
  context: Context,
  logDir: String,
  namePrefix: String,
  logToConsole: Boolean
) {
  if (sInitialized) {
    return
  }

  System.loadLibrary("marsxlog")

  // this is necessary, or may cash for SIGBUS
  val cachePath = context.filesDir.toString() + "/xlog"
  Xlog.appenderOpen(Xlog.LEVEL_INFO, Xlog.AppednerModeAsync, cachePath, logDir, namePrefix, 0, "")
  Xlog.setConsoleLogOpen(logToConsole)
  Log.setLogImp(Xlog())

  sInitialized = true
}

actual object LoggingImpl {
  @JvmStatic
  actual fun info(
    tag: String,
    content: String
  ) {
    Log.i(tag, "${Thread.currentThread().name} # $content")
  }

  @JvmStatic
  actual fun error(
    tag: String,
    content: String
  ) {
    Log.e(tag, "${Thread.currentThread().name} # $content")
    Log.appenderFlush(true)
  }
}
