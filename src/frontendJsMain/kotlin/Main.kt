
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        BrowserViewportWindow("Compose Counting Grid") {
            MainScene()
        }
    }
}
