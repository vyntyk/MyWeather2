package com.home.myweather.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * Корневой layout вкладки «Карта», который блокирует свайп между вкладками
 * ViewPager2, пока эта страница активна — но НЕ мешает жестам самой карты.
 *
 * ViewPager2 объявлен final (наследовать нельзя), поэтому блокировка сделана
 * через [ViewGroup.requestDisallowInterceptTouchEvent]: на ACTION_DOWN layout
 * запрещает родителю (RecyclerView паджера) перехватывать текущий жест.
 * В результате паджер не пролистывается по свайпу, а все движения пальца
 * (панорама карты) уходят в содержимое. Переключение кнопками нижней
 * навигации работает как раньше (это программный setCurrentItem).
 */
class SwipeLockLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            // Запрещаем ViewPager2 перехватывать жесты в этой странице.
            (parent as? ViewGroup)?.requestDisallowInterceptTouchEvent(true)
        }
        // Возвращаем false — сам gestы не перехватываем, карта работает.
        return super.onInterceptTouchEvent(ev)
    }
}