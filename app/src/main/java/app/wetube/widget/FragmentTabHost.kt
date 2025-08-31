package app.wetube.widget

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TabHost
import android.widget.TabHost.OnTabChangeListener
import android.widget.TabWidget


class FragmentTabHost : TabHost, OnTabChangeListener {
    private val mTabs = ArrayList<TabInfo>()

    private var mRealTabContent: FrameLayout? = null
    private var mContext: Context? = null
    private var mFragmentManager: FragmentManager? = null
    var mContainerId = 0
    private var mOnTabChangeListener: OnTabChangeListener? = null
    private var mLastTab: TabInfo? = null
    private var mAttached = false

    internal class TabInfo(val tag: String, val clss: Class<*>, val args: Bundle?) {
        var fragment: Fragment? = null
    }

    internal class DummyTabFactory(private val mContext: Context?) : TabContentFactory {
        override fun createTabContent(tag: String?): View {
            val v = View(mContext)
            v.setMinimumWidth(0)
            v.setMinimumHeight(0)
            return v
        }
    }

    internal class SavedState : BaseSavedState {
        var curTab: String? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(`in`: Parcel) : super(`in`) {
            curTab = `in`.readString()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(curTab)
        }

        override fun toString(): String {
            return ("FragmentTabHost.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " curTab=" + curTab + "}")
        }

        companion object  CREATOR: Parcelable.Creator<FragmentTabHost.SavedState?>{


            override fun createFromParcel(`in`: Parcel): FragmentTabHost.SavedState {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<FragmentTabHost.SavedState?> {
                return arrayOfNulls<FragmentTabHost.SavedState>(size)
            }

        }
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    constructor(context: Context) : super(context, null) {
        // Note that we call through to the version that takes an AttributeSet,
        // because the simple Context construct can result in a broken object!
        initFragmentTabHost(context, null)
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initFragmentTabHost(context, attrs)
    }

    private fun initFragmentTabHost(context: Context, attrs: AttributeSet?) {


        super.setOnTabChangedListener(this)
    }

    private fun ensureHierarchy(context: Context) {
        // If owner hasn't made its own view hierarchy, then as a convenience
        // we will construct a standard one here.
        if (findViewById<View?>(R.id.tabs) == null) {
            val ll = LinearLayout(context)
            ll.orientation = LinearLayout.VERTICAL
            addView(
                ll, LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            )

            val tw = TabWidget(context)
            tw.setId(R.id.tabs)
            tw.setOrientation(TabWidget.HORIZONTAL)
            ll.addView(
                tw, LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT, 0f
                )
            )

            var fl = FrameLayout(context)
            fl.setId(R.id.tabcontent)
            ll.addView(fl, LinearLayout.LayoutParams(0, 0, 0f))

            fl = FrameLayout(context)
            mRealTabContent = fl
            mRealTabContent!!.setId(mContainerId)
            ll.addView(
                fl, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                )
            )
        }
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    override fun setup() {
        if(context is Activity){
            (context as Activity).let {
                setup(it, it.fragmentManager)
            }
        }else if(isInEditMode){
            //do nothin
        }else{
            throw IllegalStateException("ACTIVITY ONLY BRO\nu using $context")
        }
    }

    /**
     * Set up the FragmentTabHost to use the given FragmentManager
     *
     */

    fun setup(context: Context, manager: FragmentManager) {
        ensureHierarchy(context) // Ensure views required by super.setup()
        super.setup()
        mContext = context
        mFragmentManager = manager
        ensureContent()
    }

    /**
     * Set up the FragmentTabHost to use the given FragmentManager
     *
     */

    fun setup(
        context: Context, manager: FragmentManager,
        containerId: Int,
    ) {
        ensureHierarchy(context) // Ensure views required by super.setup()
        super.setup()
        mContext = context
        mFragmentManager = manager
        mContainerId = containerId
        ensureContent()
        mRealTabContent!!.setId(containerId)

        // We must have an ID to be able to save/restore our state.  If
        // the owner hasn't set one at this point, we will set it ourselves.
        if (getId() == NO_ID) {
            setId(R.id.tabhost)
        }
    }

    private fun ensureContent() {
        if (mRealTabContent == null) {
            mRealTabContent = findViewById<View?>(mContainerId) as FrameLayout?
            checkNotNull(mRealTabContent) { "No tab content FrameLayout found for id $mContainerId" }
        }
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    override fun setOnTabChangedListener(l: OnTabChangeListener?) {
        mOnTabChangeListener = l
    }


    fun addTab(
        tabSpec: TabSpec, clss: Class<*>,
        args: Bundle?,
    ) {
        tabSpec.setContent(DummyTabFactory(mContext))

        val tag = tabSpec.getTag()
        val info = TabInfo(tag, clss, args)

        if (mAttached) {
            // If we are already attached to the window, then check to make
            // sure this tab's fragment is inactive if it exists.  This shouldn't
            // normally happen.
            info.fragment = mFragmentManager!!.findFragmentByTag(tag)
            if (info.fragment != null && !info.fragment!!.isDetached) {
                val ft = mFragmentManager!!.beginTransaction()
                ft.detach(info.fragment!!)
                ft.commit()
            }
        }

        mTabs.add(info)
        addTab(tabSpec)
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val currentTag = getCurrentTabTag()

        // Go through all tabs and make sure their fragments match
        // the correct state.
        var ft: FragmentTransaction? = null
        var i = 0
        val count = mTabs.size
        while (i < count) {
            val tab = mTabs.get(i)
            tab.fragment = mFragmentManager!!.findFragmentByTag(tab.tag)
            if (tab.fragment != null && !tab.fragment!!.isDetached()) {
                if (tab.tag == currentTag) {
                    // The fragment for this tab is already there and
                    // active, and it is what we really want to have
                    // as the current tab.  Nothing to do.
                    mLastTab = tab
                } else {
                    // This fragment was restored in the active state,
                    // but is not the current tab.  Deactivate it.
                    if (ft == null) {
                        ft = mFragmentManager!!.beginTransaction()
                    }
                    ft?.detach(tab.fragment!!)
                }
            }
            i++
        }

        // We are now ready to go.  Make sure we are switched to the
        // correct tab.
        mAttached = true
        ft = doTabChanged(currentTag, ft)
        if (ft != null) {
            ft.commit()
            mFragmentManager!!.executePendingTransactions()
        }
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mAttached = false
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.curTab = getCurrentTabTag()
        return ss
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    override fun onRestoreInstanceState(@SuppressLint("UnknownNullness") state: Parcelable?) {
        if (state !is FragmentTabHost.SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss = state
        super.onRestoreInstanceState(ss.getSuperState())
        setCurrentTabByTag(ss.curTab)
    }

    @Deprecated(
        """Use
      <a href="https://developer.android.com/guide/navigation/navigation-swipe-view ">
       TabLayout and ViewPager</a> instead."""
    )
    override fun onTabChanged(tabId: String?) {
        if (mAttached) {
            val ft = doTabChanged(tabId, null)
            if (ft != null) {
                ft.commit()
            }
        }
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener!!.onTabChanged(tabId)
        }
    }

    private fun doTabChanged(
        tag: String?,
        ft: FragmentTransaction?,
    ): FragmentTransaction? {
        var ft = ft
        val newTab = getTabInfoForTag(tag)
        if (mLastTab != newTab) {
            if (ft == null) {
                ft = mFragmentManager!!.beginTransaction()
            }

            if (mLastTab != null) {
                if (mLastTab!!.fragment != null) {
                    ft?.detach(mLastTab!!.fragment!!)
                }
            }

            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = Fragment.instantiate(
                        mContext, newTab.clss.getName()
                    )
                    newTab.fragment!!.setArguments(newTab.args)
                    ft?.add(mContainerId, newTab.fragment!!, newTab.tag)
                } else {
                    ft?.attach(newTab.fragment!!)
                }
            }

            mLastTab = newTab
        }

        return ft
    }

    private fun getTabInfoForTag(tabId: String?): TabInfo? {
        var i = 0
        val count = mTabs.size
        while (i < count) {
            val tab = mTabs.get(i)
            if (tab.tag == tabId) {
                return tab
            }
            i++
        }
        return null
    }
}