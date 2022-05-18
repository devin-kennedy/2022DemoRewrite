package com.dkennedy.a2022demorewrite

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class BoundingBoxOverlay(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    var boundingBox = RectF(0f, 0f, 0f, 0f)
    var results = listOf<RectF>()

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (results.size >= 1){
            for (box in results) {
                canvas?.drawRoundRect(box, 10f, 10f, paint)
            }
        }
        canvas?.drawRoundRect(boundingBox, 10f, 10f, paint)
    }
}