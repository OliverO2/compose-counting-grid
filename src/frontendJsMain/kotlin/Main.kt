import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTitleElement

fun main() {
    onWasmReady {
        val htmlHeadElement = document.head!!
        val htmlTitleElement = (
            htmlHeadElement.getElementsByTagName("title").item(0)
                ?: document.createElement("title").also { htmlHeadElement.appendChild(it) }
            ) as HTMLTitleElement
        htmlTitleElement.textContent = "Compose Counting Grid"

        val htmlComposeTargetElement = document.getElementById("ComposeTarget") as HTMLDivElement

        renderComposable(htmlComposeTargetElement) {
            CanvasWithSkiaContent {
                MainScene()
            }
        }
    }
}
