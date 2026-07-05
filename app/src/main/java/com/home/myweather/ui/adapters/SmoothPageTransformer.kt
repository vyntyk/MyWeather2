package com.home.myweather.ui.adapters

import android.view.View
import androidx.viewpager2.widget.ViewPager2

/**
 * PageTransformer для плавных анимаций переключения страниц.
 * Добавляет эффекты масштабирования и прозрачности для более плавного визуального опыта.
 */
class SmoothPageTransformer : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        when {
            position < -1 -> {
                // Слева за экраном
                page.alpha = 0f
            }
            position <= 0 -> {
                // От -1 до 0: скользим влево
                page.alpha = 1f
                page.translationX = 0f
                page.scaleX = 1f
                page.scaleY = 1f
            }
            position <= 1 -> {
                // От 0 до 1: скользим вправо
                page.alpha = 1f
                page.translationX = 0f
                page.scaleX = 1f
                page.scaleY = 1f
            }
            else -> {
                // Справа за экраном
                page.alpha = 0f
            }
        }
    }
}
