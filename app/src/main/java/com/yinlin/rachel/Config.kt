package com.yinlin.rachel

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
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

    class CacheKeyMeta(name: String) : Meta<Long>(name, System.currentTimeMillis()), CheckDefault {
        override fun set(value: Long) { kv.encode(name, value) }
        override fun get() = kv.decodeLong(name, defValue!!)
        override fun setDefault() { kv.encode(name, defValue!!) }
        override fun isDefault() = get() == defValue
        fun update() = set(System.currentTimeMillis())
    }

    lateinit var kv: MMKV

    // 登录相关
    val user_id_meta = StringMeta("user_id", "")
    var user_id: String
        get() = user_id_meta.get()
        set(value) { user_id_meta.set(value) }

    val user_pwd_meta = StringMeta("user_pwd", "")
    var user_pwd: String
        get() = user_pwd_meta.get()
        set(value) { user_pwd_meta.set(value) }

    val user_meta = JsonMeta<User>("user", "{}", object : TypeToken<User>(){}.type)
    var user: User
        get() = user_meta.get()
        set(value) { user_meta.set(value) }

    // 登录无关
    val playlist_meta = JsonMeta<IPlaylistMap>("playlist", "{}", object : TypeToken<PlaylistMap>(){}.type)
    var playlist: IPlaylistMap
        get() = playlist_meta.get()
        set(value) { playlist_meta.set(value) }

    val weibo_users_meta = JsonMeta<IWeiboUserMap>("weibo_users", WeiboUser.getDefaultWeiboUsers(), object : TypeToken<WeiboUserMap>(){}.type)
    var weibo_users: IWeiboUserMap
        get() = weibo_users_meta.get()
        set(value) { weibo_users_meta.set(value) }

    // 图片缓存键
    var cache_key_avatar = CacheKeyMeta("cache_key_avatar")
    var cache_key_wall = CacheKeyMeta("cache_key_wall")
}