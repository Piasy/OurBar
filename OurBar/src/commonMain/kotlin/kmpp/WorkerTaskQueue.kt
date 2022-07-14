package com.piasy.kmpp

/**
 * Created by Piasy{github.com/Piasy} on 2019-11-30.
 */
interface WorkerTaskQueue<T> {
  fun state(): T

  fun updateState(newState: T): T

  fun execute(task: () -> Unit)

  fun executeAfter(
    millis: Long,
    task: () -> Unit
  )

  fun shutdown()
}

expect object WorkerTaskQueueFactory {
  fun <T> createQueue(state: T): WorkerTaskQueue<T>
}
