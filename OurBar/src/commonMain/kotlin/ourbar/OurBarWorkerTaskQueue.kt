package com.piasy.ourbar

import com.piasy.kmpp.AtomicBool
import com.piasy.kmpp.Logging
import com.piasy.kmpp.WorkerTaskQueueFactory

/**
 * Created by Piasy{github.com/Piasy} on 2019-12-02.
 */
class OurBarWorkerTaskQueue {
  private val realQueue = WorkerTaskQueueFactory.createQueue(HashMap<String, Any>())
  private var active = AtomicBool(true)

  fun downloadState(): DownloadState = realQueue.state()[KEY_DOWNLOAD_STATE]!! as DownloadState

  fun updateDownloadState(downloadState: DownloadState): DownloadState {
    val state = HashMap(realQueue.state())
    state[KEY_DOWNLOAD_STATE] = downloadState
    realQueue.updateState(state)
    return downloadState
  }

  fun execute(task: () -> Unit) {
    if (active.get()) {
      realQueue.execute(errorAwareTask(task))
    }
  }

  fun executeAfter(
    millis: Long,
    task: () -> Unit
  ) {
    if (active.get()) {
      realQueue.executeAfter(millis, errorAwareTask(task))
    }
  }

  fun shutdown() {
    Logging.info(TAG, "shutdown")
    if (active.get()) {
      active.set(false)
      realQueue.shutdown()
    }
  }

  private fun errorAwareTask(task: () -> Unit): () -> Unit {
    return {
      try {
        task()
      } catch (t: Throwable) {
        Logging.error(TAG, "execute error: ${t.stackTraceToString()}")
      }
    }
  }

  companion object {
    private const val TAG = "WorkerTaskQueue"

    private const val KEY_DOWNLOAD_STATE = "DOWNLOAD_STATE"
  }
}
