package app.wetube


import android.annotation.SuppressLint
import android.app.ListFragment
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

class LiveTV : ListFragment() {
    @get:SuppressLint("MissingInflatedId", "ResourceType")
    var getlangu  = "Everything"
    val lang by lazy { arrayOf("Everything", "Indonesian", "English", "Arabia") }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        val tlID = arrayOf("TVOne", "Metro TV", "KompasTV")
        val tlEN =
            arrayOf("ABC TV", "CNA", "Nat GEO", "Al Jazeera EN", "France 24 EN", "LiveNOW (FOX)")
        val tlAR = arrayOf("Makkah LIVE")
        val ilID = arrayOf("yNKvkPJl-tg", "XKueVSGTk2o", "DOOrIxw5xOw")
        val ilEN = arrayOf(
            "gN0PZCe-kwQ",
            "XWq5kBlakcQ",
            "BJ3Yv572V1A",
            "gCNeDWCI0vo",
            "tkDUSYHoKxE",
            "YDfiTGGPYCk"
        )
        val ilAR = arrayOf("moQtMet7F7w")
        val titlelists = arrayOf(*tlAR, *tlEN, *tlID)
        val idlists = arrayOf(*ilAR, *ilEN, *ilID)
        listAdapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, titlelists)

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
//            when (getlangu) {
//                "Everything" -> {
//                    openVideo(
//                        requireActivity(),
//                        VideoDetail(idlists[position])
//                    )
//                }
//
//                "Indonesian" -> {
//                    openVideo(
//                        requireActivity(),
//                        ilID[position]
//                    )
//                }
//
//                "English" -> {
//                    openVideo(
//                        requireActivity(),
//                        ilEN[position]
//                    )
//                }
//
//                "Arabia" -> {
//                    openVideo(
//                        requireActivity(),
//                        ilAR[position]
//                    )
//                }
//            }
        }
    }











}