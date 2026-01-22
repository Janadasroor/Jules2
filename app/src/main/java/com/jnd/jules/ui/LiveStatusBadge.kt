package com.jnd.jules.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.jnd.jules.model.SessionState

@Composable
fun LiveStatusBadge(state: SessionState) {
    val isLive = state in listOf(SessionState.PLANNING, SessionState.IN_PROGRESS, SessionState.QUEUED)
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by if (isLive) {
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = getStatusColor(state),
        contentColor = Color.White,
        modifier = Modifier.graphicsLayer { this.alpha = if (isLive) alpha else 1f }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = state.name,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
