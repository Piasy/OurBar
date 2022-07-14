package com.piasy.kmpp

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/13.
 */
interface KVStorage {
  fun get(key: String): String?
  fun set(key: String, value: String)
  fun del(key: String)
}

expect object KVStorageFactory {
  fun createKVStorage(context: Any): KVStorage
}
