package com.piasy.kmpp

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * Created by Piasy{github.com/Piasy} on 2022/7/13.
 */
actual object KVStorageFactory {
  actual fun createKVStorage(context: Any): KVStorage {
    return AndroidKVStorage(context as Context)
  }
}

class AndroidKVStorage(context: Context) : KVStorage {
  private val kv: MMKV

  init {
    MMKV.initialize(context)
    kv = MMKV.defaultMMKV()
  }

  override fun get(key: String): String? {
    return kv.decodeString(key)
  }

  override fun set(key: String, value: String) {
    kv.encode(key, value)
  }

  override fun del(key: String) {
    kv.remove(key)
  }
}
