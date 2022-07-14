package com.piasy.kmpp

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Created by Piasy{github.com/Piasy} on 2019-11-30.
 */
actual object WorkerTaskQueueFactory {
  actual fun <T> createQueue(state: T): WorkerTaskQueue<T> {
    return AndroidWorkerTaskQueue(state)
  }
}

class AndroidWorkerTaskQueue<T>(private var state: T) : WorkerTaskQueue<T> {
  private val executor = Executors.newSingleThreadScheduledExecutor()

  override fun state(): T = state

  override fun updateState(newState: T): T {
    this.state = newState
    return newState
  }

  override fun execute(task: () -> Unit) {
    executor.execute(task)
  }

  override fun executeAfter(
    millis: Long,
    task: () -> Unit
  ) {
    executor.schedule(task, millis, MILLISECONDS)
  }

  override fun shutdown() {
    executor.shutdownNow()
  }
}
