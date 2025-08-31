package app.wetube

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

private const val DEBUG_TAG = "Gestures"

class MyGestureListener : GestureDetector.SimpleOnGestureListener() {
    // You can use GestureDetector.SimpleOnGestureListener for a simpler implementation
    // if you only need to override a few methods.


    var onDown = {

    }

    var onUp = {

    }

    override fun onDown(event: MotionEvent): Boolean {

        Log.d(DEBUG_TAG, "onDown: $event")
        // Must return true here to indicatethat you want to handle the gesture.
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(DEBUG_TAG, "onFling: $e1 $event2 velocityX: $velocityX velocityY: $velocityY")
        // Handle the fling gesture here
        if (velocityY > 0) {
            Log.d(DEBUG_TAG, "Fling Up")
            onUp() // Or a specific onFlingUp()
        } else if (velocityY < 0) {
            Log.d(DEBUG_TAG, "Fling Down")
            onDown() // Or a specific onFlingDown()
        }
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onLongPress: $event")
        // Handle the long press gesture here
    }

    override fun onScroll(
        event1: MotionEvent?, // The first down motion event that started the scroll.
        event2: MotionEvent,  // The move motion event that triggered the current onScroll.
        distanceX: Float,     // The distance along the X axis that has been scrolled since the last call to onScroll.
        distanceY: Float,      // The distance along the Y axis that has been scrolled since the last call to onScroll.
    ): Boolean {
        Log.d(DEBUG_TAG, "onScroll: $event1 $event2 distanceX: $distanceX distanceY: $distanceY")
        // Handle the scroll gesture here

        if (distanceY > 0) {
            // Scrolling Up
            Log.d(DEBUG_TAG, "Scrolled Up")
//            onUp()
        } else if (distanceY < 0) {
            // Scrolling Down
            Log.d(DEBUG_TAG, "Scrolled Down")
            onDown()
        }
        return !true
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onShowPress: $event")
        // Called when the user has performed a down MotionEvent and not yet moved or up.
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapUp: $event")
        // Handle the single tap up gesture here (occurs when the user lifts their finger after a tap)
        return true
    }

    // You can also override other methods from GestureDetector.OnGestureListener
    // like onDoubleTap, onDoubleTapEvent, onSingleTapConfirmed
    // If you need double tap detection, you'll also need to implement
    // GestureDetector.OnDoubleTapListener and set it using detector.setOnDoubleTapListener()
}