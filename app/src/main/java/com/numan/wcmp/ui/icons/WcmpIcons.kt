package com.numan.wcmp.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * WCMP Vector Icons
 * Tailored cybersecurity iconography built natively in Jetpack Compose.
 */
object WcmpIcons {

    val Shield: ImageVector by lazy {
        ImageVector.Builder(
            name = "Shield",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 2f)
                lineTo(3f, 6f)
                verticalLineTo(11f)
                curveTo(3f, 16.5f, 6.8f, 21.7f, 12f, 23f)
                curveTo(17.2f, 21.7f, 21f, 16.5f, 21f, 11f)
                verticalLineTo(6f)
                close()
            }
        }.build()
    }

    val ShieldCheck: ImageVector by lazy {
        ImageVector.Builder(
            name = "ShieldCheck",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 2f)
                lineTo(3f, 6f)
                verticalLineTo(11f)
                curveTo(3f, 16.5f, 6.8f, 21.7f, 12f, 23f)
                curveTo(17.2f, 21.7f, 21f, 16.5f, 21f, 11f)
                verticalLineTo(6f)
                close()
                moveTo(9f, 12f)
                lineTo(11f, 14f)
                lineTo(15f, 10f)
            }
        }.build()
    }

    val Eye: ImageVector by lazy {
        ImageVector.Builder(
            name = "Eye",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(1f, 12f)
                curveTo(1f, 12f, 5f, 4f, 12f, 4f)
                curveTo(19f, 4f, 23f, 12f, 23f, 12f)
                curveTo(23f, 12f, 19f, 20f, 12f, 20f)
                curveTo(5f, 20f, 1f, 12f, 1f, 12f)
                close()
            }
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                close()
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "Lock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 11f)
                lineTo(19f, 11f)
                curveTo(20.1f, 11f, 21f, 11.9f, 21f, 13f)
                lineTo(21f, 20f)
                curveTo(21f, 21.1f, 20.1f, 22f, 19f, 22f)
                lineTo(5f, 22f)
                curveTo(3.9f, 22f, 3f, 21.1f, 3f, 20f)
                lineTo(3f, 13f)
                curveTo(3f, 11.9f, 3.9f, 11f, 5f, 11f)
                close()
                moveTo(7f, 11f)
                verticalLineTo(7f)
                curveTo(7f, 4.24f, 9.24f, 2f, 12f, 2f)
                curveTo(14.76f, 2f, 17f, 4.24f, 17f, 7f)
                verticalLineTo(11f)
            }
        }.build()
    }

    val Radar: ImageVector by lazy {
        ImageVector.Builder(
            name = "Radar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 12f)
                curveTo(12f, 12f, 12f, 12f, 12f, 12f)
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(12f, 6f)
                curveTo(8.69f, 6f, 6f, 8.69f, 6f, 12f)
                curveTo(6f, 15.31f, 8.69f, 18f, 12f, 18f)
                curveTo(15.31f, 18f, 18f, 15.31f, 18f, 12f)
                moveTo(12f, 12f)
                lineTo(19f, 5f)
            }
        }.build()
    }

    val History: ImageVector by lazy {
        ImageVector.Builder(
            name = "History",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 12f)
                curveTo(3f, 7.03f, 7.03f, 3f, 12f, 3f)
                curveTo(16.97f, 3f, 21f, 7.03f, 21f, 12f)
                curveTo(21f, 16.97f, 16.97f, 21f, 12f, 21f)
                curveTo(8.05f, 21f, 4.75f, 18.45f, 3.53f, 14.88f)
                moveTo(12f, 7f)
                verticalLineTo(12f)
                lineTo(15f, 15f)
            }
        }.build()
    }

    val Apps: ImageVector by lazy {
        ImageVector.Builder(
            name = "Apps",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 4f)
                lineTo(9f, 4f)
                lineTo(9f, 9f)
                lineTo(4f, 9f)
                close()
                moveTo(15f, 4f)
                lineTo(20f, 4f)
                lineTo(20f, 9f)
                lineTo(15f, 9f)
                close()
                moveTo(4f, 15f)
                lineTo(9f, 15f)
                lineTo(9f, 20f)
                lineTo(4f, 20f)
                close()
                moveTo(15f, 15f)
                lineTo(20f, 15f)
                lineTo(20f, 20f)
                lineTo(15f, 20f)
                close()
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "Settings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                close()
                moveTo(19.4f, 15f)
                curveTo(19.5f, 14.5f, 19.5f, 14f, 19.5f, 13.5f)
                curveTo(19.5f, 13f, 19.5f, 12.5f, 19.4f, 12f)
                lineTo(21.5f, 10.4f)
                curveTo(21.7f, 10.2f, 21.8f, 9.9f, 21.6f, 9.7f)
                lineTo(19.6f, 6.3f)
                curveTo(19.5f, 6.1f, 19.2f, 6f, 19f, 6.1f)
                lineTo(16.5f, 7.1f)
                curveTo(16f, 6.7f, 15.4f, 6.4f, 14.8f, 6.1f)
                lineTo(14.4f, 3.5f)
                curveTo(14.4f, 3.2f, 14.2f, 3f, 13.9f, 3f)
                lineTo(10.1f, 3f)
                curveTo(9.8f, 3f, 9.6f, 3.2f, 9.6f, 3.5f)
                lineTo(9.2f, 6.1f)
                curveTo(8.6f, 6.4f, 8f, 6.7f, 7.5f, 7.1f)
                lineTo(5f, 6.1f)
                curveTo(4.8f, 6f, 4.5f, 6.1f, 4.4f, 6.3f)
                lineTo(2.4f, 9.7f)
                curveTo(2.2f, 9.9f, 2.3f, 10.2f, 2.5f, 10.4f)
                lineTo(4.6f, 12f)
                curveTo(4.5f, 12.5f, 4.5f, 13f, 4.5f, 13.5f)
                curveTo(4.5f, 14f, 4.5f, 14.5f, 4.6f, 15f)
                lineTo(2.5f, 16.6f)
                curveTo(2.3f, 16.8f, 2.2f, 17.1f, 2.4f, 17.3f)
                lineTo(4.4f, 20.7f)
                curveTo(4.5f, 20.9f, 4.8f, 21f, 5f, 20.9f)
                lineTo(7.5f, 19.9f)
                curveTo(8f, 20.3f, 8.6f, 20.6f, 9.2f, 20.9f)
                lineTo(9.6f, 23.5f)
                curveTo(9.6f, 23.8f, 9.8f, 24f, 10.1f, 24f)
                lineTo(13.9f, 24f)
                curveTo(14.2f, 24f, 14.4f, 23.8f, 14.4f, 23.5f)
                lineTo(14.8f, 20.9f)
                curveTo(15.4f, 20.6f, 16f, 20.3f, 16.5f, 19.9f)
                lineTo(19f, 20.9f)
                curveTo(19.2f, 21f, 19.5f, 20.9f, 19.6f, 20.7f)
                lineTo(21.6f, 17.3f)
                curveTo(21.8f, 17.1f, 21.7f, 16.8f, 21.5f, 16.6f)
                lineTo(19.4f, 15f)
                close()
            }
        }.build()
    }

    val Menu: ImageVector by lazy {
        ImageVector.Builder(
            name = "Menu",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3f, 12f)
                lineTo(21f, 12f)
                moveTo(3f, 6f)
                lineTo(21f, 6f)
                moveTo(3f, 18f)
                lineTo(21f, 18f)
            }
        }.build()
    }

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "ArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 12f)
                lineTo(5f, 12f)
                moveTo(12f, 19f)
                lineTo(5f, 12f)
                lineTo(12f, 5f)
            }
        }.build()
    }
    
    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20f, 6f)
                lineTo(9f, 17f)
                lineTo(4f, 12f)
            }
        }.build()
    }
}
