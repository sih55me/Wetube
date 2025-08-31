package app.wetube.adapter


import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.PreferenceGroup
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ListView
import androidx.annotation.RequiresApi
import java.util.Collections
import kotlin.math.max



/**
 * An adapter that returns the [Preference] contained in this group.
 * In most cases, this adapter should be the base class for any custom
 * adapters from [Preference.getAdapter].
 *
 *
 * This adapter obeys the
 * [Preference]'s adapter rule (the
 * [Adapter.getView] should be used instead of
 * [Preference.getView] if a [Preference] has an
 * adapter via [Preference.getAdapter]).
 *
 *
 * This adapter also propagates data change/invalidated notifications upward.
 *
 *
 * This adapter does not include this [PreferenceGroup] in the returned
 * adapter, use [PreferenceCategoryAdapter] instead.
 *
 * @see PreferenceCategoryAdapter
 *
 *
 * @hide
 *
 */

class PreferenceGroupAdapter(
    /**
     * The group that we are providing data from.
     */
    private val mPreferenceGroup: PreferenceGroup,
) : BaseAdapter(), OnPreferenceChangeListener {
    /**
     * Maps a position into this adapter -> [Preference]. These
     * [Preference]s don't have to be direct children of this
     * [PreferenceGroup], they can be grand children or younger)
     */
    private var mPreferenceList: MutableList<Preference?>

    /**
     * List of unique Preference and its subclasses' names. This is used to find
     * out how many types of views this adapter can return. Once the count is
     * returned, this cannot be modified (since the ListView only checks the
     * count once--when the adapter is being set). We will not recycle views for
     * Preference subclasses seen after the count has been returned.
     */
    private val mPreferenceLayouts: ArrayList<PreferenceLayout?>

    private var mTempPreferenceLayout: PreferenceLayout =
        PreferenceLayout()

    /**
     * Blocks the mPreferenceClassNames from being changed anymore.
     */
    private var mHasReturnedViewTypeCount = false

    @Volatile
    private var mIsSyncing = false

    private val mHandler = Handler()

    private val mSyncRunnable: Runnable = object : Runnable {
        override fun run() {
            syncMyPreferences()
        }
    }

    private var mHighlightedPosition = -1
    private var mHighlightedDrawable: Drawable? = null

    private class PreferenceLayout :
        Comparable<PreferenceLayout?> {
        internal var resId = 0
        internal var widgetResId = 0
        internal var name: String? = null

        override fun compareTo(other: PreferenceLayout?): Int {
            val compareNames = name?.compareTo(other?.name.toString())?:0
            if (compareNames == 0) {
                if (resId == other?.resId) {
                    if (widgetResId == other.widgetResId) {
                        return 0
                    } else {
                        return widgetResId - other.widgetResId
                    }
                } else {
                    return resId -  (other?.resId ?:0)
                }
            } else {
                return compareNames
            }
        }
    }

    init {
        // If this group gets or loses any children, let us know
        mPreferenceGroup.onPreferenceChangeListener = this

        mPreferenceList = ArrayList<Preference?>()
        mPreferenceLayouts =
            ArrayList<PreferenceLayout?>()

        syncMyPreferences()
    }

    private fun syncMyPreferences() {
        synchronized(this) {
            if (mIsSyncing) {
                return
            }
            mIsSyncing = true
        }

        val newPreferenceList: MutableList<Preference?> =
            ArrayList<Preference?>(mPreferenceList.size)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flattenPreferenceGroup(newPreferenceList, mPreferenceGroup)
        }
        mPreferenceList = newPreferenceList

        notifyDataSetChanged()

        synchronized(this) {
            mIsSyncing = false
            (this as Object).notifyAll()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun flattenPreferenceGroup(
        preferences: MutableList<Preference?>,
        group: PreferenceGroup,
    ) {

        val groupSize = group.getPreferenceCount()
        for (i in 0..<groupSize) {
            val preference = group.getPreference(i)

            preferences.add(preference)

            if (!mHasReturnedViewTypeCount && preference.isRecycleEnabled()) {
                addPreferenceClassName(preference)
            }


            preference.setOnPreferenceChangeListener(this)
        }
    }

    /**
     * Creates a string that includes the preference name, layout id and widget layout id.
     * If a particular preference type uses 2 different resources, they will be treated as
     * different view types.
     */
    private fun createPreferenceLayout(
        preference: Preference,
        `in`: PreferenceLayout?,
    ): PreferenceLayout {
        val pl: PreferenceLayout =
            `in` ?: PreferenceLayout()
        pl.name = preference.javaClass.getName()
        pl.resId = preference.getLayoutResource()
        pl.widgetResId = preference.getWidgetLayoutResource()
        return pl
    }

    private fun addPreferenceClassName(preference: Preference) {
        val pl: PreferenceLayout =
            createPreferenceLayout(preference, null)
        var insertPos =
            Collections.binarySearch<PreferenceLayout?>(
                mPreferenceLayouts,
                pl
            )

        // Only insert if it doesn't exist (when it is negative).
        if (insertPos < 0) {
            // Convert to insert index
            insertPos = insertPos * -1 - 1
            mPreferenceLayouts.add(insertPos, pl)
        }
    }

    override fun getCount(): Int {
        return mPreferenceList.size
    }

    override fun getItem(position: Int): Preference? {
        if (position < 0 || position >= getCount()) return null
        return mPreferenceList.get(position)
    }

    override fun getItemId(position: Int): Long {
        if (position < 0 || position >= getCount()) return ListView.INVALID_ROW_ID
        return this.getItem(position).hashCode().toLong()
    }

    /**
     * @hide
     */
    fun setHighlighted(position: Int) {
        mHighlightedPosition = position
    }

    /**
     * @hide
     */
    fun setHighlightedDrawable(drawable: Drawable?) {
        mHighlightedDrawable = drawable
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        val preference = this.getItem(position)
        // Build a PreferenceLayout to compare with known ones that are cacheable.
        mTempPreferenceLayout = createPreferenceLayout(preference!!, mTempPreferenceLayout)

        // If it's not one of the cached ones, set the convertView to null so that
        // the layout gets re-created by the Preference.
        if (Collections.binarySearch<PreferenceLayout?>(
                mPreferenceLayouts,
                mTempPreferenceLayout
            ) < 0 ||
            (getItemViewType(position) == this.highlightItemViewType)
        ) {
            convertView = null
        }
        var result = preference.getView(convertView, parent)
        if (position == mHighlightedPosition && mHighlightedDrawable != null) {
            val wrapper: ViewGroup = FrameLayout(parent.getContext())
            wrapper.setLayoutParams(sWrapperLayoutParams)
            wrapper.setBackgroundDrawable(mHighlightedDrawable)
            wrapper.addView(result)
            result = wrapper
        }
        return result
    }

    override fun isEnabled(position: Int): Boolean {
        if (position < 0 || position >= getCount()) return true
        return this.getItem(position)!!.isSelectable()
    }

    override fun areAllItemsEnabled(): Boolean {
        // There should always be a preference group, and these groups are always
        // disabled
        return false
    }

    override fun onPreferenceChange(preference: Preference?,n:Any?): Boolean {
        notifyDataSetChanged()
        return true
    }

    fun onPreferenceHierarchyChange(preference: Preference?) {
        mHandler.removeCallbacks(mSyncRunnable)
        mHandler.post(mSyncRunnable)
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    private val highlightItemViewType: Int
        get() = getViewTypeCount() - 1

    override fun getItemViewType(position: Int): Int {
        if (position == mHighlightedPosition) {
            return this.highlightItemViewType
        }

        if (!mHasReturnedViewTypeCount) {
            mHasReturnedViewTypeCount = true
        }

        val preference = this.getItem(position)
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                !preference!!.isRecycleEnabled()
            } else {
                false
            }
        ) {
            return IGNORE_ITEM_VIEW_TYPE
        }
        if(preference == null){
            return IGNORE_ITEM_VIEW_TYPE
        }
        mTempPreferenceLayout = createPreferenceLayout(preference, mTempPreferenceLayout)

        val viewType =
            Collections.binarySearch<PreferenceLayout?>(
                mPreferenceLayouts,
                mTempPreferenceLayout
            )
        return if (viewType < 0) {
            // This is a class that was seen after we returned the count, so
            // don't recycle it.
            IGNORE_ITEM_VIEW_TYPE
        } else {
            viewType
        }
    }

    override fun getViewTypeCount(): Int {
        if (!mHasReturnedViewTypeCount) {
            mHasReturnedViewTypeCount = true
        }

        return max(1.0, mPreferenceLayouts.size.toDouble()).toInt() + 1
    }



    companion object {
        private const val TAG = "PreferenceGroupAdapter"

        private val sWrapperLayoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}