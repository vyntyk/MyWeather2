package com.home.myweather.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Скрытие экранной клавиатуры.
 */
public final class KeyboardUtils {

    private KeyboardUtils() {}

    public static void hide(Context context, View view) {
        if (context == null || view == null) return;
        view.clearFocus();
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
