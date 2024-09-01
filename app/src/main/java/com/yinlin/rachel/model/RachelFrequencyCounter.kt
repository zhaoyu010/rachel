package com.yinlin.rachel.model

class RachelFrequencyCounter(count: Int) {
    private var num: Int = 0
    private val count: Int = count.coerceAtLeast(1)

    fun ok(): Boolean {
        ++num
        if (num == count) {
            num = 0
            return true
        }
        return false
    }

    fun reset() { num = 0 }
}