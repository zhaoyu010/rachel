package com.yinlin.rachel

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.data.User
import com.yinlin.rachel.data.WeiboUser
import java.lang.reflect.Type


object Config {
    abstract class Meta<T>(protected val name: String, protected val defValue: T?) {
        abstract fun set(value: T)
        abstract fun get(): T
        abstract fun setDefault()
    }

    interface CheckDefault {
        fun isDefault(): Boolean
    }

    class IntMeta(name: String, defValue: Int) : Meta<Int>(name, defValue), CheckDefault {
        override fun set(value: Int) { kv.encode(name, value) }
        override fun get() = kv.decodeInt(name, defValue!!)
        override fun setDefault() { kv.encode(name, defValue!!) }
        override fun isDefault() = get() == defValue
    }

    class StringMeta(name: String, defValue: String) : Meta<String>(name, defValue), CheckDefault {
        override fun set(value: String) { kv.encode(name, value) }
        override fun get() = kv.decodeString(name, defValue)!!
        override fun setDefault() { kv.encode(name, defValue) }
        override fun isDefault() = get() == defValue
    }

    class JsonMeta<U>(name: String, private val defJson: String, private val type: Type) : Meta<U>(name, null) {
        override fun set(value: U) { kv.encode(name, Gson().toJson(value)) }
        override fun get(): U = Gson().fromJson(kv.decodeString(name, defJson), type)
        override fun setDefault() { kv.encode(name, defJson) }
    }

    lateinit var kv: MMKV

    // 登录相关
    val user_id = StringMeta("user_id", "")
    val user_pwd = StringMeta("user_pwd", "")
    val user = JsonMeta<User>("user", "{}", object : TypeToken<User>(){}.type)

    // 登录无关
    val playlist = JsonMeta<MutableMap<String, Playlist>>("playlist", "{}", object : TypeToken<MutableMap<String, Playlist>>(){}.type)
    val weibo_users = JsonMeta<MutableMap<String, WeiboUser>>("weibo_users", WeiboUser.getDefaultWeiboUsers(), object : TypeToken<MutableMap<String, WeiboUser>>(){}.type)
}