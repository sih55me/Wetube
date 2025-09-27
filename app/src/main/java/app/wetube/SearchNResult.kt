package app.wetube

import android.app.Activity
import android.app.AlertDialog
import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.TextView
import app.wetube.adapter.HistoryAdapter
import app.wetube.core.info
import app.wetube.core.setupTheme
import app.wetube.core.tryOn
import app.wetube.databinding.SearchLayoutBinding
import app.wetube.manage.db.HistoryDB
import kotlin.getValue

class SearchNResult: ListActivity(), HistoryAdapter.HistoryListener {


    var text = ""
    var isTextEmpty = text.isEmpty()
    private val db by lazy { HistoryDB(this) }
    private val bin by lazy{ SearchLayoutBinding.inflate(layoutInflater)}
    private val ad by lazy { HistoryAdapter(arrayListOf(),this, this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setupTheme()
        setContentView(bin.root)
        setActionBar(bin.toolbar2)
        bin.list.adapter = ad
        bin.textView.setOnEditorActionListener { _, _, _ ->
            onClick(text)
            true
        }
        bin.textView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                isTextEmpty = s.isNullOrEmpty()
                text = (s?:"").toString()
                tryOn{
                    invalidateOptionsMenu()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                
            }

        })

        reload()
    }

    override fun finish() {
        setResult(RESULT_CANCELED)
        super.finish()
    }

    fun reload(){
        ad.clear()
        ad.addAll(db.listAsList())
    }

    override fun onClick(text: CharSequence) {
        val i = Intent()
        i.putExtra(Intent.EXTRA_TEXT, text)
        setResult(RESULT_OK, i)
        super.finish()
    }

    override fun onUp(text: CharSequence) {
        bin.textView.setText(text)
    }

    override fun onDelete(text: CharSequence) {
        AlertDialog.Builder(this).setTitle(text).setMessage("Delete this from history?").setPositiveButton(R.string.del){_,_->
            info(if(db.deleteByName(text.toString()))"Deleted" else "Fail to deleted")
            reload()
        }.setNegativeButton(android.R.string.cancel, null).show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==android.R.id.home){finish()}
        if(item.title == getString(android.R.string.search_go)){
            onClick(text)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu == null)return false
        menu.add(R.string.clear).setVisible(!isTextEmpty).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setIcon(R.drawable.close).setOnMenuItemClickListener {
            bin.textView.setText(null)
            true
        }
        menu.add(android.R.string.search_go)
        return super.onCreateOptionsMenu(menu)
    }
}