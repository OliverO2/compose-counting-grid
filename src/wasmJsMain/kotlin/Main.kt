import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

fun main() {
    @OptIn(ExperimentalComposeUiApi::class)
    CanvasBasedWindow("Compose Counting Grid", canvasElementId = "ComposeTarget") {
        MainScene()
    }
}
