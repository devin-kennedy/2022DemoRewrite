//   Copyright 2022 Devin Kennedy
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
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