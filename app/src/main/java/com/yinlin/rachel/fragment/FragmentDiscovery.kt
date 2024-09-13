package com.yinlin.rachel.fragment

import com.yinlin.rachel.databinding.FragmentDiscoveryBinding
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelPages

class FragmentDiscovery(pages: RachelPages) : RachelFragment<FragmentDiscoveryBinding>(pages) {
    override fun bindingClass() = FragmentDiscoveryBinding::class.java
}