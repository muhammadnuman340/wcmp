package com.numan.wcmp.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val WcmpIcons.ChevronRight: ImageVector
    get() {
        return ImageVector.Builder(
            name = "ChevronRight", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(9f, 18f); lineTo(15f, 12f); lineTo(9f, 6f)
            }
        }.build()
    }

val WcmpIcons.ChevronLeft: ImageVector
    get() {
        return ImageVector.Builder(
            name = "ChevronLeft", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(15f, 18f); lineTo(9f, 12f); lineTo(15f, 6f)
            }
        }.build()
    }

val WcmpIcons.MoreVertical: ImageVector
    get() {
        return ImageVector.Builder(
            name = "MoreVertical", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 12f); curveTo(12.55f, 12f, 13f, 11.55f, 13f, 11f); curveTo(13f, 10.45f, 12.55f, 10f, 12f, 10f); curveTo(11.45f, 10f, 11f, 10.45f, 11f, 11f); curveTo(11f, 11.55f, 11.45f, 12f, 12f, 12f); close()
                moveTo(12f, 19f); curveTo(12.55f, 19f, 13f, 18.55f, 13f, 18f); curveTo(13f, 17.45f, 12.55f, 17f, 12f, 17f); curveTo(11.45f, 17f, 11f, 17.45f, 11f, 18f); curveTo(11f, 18.55f, 11.45f, 19f, 12f, 19f); close()
                moveTo(12f, 5f); curveTo(12.55f, 5f, 13f, 4.55f, 13f, 4f); curveTo(13f, 3.45f, 12.55f, 3f, 12f, 3f); curveTo(11.45f, 3f, 11f, 3.45f, 11f, 4f); curveTo(11f, 4.55f, 11.45f, 5f, 12f, 5f); close()
            }
        }.build()
    }

val WcmpIcons.Image: ImageVector
    get() {
        return ImageVector.Builder(
            name = "Image", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(21f, 19f); curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f); lineTo(5f, 21f); curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f); lineTo(3f, 5f); curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f); lineTo(19f, 3f); curveTo(20.1f, 3f, 21f, 3.9f, 21f, 5f); lineTo(21f, 19f); close()
                moveTo(8.5f, 13.5f); lineTo(11f, 16.5f); lineTo(14.5f, 12f); lineTo(19f, 18f); lineTo(5f, 18f); lineTo(8.5f, 13.5f); close()
            }
        }.build()
    }

val WcmpIcons.Search: ImageVector
    get() {
        return ImageVector.Builder(
            name = "Search", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(11f, 19f); curveTo(15.418f, 19f, 19f, 15.418f, 19f, 11f); curveTo(19f, 6.582f, 15.418f, 3f, 11f, 3f); curveTo(6.582f, 3f, 3f, 6.582f, 3f, 11f); curveTo(3f, 15.418f, 6.582f, 19f, 11f, 19f); close()
                moveTo(21f, 21f); lineTo(16.65f, 16.65f)
            }
        }.build()
    }

val WcmpIcons.Close: ImageVector
    get() {
        return ImageVector.Builder(
            name = "Close", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(18f, 6f); lineTo(6f, 18f)
                moveTo(6f, 6f); lineTo(18f, 18f)
            }
        }.build()
    }

val WcmpIcons.ShieldAlert: ImageVector
    get() {
        return ImageVector.Builder(
            name = "ShieldAlert", defaultWidth = 24.dp, defaultHeight = 24.dp,
            viewportWidth = 24f, viewportHeight = 24f
        ).apply {
            path(fill = null, stroke = SolidColor(Color.White), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(12f, 22f); curveTo(12f, 22f, 20f, 18f, 20f, 12f); lineTo(20f, 5f); lineTo(12f, 2f); lineTo(4f, 5f); lineTo(4f, 12f); curveTo(4f, 18f, 12f, 22f, 12f, 22f); close()
                moveTo(12f, 8f); lineTo(12f, 12f)
                moveTo(12f, 16f); lineTo(12.01f, 16f)
            }
        }.build()
    }
