package com.numan.wcmp.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val WcmpIcons.Delete: ImageVector
    get() {
        return ImageVector.Builder(
            name = "Delete", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(3f, 6f); lineTo(5f, 6f); lineTo(21f, 6f)
                moveTo(19f, 6f); lineTo(19f, 20f); curveTo(19f, 21.1f, 18.1f, 22f, 17f, 22f); lineTo(7f, 22f); curveTo(5.9f, 22f, 5f, 21.1f, 5f, 20f); lineTo(5f, 6f)
                moveTo(8f, 6f); lineTo(8f, 4f); curveTo(8f, 2.9f, 8.9f, 2f, 10f, 2f); lineTo(14f, 2f); curveTo(15.1f, 2f, 16f, 2.9f, 16f, 4f); lineTo(16f, 6f)
                moveTo(10f, 11f); lineTo(10f, 17f)
                moveTo(14f, 11f); lineTo(14f, 17f)
            }
        }.build()
    }


val WcmpIcons.Mic: ImageVector
    get() {
        return ImageVector.Builder(
            name = "Mic", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 1f); curveTo(10.3f, 1f, 9f, 2.3f, 9f, 4f); lineTo(9f, 11f); curveTo(9f, 12.7f, 10.3f, 14f, 12f, 14f); curveTo(13.7f, 14f, 15f, 12.7f, 15f, 11f); lineTo(15f, 4f); curveTo(15f, 2.3f, 13.7f, 1f, 12f, 1f); close()
                moveTo(19f, 11f); curveTo(19f, 14.9f, 15.9f, 18f, 12f, 18f); curveTo(8.1f, 18f, 5f, 14.9f, 5f, 11f)
                moveTo(12f, 18f); lineTo(12f, 22f)
                moveTo(8f, 22f); lineTo(16f, 22f)
            }
        }.build()
    }

val WcmpIcons.LocationOn: ImageVector
    get() {
        return ImageVector.Builder(
            name = "LocationOn", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(21f, 10f); curveTo(21f, 17f, 12f, 23f, 12f, 23f); curveTo(12f, 23f, 3f, 17f, 3f, 10f); curveTo(3f, 7.613f, 3.948f, 5.324f, 5.636f, 3.636f); curveTo(7.324f, 1.948f, 9.613f, 1f, 12f, 1f); curveTo(14.387f, 1f, 16.676f, 1.948f, 18.364f, 3.636f); curveTo(20.052f, 5.324f, 21f, 7.613f, 21f, 10f); close()
                moveTo(12f, 13f); curveTo(13.6569f, 13f, 15f, 11.6569f, 15f, 10f); curveTo(15f, 8.3431f, 13.6569f, 7f, 12f, 7f); curveTo(10.3431f, 7f, 9f, 8.3431f, 9f, 10f); curveTo(9f, 11.6569f, 10.3431f, 13f, 12f, 13f); close()
            }
        }.build()
    }

val WcmpIcons.ShieldX: ImageVector
    get() {
        return ImageVector.Builder(
            name = "ShieldX", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 22f); curveTo(12f, 22f, 20f, 18f, 20f, 12f); lineTo(20f, 5f); lineTo(12f, 2f); lineTo(4f, 5f); lineTo(4f, 12f); curveTo(4f, 18f, 12f, 22f, 12f, 22f); close()
                moveTo(9f, 9f); lineTo(15f, 15f)
                moveTo(15f, 9f); lineTo(9f, 15f)
            }
        }.build()
    }
val WcmpIcons.Camera: ImageVector
    get() = Image

val WcmpIcons.Navigation: ImageVector
    get() = LocationOn
