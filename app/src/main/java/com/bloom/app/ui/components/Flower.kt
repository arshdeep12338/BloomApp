package com.bloom.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.bloom.app.ui.theme.Gold
import com.bloom.app.ui.theme.Rose

/** The Bloom logo: four petals around a gold centre, drawn with Canvas so it scales cleanly. */
@Composable
fun BloomFlower(
    modifier: Modifier = Modifier,
    petalColor: Color = Rose,
    centerColor: Color = Gold,
) {
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val s = size.minDimension
        val c = Offset(size.width / 2f, size.height / 2f)
        val petalW = s * 0.36f
        val petalH = s * 0.54f
        listOf(0f, 90f, 180f, 270f).forEach { angle ->
            rotate(degrees = angle, pivot = c) {
                drawOval(
                    color = petalColor,
                    topLeft = Offset(c.x - petalW / 2f, c.y - petalH + s * 0.05f),
                    size = Size(petalW, petalH),
                )
            }
        }
        drawCircle(color = centerColor, radius = s * 0.13f, center = c)
    }
}
