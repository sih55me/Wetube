package app.wetube.page.setup

import android.app.Fragment
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import app.wetube.R


class DummyInfo():Fragment() {
    var laid = 0

    /**
     * only call if [view] exist
     */
    var onViewCreatedListener = fun(v:View){

    }
    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if(laid == 0) {
            super.onCreateView(inflater, container, savedInstanceState)
        }else{
            inflater?.inflate(laid, null)
        }
    }

}