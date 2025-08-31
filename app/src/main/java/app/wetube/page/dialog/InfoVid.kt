package app.wetube.page.dialog

import android.app.ActionBar
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import app.wetube.R
import app.wetube.core.tryOn
import app.wetube.databinding.InfoBinding
import app.wetube.window.Paper

class InfoVid(context: Context, private val script : Triple<String, String, String>): Paper(context) {
    val binding by lazy{ InfoBinding.inflate(layoutInflater)}
    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            who.text = script.first
            info.text = script.second
            icon.setImageResource(R.drawable.video)
        }
    }

    override fun setupActionBar(actionBar: ActionBar) {
        actionBar.title = "Post date :"
        actionBar.subtitle = script.third
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tryOn {
            binding.root.parent?.let {
                if (it is ViewGroup) {
                    it.removeView(binding.root)
                }
            }
        }
    }

}