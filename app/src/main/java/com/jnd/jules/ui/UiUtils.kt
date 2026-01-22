package com.jnd.jules.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.jnd.jules.model.SessionState

fun getStatusColor(state: SessionState?): Color {
    return when (state) {
        SessionState.COMPLETED -> Color(0xFF4CAF50) // Green
        SessionState.FAILED -> Color(0xFFF44336) // Red
        SessionState.IN_PROGRESS, SessionState.PLANNING -> Color(0xFF2196F3) // Blue
        SessionState.AWAITING_USER_FEEDBACK, SessionState.AWAITING_PLAN_APPROVAL -> Color(0xFFFF9800) // Orange
        else -> Color.Gray
    }
}
