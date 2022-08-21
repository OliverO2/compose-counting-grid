import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    onWasmReady {
        renderComposeTarget("Compose Counting Grid") {
            MainScene()
        }
    }
}
