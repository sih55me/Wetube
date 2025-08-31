package app.wetube.page.setup

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class DummyInfo():Fragment() {
    var laid = 0
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