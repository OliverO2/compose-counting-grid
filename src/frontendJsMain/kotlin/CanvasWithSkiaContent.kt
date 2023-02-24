@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "EXPOSED_PARAMETER_TYPE"
) // WORKAROUND: ComposeLayer is internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.createSkiaLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.native.ComposeLayer
import androidx.compose.ui.unit.IntSize
import kotlinx.browser.window
import org.jetbrains.compose.web.dom.Canvas
import org.w3c.dom.HTMLCanvasElement

@Composable
fun CanvasWithSkiaContent(content: @Composable () -> Unit) = Canvas({
    ref {
        val canvasLayer = CanvasLayer(it)
        canvasLayer.setContent(content)
        onDispose { canvasLayer.dispose() }
    }
})

private class CanvasLayer(private val canvas: HTMLCanvasElement) {

    var size by mutableStateOf(IntSize(canvas.width, canvas.height))

    val composeLayer = ComposeLayer(
        layer = createSkiaLayer(),
        showSoftwareKeyboard = { println("TODO showSoftwareKeyboard in JS") },
        hideSoftwareKeyboard = { println("TODO hideSoftwareKeyboard in JS") },
        getTopLeftOffset = { Offset.Zero }
    )

    private fun reload(newSize: IntSize) {
        console.info("CanvasLayer.reload($newSize)")
        canvas.width = newSize.width
        canvas.height = newSize.height
        // The only way to update the underlying SkiaLayer's size seems to be via `attachTo`.
        // However, `detach` is not implemented, so this may be unstable.
        with(composeLayer) {
            layer.attachTo(canvas)
            layer.needRedraw()
            val scale = layer.contentScale
            val density = window.devicePixelRatio.toFloat()
            setSize((newSize.width / scale * density).toInt(), (newSize.height / scale * density).toInt())
        }
    }

    fun setContent(content: @Composable () -> Unit) = composeLayer.setContent {
        LaunchedEffect(size) { reload(size) }
        Box(
            Modifier
                .wrapContentSize(unbounded = true)
                .onSizeChanged { size = it }
        ) { content() }
    }

    fun dispose() = composeLayer.dispose()
}
