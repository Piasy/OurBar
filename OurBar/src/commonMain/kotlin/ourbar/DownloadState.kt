package com.piasy.ourbar

import com.piasy.kmpp.DownloadStatus

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/14.
 */
data class DownloadState(
  val status: HashMap<String, DownloadStatus>,
) {
  fun updateStatus(hash: String, status: DownloadStatus): DownloadState {
    val state = deepCopy()
    state.status[hash] = status
    return state
  }

  private fun deepCopy(): DownloadState {
    return DownloadState(HashMap(status))
  }

  companion object {
    fun initState(): DownloadState {
      return DownloadState(HashMap())
    }
  }
}
