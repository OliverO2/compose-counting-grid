import kotlinx.datetime.Clock

fun log(message: String) {
    println("${Clock.System.now()} – $message")
}
