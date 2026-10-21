package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object SampleFoodHelper {
    fun createSampleMealBitmap(mealName: String): Bitmap {
        val bitmap = Bitmap.createBitmap(480, 480, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Plate base
        paint.color = Color.parseColor("#E8F5E9")
        canvas.drawCircle(240f, 240f, 230f, paint)

        paint.color = Color.parseColor("#FFFFFF")
        canvas.drawCircle(240f, 240f, 205f, paint)

        when {
            mealName.contains("Salmon", ignoreCase = true) -> {
                // Quinoa & Rice base
                paint.color = Color.parseColor("#FFE0B2")
                canvas.drawCircle(240f, 240f, 150f, paint)

                // Salmon fillet
                paint.color = Color.parseColor("#FF7043")
                canvas.drawRoundRect(RectF(140f, 170f, 340f, 310f), 24f, 24f, paint)

                // Steamed greens & avocado
                paint.color = Color.parseColor("#43A047")
                canvas.drawCircle(180f, 140f, 40f, paint)
                canvas.drawCircle(300f, 150f, 35f, paint)
                canvas.drawCircle(320f, 280f, 30f, paint)
            }
            mealName.contains("Salad", ignoreCase = true) -> {
                // Mixed salad greens
                paint.color = Color.parseColor("#4CAF50")
                canvas.drawCircle(240f, 240f, 160f, paint)

                // Tomatoes
                paint.color = Color.parseColor("#E53935")
                canvas.drawCircle(180f, 190f, 30f, paint)
                canvas.drawCircle(280f, 210f, 28f, paint)
                canvas.drawCircle(220f, 300f, 26f, paint)

                // Avocado
                paint.color = Color.parseColor("#81C784")
                canvas.drawCircle(290f, 280f, 32f, paint)
                canvas.drawCircle(190f, 270f, 30f, paint)
            }
            else -> {
                // Avocado Toast & Eggs
                paint.color = Color.parseColor("#D7CCC8")
                canvas.drawRoundRect(RectF(120f, 130f, 360f, 350f), 32f, 32f, paint)

                // Mashed Avocado
                paint.color = Color.parseColor("#66BB6A")
                canvas.drawRoundRect(RectF(140f, 150f, 340f, 330f), 24f, 24f, paint)

                // Poached egg whites & yolk
                paint.color = Color.parseColor("#FFFDE7")
                canvas.drawCircle(200f, 210f, 45f, paint)
                paint.color = Color.parseColor("#FFA000")
                canvas.drawCircle(200f, 210f, 22f, paint)

                paint.color = Color.parseColor("#FFFDE7")
                canvas.drawCircle(280f, 260f, 45f, paint)
                paint.color = Color.parseColor("#FFA000")
                canvas.drawCircle(280f, 260f, 22f, paint)
            }
        }

        return bitmap
    }
}
