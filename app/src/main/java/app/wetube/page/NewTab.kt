package app.wetube.page

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.wetube.databinding.NewTabBinding
import app.wetube.widget.SearchBar

class NewTab : Fragment(), SearchBar.onSubmit {

    lateinit var bin : NewTabBinding
    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(inflater != null){
            bin = NewTabBinding.inflate(inflater, container, false)
            return bin.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val s = SearchBar(activity)
        bin.searchCon.addView(s.view)
        s.onSubmitListener = this
        bin.s.setOnClickListener {
            nextPage(MySavedVideo(), "saved")
        }
        bin.f.setOnClickListener {
            nextPage(FavCha(), "fc")
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onSubmit(q: CharSequence?) {
        val e = Search.newTab(query = q.toString())
        nextPage(e)
    }
}