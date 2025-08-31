package app.wetube.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class VideoSizeImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0 , defStyleRes: Int = 0):
    ImageView(context, attrs, defStyleAttr,defStyleRes) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val sixteenNineHeight = MeasureSpec.makeMeasureSpec(
            MeasureSpec.getSize(widthMeasureSpec) * 9 / 16,
            MeasureSpec.EXACTLY
        )
        super.onMeasure(widthMeasureSpec, sixteenNineHeight)
    }
}