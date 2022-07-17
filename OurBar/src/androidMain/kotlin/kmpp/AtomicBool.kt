package com.piasy.kmpp

actual class AtomicBool actual constructor(initValue: Boolean) {
  private var value = initValue

  actual fun set(value: Boolean) {
    this.value = value
  }

  actual fun get(): Boolean = value
}
