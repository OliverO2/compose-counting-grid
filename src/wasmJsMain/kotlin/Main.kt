import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

fun main() {
    val title = "Compose Counting Grid on JS/Wasm"
    @OptIn(ExperimentalComposeUiApi::class)
    CanvasBasedWindow(title, canvasElementId = "ComposeTarget") {
        MainScene(title)
    }
}
