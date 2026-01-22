package com.jnd.jules.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val customColor: ColorFamily,
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        customColor = ColorFamily(
            color = Color.Unspecified,
            onColor = Color.Unspecified,
            colorContainer = Color.Unspecified,
            onColorContainer = Color.Unspecified
        )
    )
}
