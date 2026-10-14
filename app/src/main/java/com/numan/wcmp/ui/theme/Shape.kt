package com.numan.wcmp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// WCMP Premium V2 Shapes
val WcmpShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(12.dp), // Small controls
    medium = RoundedCornerShape(16.dp), // Buttons
    large = RoundedCornerShape(20.dp), // Cards
    extraLarge = RoundedCornerShape(24.dp) // Large hero surfaces / Bottom sheets
)
