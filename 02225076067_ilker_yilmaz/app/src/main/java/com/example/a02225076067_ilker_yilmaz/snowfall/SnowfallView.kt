package com.example.a02225076067_ilker_yilmaz.snowfall

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class SnowfallView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val snowflakes = mutableListOf<Snowflake>() // Kar taneleri listesi
    private val paint = Paint().apply {
        color = Color.WHITE // Kar tanelerinin rengi
        style = Paint.Style.FILL
    }
    private val random = Random

    init {
        // Kar tanelerini oluştur
        repeat(100) { // 100 kar tanesi
            snowflakes.add(
                Snowflake(
                    x = random.nextFloat() * width, // Genişlik içinde rastgele bir yer
                    y = random.nextFloat() * height, // Yükseklik içinde rastgele bir yer
                    size = random.nextFloat() * 8 + 2, // 5-15 dp arası boyut
                    speed = random.nextFloat() * 4 + 1 // 2-7 dp/sn hız
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Her kar tanesini çiz
        snowflakes.forEach { snowflake ->
            canvas.drawCircle(snowflake.x, snowflake.y, snowflake.size, paint)

            // Kar tanesinin düşme hareketi
            snowflake.y += snowflake.speed
            if (snowflake.y > height) { // Ekranı geçtiyse tekrar yukarıdan başlat
                snowflake.y = 0f
                snowflake.x = random.nextFloat() * width
            }
        }

        // Animasyonu devam ettir
        invalidate()
    }
}
