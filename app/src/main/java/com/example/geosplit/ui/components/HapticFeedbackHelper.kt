package com.example.geosplit.ui.components

import android.view.HapticFeedbackConstants
import android.view.View

object HapticFeedbackHelper {
    fun performLightHaptic(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}
