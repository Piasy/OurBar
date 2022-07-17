package com.piasy.kmpp

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/14.
 */
expect class AtomicBool(initValue: Boolean) {
  fun set(value: Boolean)
  fun get(): Boolean
}
