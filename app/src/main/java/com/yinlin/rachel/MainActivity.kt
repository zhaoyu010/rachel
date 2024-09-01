package com.yinlin.rachel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yinlin.rachel.databinding.ActivityMainBinding
import com.yinlin.rachel.fragment.FragmentImportMod
import com.yinlin.rachel.model.RachelPages
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class MainActivity : AppCompatActivity() {
    private lateinit var v: ActivityMainBinding
    private lateinit var pages: RachelPages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        v = ActivityMainBinding.inflate(layoutInflater)
        val view = v.root
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        pages = RachelPages(this, v.bbl, arrayOf(
            RachelPages.msg, RachelPages.res, RachelPages.music, RachelPages.group, RachelPages.me
        ), RachelPages.msg, R.id.frame, R.id.frame_child)

        detectImportMod(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        detectImportMod(intent)
    }

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))

    @Deprecated("Deprecated in Java") @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (pages.goBack()) super.onBackPressed()
    }

    private fun detectImportMod(intent: Intent?) {
        if (intent?.action?.equals(Intent.ACTION_VIEW) == true) {
            intent.data?.apply { pages.gotoSecond(FragmentImportMod(pages, this)) }
        }
    }
}