package app.wetube.preference

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import app.wetube.R

class ButtonPreference @JvmOverloads constructor(context: Context, attr: AttributeSet?=null, defStyleAttr: Int=0, defStyleRes: Int=0): Preference(context, attr, defStyleAttr,defStyleRes) {
    var borderlessButton = false
    init {
        val ta = context.obtainStyledAttributes(attr, R.styleable.ButtonPreference);
        borderlessButton = ta.getBoolean(R.styleable.ButtonPreference_borderlessButton, true);
        ta.recycle()
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreateView(parent: ViewGroup?): View? {
        if(parent?.isInEditMode == true){
            return super.onCreateView(parent)
        }
        val e = if(borderlessButton) android.R.attr.borderlessButtonStyle else android.R.attr.buttonStyle
        val button = Button(context,null, e)
        button.id = android.R.id.button1
        return button
    }

    @SuppressLint("MissingSuperCall")
    override fun onBindView(view: View?) {
        if(view?.isInEditMode == true){
            super.onBindView(view)
        }
        val button = view as Button
        button.text = title
        button.setCompoundDrawablesWithIntrinsicBounds(icon,null,null,null)
        button.contentDescription = title
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            button.isSingleLine = isSingleLineTitle
            button.tooltipText = summary

        }
        button.setOnClickListener {
            onPreferenceClickListener?.onPreferenceClick(this)
            if (intent != null) {
                getContext().startActivity(intent)
            }
        }
    }
}