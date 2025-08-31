package app.wetube.adapter

import android.content.Context
import android.util.Log
import android.widget.SectionIndexer
import java.util.Locale

open class FastSupportAdapter<T> @JvmOverloads constructor(context: Context, resId:Int = android.R.layout.simple_list_item_1, objects: MutableList<T>, val tvid:Int = android.R.id.text1): android.widget.ArrayAdapter<T>(context,resId,tvid, objects), SectionIndexer {
    var mapIndex: HashMap<String, Int>? = linkedMapOf<String, Int>()
    var sections: Array<String?>? = null
    init {
        for (x in objects.indices) {
            val fruit = objects[x]
            var ch = fruit.toString().substring(0, 1)
            ch = ch.toUpperCase(Locale.US)

            // HashMap will prevent duplicates
            mapIndex?.set(ch, x)
        }

        val sectionLetters = mapIndex?.keys

// create a list from the set to sort
        val sectionList = sectionLetters?.toList()

        Log.d("sectionList", sectionList.toString())
        val sortedSectionList = sectionList?.sorted()

        sections = sortedSectionList?.toTypedArray()
    }

    override fun getSections(): Array<out Any?>? {
        return sections
    }

    override fun getPositionForSection(section: Int): Int {
        Log.d("section", "" + section);
        return mapIndex?.get(sections?.get(section)) ?: 0;
    }

    override fun getSectionForPosition(position: Int): Int {
        Log.d("position", "" + position);
        return 0;
    }
}