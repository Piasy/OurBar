package com.piasy.kmpp

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/23.
 */
actual object KVStorageFactory {
  actual fun createKVStorage(context: Any): KVStorage {
    return IOSKVStorage()
  }
}

class IOSKVStorage() : KVStorage {
  override fun get(key: String): String? {
    return null
  }

  override fun set(key: String, value: String) {
  }

  override fun del(key: String) {
  }
}
