import kotlinx.browser.document
import org.jetbrains.compose.web.renderComposable
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        // renderComposeTarget("Compose Counting Grid") {
        //     MainScene()
        // }
        renderComposable(document.body!!) {
            CanvasWithSkiaContent {
                MainScene()
            }
        }
    }
}
