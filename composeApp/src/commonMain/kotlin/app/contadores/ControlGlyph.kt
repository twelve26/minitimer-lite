package app.contadores

import app.contadores.resources.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

internal enum class Glyph { Pencil, Sun, Moon, Handle, Tally }

@Composable
internal fun ThemeButton(dark: Boolean, toggle: () -> Unit, enabled: Boolean = true) {
    GlyphButton(
        glyph = if (dark) Glyph.Sun else Glyph.Moon,
        label = if (dark) stringResource(Res.string.activate_light) else stringResource(Res.string.activate_dark),
        onClick = toggle,
        enabled = enabled,
    )
}

@Composable
internal fun GlyphButton(glyph: Glyph, label: String, onClick: () -> Unit, enabled: Boolean = true) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.semantics { contentDescription = label },
    ) { DrawGlyph(glyph) }
}

@Composable
internal fun DrawGlyph(glyph: Glyph, modifier: Modifier = Modifier.size(22.dp)) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val unit = size.width / 24
        fun point(x: Float, y: Float) = Offset(x * unit, y * unit)
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(color, point(x1, y1), point(x2, y2), 1.8f * unit, StrokeCap.Round)
        }
        when (glyph) {
            Glyph.Pencil -> {
                line(4f, 20f, 5f, 15f)
                line(4f, 20f, 9f, 19f)
                line(5f, 15f, 17f, 3f)
                line(9f, 19f, 21f, 7f)
                line(17f, 3f, 21f, 7f)
                line(14.5f, 5.5f, 18.5f, 9.5f)
            }
            Glyph.Tally -> {
                line(6f, 5f, 6f, 19f)
                line(12f, 5f, 12f, 19f)
                line(18f, 5f, 18f, 19f)
                line(3f, 17f, 21f, 7f)
            }
            Glyph.Handle -> {
                line(4f, 9f, 20f, 9f)
                line(4f, 15f, 20f, 15f)
            }
            Glyph.Sun -> {
                drawCircle(color, 4.4f * unit, point(12f, 12f))
                line(12f, 2f, 12f, 4f)
                line(12f, 20f, 12f, 22f)
                line(2f, 12f, 4f, 12f)
                line(20f, 12f, 22f, 12f)
                line(5f, 5f, 6.5f, 6.5f)
                line(17.5f, 17.5f, 19f, 19f)
                line(5f, 19f, 6.5f, 17.5f)
                line(17.5f, 6.5f, 19f, 5f)
            }
            Glyph.Moon -> drawPath(
                path = Path().apply {
                    moveTo(11f * unit, 3f * unit)
                    cubicTo(4f * unit, 4f * unit, 2f * unit, 9f * unit, 4f * unit, 15f * unit)
                    cubicTo(6f * unit, 22f * unit, 17f * unit, 24f * unit, 21f * unit, 15f * unit)
                    cubicTo(13f * unit, 17f * unit, 8f * unit, 11f * unit, 11f * unit, 3f * unit)
                    close()
                },
                color = color,
            )
        }
    }
}
