import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main() {
    System.setProperty("skiko.vsync.enabled", "false") // allow high-speed refresh beyond monitor frame rates

    singleWindowApplication(
        title = "Compose Counting Grid",
        state = WindowState(width = 800.dp, height = 800.dp)
    ) {
        MaterialTheme {
            ScrollView {
                MainScene()
            }
        }
    }
}
