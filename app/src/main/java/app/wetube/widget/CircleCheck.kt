package app.wetube.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import android.widget.RadioButton

class CircleCheck @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.radioButtonStyle ,
    defStyleRes: Int = 0) :
    RadioButton(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {

        val whenToggle : CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

        }
    override fun toggle() {
        isChecked = !isChecked
        whenToggle.onCheckedChanged(this, isChecked)
    }


}