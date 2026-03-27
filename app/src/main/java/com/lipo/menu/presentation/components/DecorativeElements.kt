package com.lipo.menu.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 像素风装饰元素 - 可爱的浮动星星
 */
@Composable
fun PixelStars(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "twinkle")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // 绘制多个像素星星
        drawPixelStar(
            center = Offset(size.width * 0.2f, size.height * 0.3f),
            size = 8.dp.toPx(),
            color = color.copy(alpha = alpha)
        )
        drawPixelStar(
            center = Offset(size.width * 0.8f, size.height * 0.2f),
            size = 6.dp.toPx(),
            color = color.copy(alpha = alpha * 0.8f)
        )
        drawPixelStar(
            center = Offset(size.width * 0.9f, size.height * 0.7f),
            size = 7.dp.toPx(),
            color = color.copy(alpha = alpha * 0.9f)
        )
    }
}

/**
 * 像素风装饰元素 - 浮动气泡
 */
@Composable
fun FloatingBubbles(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFFFF8FAB).copy(alpha = 0.3f),
        Color(0xFFB39DDB).copy(alpha = 0.3f),
        Color(0xFF81C784).copy(alpha = 0.3f)
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // 绘制多个圆形气泡
        drawCircle(
            color = colors[0],
            radius = 30.dp.toPx(),
            center = Offset(size.width * 0.1f, size.height * 0.2f + offsetY)
        )
        drawCircle(
            color = colors[1],
            radius = 20.dp.toPx(),
            center = Offset(size.width * 0.85f, size.height * 0.3f + offsetY * 0.8f)
        )
        drawCircle(
            color = colors[2],
            radius = 25.dp.toPx(),
            center = Offset(size.width * 0.15f, size.height * 0.7f + offsetY * 1.2f)
        )
    }
}

/**
 * 像素风装饰元素 - 波浪背景
 */
@Composable
fun WaveBackground(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.1f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val path = Path()
        val waveHeight = 50.dp.toPx()

        path.moveTo(0f, size.height)

        for (x in 0..size.width.toInt() step 10) {
            val y = size.height - waveHeight +
                    kotlin.math.sin((x + phase) * Math.PI / 180).toFloat() * 20.dp.toPx()
            path.lineTo(x.toFloat(), y)
        }

        path.lineTo(size.width, size.height)
        path.close()

        drawPath(
            path = path,
            color = color,
            style = Fill
        )
    }
}

/**
 * 绘制像素风格的星星
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPixelStar(
    center: Offset,
    size: Float,
    color: Color
) {
    val path = Path()

    // 像素星星形状（十字形）
    path.moveTo(center.x - size / 2, center.y)
    path.lineTo(center.x - size / 6, center.y - size / 6)
    path.lineTo(center.x, center.y - size / 2)
    path.lineTo(center.x + size / 6, center.y - size / 6)
    path.lineTo(center.x + size / 2, center.y)
    path.lineTo(center.x + size / 6, center.y + size / 6)
    path.lineTo(center.x, center.y + size / 2)
    path.lineTo(center.x - size / 6, center.y + size / 6)
    path.close()

    drawPath(
        path = path,
        color = color,
        style = Fill
    )
}

/**
 * 装饰性横幅 - 用于显示提示信息
 */
@Composable
fun DecorativeBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
