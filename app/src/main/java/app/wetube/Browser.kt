package app.wetube

import android.app.Activity
import android.app.ActivityGroup
import android.app.FragmentManager
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.TabHost
import android.widget.TabWidget
import android.widget.TextView
import app.wetube.databinding.BrowserBinding

class Browser: ActivityGroup(false), FragmentManager.OnBackStackChangedListener {
    private var count = 0
    val b by lazy{ BrowserBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        newTab()
    }
    private var mTabHost: TabHost? = null
    private var mDefaultTab: String? = null
    private var mDefaultTabIndex = -1

    /**
     * Sets the default tab that is the first tab highlighted.
     *
     * @param tag the name of the default tab
     */
    fun setDefaultTab(tag: String?) {
        mDefaultTab = tag
        mDefaultTabIndex = -1
    }

    /**
     * Sets the default tab that is the first tab highlighted.
     *
     * @param index the index of the default tab
     */
    fun setDefaultTab(index: Int) {
        mDefaultTab = null
        mDefaultTabIndex = index
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        ensureTabHost()
        val cur = state.getString("currentTab")
        if (cur != null) {
            mTabHost!!.setCurrentTabByTag(cur)
        }
        if (mTabHost!!.getCurrentTab() < 0) {
            if (mDefaultTab != null) {
                mTabHost!!.setCurrentTabByTag(mDefaultTab)
            } else if (mDefaultTabIndex >= 0) {
                mTabHost!!.setCurrentTab(mDefaultTabIndex)
            }
        }
    }

    override fun onPostCreate(icicle: Bundle?) {
        super.onPostCreate(icicle)

        ensureTabHost()

        if (mTabHost!!.getCurrentTab() == -1) {
            mTabHost!!.setCurrentTab(0)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val currentTabTag = mTabHost!!.getCurrentTabTag()
        if (currentTabTag != null) {
            outState.putString("currentTab", currentTabTag)
        }
    }

    /**
     * Updates the screen state (current list and other views) when the
     * content changes.
     *
     * @see Activity.onContentChanged
     */
    override fun onContentChanged() {
        super.onContentChanged()
        mTabHost = b.tabhost
        mTabHost?.setup(getLocalActivityManager())
        b.back.setOnClickListener {
            try{
                childFA.popBackStack()
            }catch(e: Exception){

            }
        }
        b.newTab.setOnClickListener {
            newTab()
        }
    }

    private fun ensureTabHost() {
        if (mTabHost == null) {
            this.setContentView(b.root)
        }
    }

    override fun onChildTitleChanged(childActivity: Activity?, title: CharSequence?) {
        // Dorky implementation until we can have multiple activities running.
        if (getLocalActivityManager().getCurrentActivity() === childActivity) {
            val tabView = mTabHost!!.getCurrentTabView()
            if (tabView != null && tabView is TextView) {
                tabView.setText(title)
            }
        }
    }

    val childFA : FragmentManager get(){
        return localActivityManager.currentActivity.fragmentManager
    }

    /**
     * Returns the [TabHost] the activity is using to host its tabs.
     *
     * @return the [TabHost] the activity is using to host its tabs.
     */
    val tabHost: TabHost? get() {
        ensureTabHost()
        return mTabHost
    }

    /**
     * Returns the [TabWidget] the activity is using to draw the actual tabs.
     *
     * @return the [TabWidget] the activity is using to draw the actual tabs.
     */
    val tabWidget: TabWidget? get() {
        return mTabHost!!.getTabWidget()
    }

    private fun newTab() {
        tabHost?.apply {
            addTab(
                newTabSpec("${count + 1}").also {
                    it.setContent(Intent(context.applicationContext, ContentBrow::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    })
                    it.setIndicator("New tab")
                }
            )
        }
    }

    override fun onBackStackChanged() {
        try{
            b.back.isEnabled = childFA.backStackEntryCount != 0
        }catch(e: Exception){

        }
    }
}