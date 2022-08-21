@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "EXPOSED_PARAMETER_TYPE"
) // WORKAROUND: ComposeWindow and ComposeLayer are internal

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.window.ComposeWindow
import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLStyleElement
import org.w3c.dom.HTMLTitleElement

private const val CANVAS_ELEMENT_ID = "ComposeTarget" // Hardwired into ComposeWindow

/**
 * Renders Compose content in the browser's canvas element #[CANVAS_ELEMENT_ID], auto-sizing the element.
 */
fun renderComposeTarget(
    title: String? = null,
    content: @Composable () -> Unit
) {
    val htmlHeadElement = document.head!!
    htmlHeadElement.appendChild(
        (document.createElement("style") as HTMLStyleElement).apply {
            type = "text/css"
            appendChild(document.createTextNode("#$CANVAS_ELEMENT_ID { outline: none; }"))
        }
    )

    if (title != null) {
        // WORKAROUND: ComposeWindow does not implement `setTitle(title)`
        val htmlTitleElement = (
            htmlHeadElement.getElementsByTagName("title").item(0)
                ?: document.createElement("title").also { htmlHeadElement.appendChild(it) }
            ) as HTMLTitleElement
        htmlTitleElement.textContent = title
    }

    ComposeWindow().apply {
        setContent {
            AutoSizedBrowserCanvas(ComposeManagedBrowserCanvas(this)) {
                content()
            }
        }
    }
}

@Composable
private fun AutoSizedBrowserCanvas(
    browserCanvas: ComposeManagedBrowserCanvas,
    content: @Composable () -> Unit
) {
    Box(
        Modifier.layout { measurable, _ ->
            val placeable = measurable.measure(Constraints())

            layout(placeable.width, placeable.height) {
                browserCanvas.resize(placeable.measuredWidth, placeable.measuredHeight)
                placeable.placeRelative(0, 0)
            }
        }
    ) {
        content()
    }
}

private class ComposeManagedBrowserCanvas(
    private val composeWindow: ComposeWindow,
    private val elementId: String = CANVAS_ELEMENT_ID
) {
    private var htmlCanvasElement = document.getElementById(elementId) as HTMLCanvasElement
    private var width by htmlCanvasElement::width
    private var height by htmlCanvasElement::height

    fun resize(newWidth: Int, newHeight: Int) {
        if (newWidth == width && newHeight == height) return

        console.info(
            "ComposeManagedBrowserCanvas: resizing #$elementId from ($width, $height) to ($newWidth, $newHeight)"
        )

        width = newWidth
        height = newHeight

        with(composeWindow.layer) {
            // The only way to update the underlying SkiaLayer's size seems to be via `attachTo`.
            // However, `detach` is not implemented, so this may be unstable.
            layer.attachTo(htmlCanvasElement)
            layer.needRedraw()
            val scale = layer.contentScale
            setSize((width / scale).toInt(), (height / scale).toInt())
        }
    }
}
