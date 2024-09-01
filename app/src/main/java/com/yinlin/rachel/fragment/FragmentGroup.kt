package com.yinlin.rachel.fragment

import com.yinlin.rachel.databinding.FragmentGroupBinding
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelPages

class FragmentGroup(pages: RachelPages) : RachelFragment<FragmentGroupBinding>(pages) {
    override fun bindingClass() = FragmentGroupBinding::class.java
}