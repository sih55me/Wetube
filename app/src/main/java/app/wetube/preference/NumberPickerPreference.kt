package app.wetube.preference

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.preference.DialogPreference
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.NumberPicker
import app.wetube.R


class NumberPickerPreference @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): DialogPreference(context, attrs) {


    var min = 0
    private val change = NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
        inNow = newVal
    }
    var summaryForValue = true
    set(value) {
        internalSet(now, )
        field = value
    }

    //indialog
    private var inNow= 0

    var now
    get() = preferenceManager.getSharedPreferences().getInt(key, min)
    set(value) {
        internalSet(value)
    }

    var max = 1

    init {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference)
        now = ta.getInt(R.styleable.SeekbarPreference_android_progress, 0)
        max = ta.getInt(R.styleable.SeekbarPreference_android_max, 1)
        ta.recycle()
    }

    private fun internalSet(value: Int) {
        //do not change seekbar if true
        summary = if(summaryForValue){
            value.toString()
        }else{
            ""
        }
        if (callChangeListener(value)) {


            persistInt(value)
            notifyDependencyChange(shouldDisableDependents())
            notifyChanged()
        }
    }

    override fun showDialog(state: Bundle?) {
        super.showDialog(state)
    }


    override fun onCreateDialogView(): View {
        return FrameLayout(context).apply {
            addView(NumberPicker(context))
        }
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        if (view is FrameLayout) {
            val s = view.getChildAt(0)
            if(s is NumberPicker) {
                s.apply {
                    setOnValueChangedListener(change)
                    val prmi = this@NumberPickerPreference.min
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        minValue = prmi
                    }
                    maxValue = this@NumberPickerPreference.max

                    value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) now else now - prmi
                }
            }
        }
    }



    override fun onDialogClosed(positiveResult: Boolean) {
        if(positiveResult){
            internalSet(inNow)
        }
        super.onDialogClosed(positiveResult)
    }



    override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
        return a.getInt(index, 10)
    }



    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {
        now = (if (restoreValue) getPersistedInt(now) else defaultValue as Int)
    }






    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        val myState: SavedState = SavedState(superState)

        myState.min = min

        myState.now = now
        myState.max = max
        return myState
    }







    private class SavedState : BaseSavedState {
        var min = 0
        var now = 0
        var max = 1
        constructor(source: Parcel): super(source){
            min = source.readInt()
            now = source.readInt()
            max = source.readInt()
        }

        constructor(superState: Parcelable) : super(superState)



        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.apply{
                writeInt(min)
                writeInt(now)
                writeInt(max)
            }
        }


        companion object CREATOR: Parcelable.Creator<SavedState> {
            override fun createFromParcel(`in`: Parcel): SavedState {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}