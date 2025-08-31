package app.wetube.manage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import app.wetube.R
import app.wetube.core.info
import app.wetube.core.setupTheme
import app.wetube.manage.db.VidDB
import app.wetube.page.dialog.NewVidDialog


class New_vid : Activity() {
    private val db by lazy { VidDB(this) }
    var nv : Int = 0
    var dialogCon : NewVidDialog? = null


    @SuppressLint("SetTextI18n", "MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTheme(true)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        actionBar?.setDisplayShowTitleEnabled(false)
        val a = intent.action
        val type = intent.type
        val b = Bundle()
        if (Intent.ACTION_SEND == a && type != null) {
            b.putString("id",intent.getStringExtra(Intent.EXTRA_TEXT).toString().ifEmpty { "" })

        } else if (intent.getStringExtra("vid")?.isNotEmpty() == true) {
            b.putString("id", intent.getStringExtra("vid") ?: "")
        }
        val bs= NewVidDialog.newB(b.getString("id").toString()) {
            finish()
        }
        dialogCon = NewVidDialog.new(this, bs)
        dialogCon?.show()
        dialogCon?.showBackButton {
            finish()
        }
    }


    override fun onStart() {
        super.onStart()

    }

    override fun onDestroy() {
        super.onDestroy()
        if(isFinishing){
            dialogCon = null
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getBundle("n")?.let { dialogCon?.onRestoreInstanceState(it) }
    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("n", dialogCon?.onSaveInstanceState())

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

            when (item.itemId) {
                R.id.add_ic -> add(
                    findViewById<EditText>(R.id.editTextText3).text.toString(),
                    findViewById<EditText>(R.id.editTextText4).text.toString()
                )
                android.R.id.home -> finish()
            }

        return super.onOptionsItemSelected(item)
    }


    private fun add(title : String, id : String){
        if (title.isNotEmpty() && id.isNotEmpty()) {
            db.doing {
                db.insert(title, id)
            }

            info(getString(R.string.vid_add))

            Toast.makeText(this, "Saved!", Toast.LENGTH_LONG).show()
            finish()
        } else { Toast.makeText(this, "Cannot saving the video!", Toast.LENGTH_LONG).show() }
    }



}