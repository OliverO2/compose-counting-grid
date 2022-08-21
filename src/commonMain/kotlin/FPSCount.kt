import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlin.math.roundToInt

suspend fun withFpsCount(block: () -> Unit) {
    withFrameNanos {
        block()
        FPSCount.recordFrame(it)
    }
}

object FPSCount {
    private const val NANOS_PER_SECOND = 1_000_000_000.0

    private const val updateIntervalNanos = (1 * NANOS_PER_SECOND).toLong()

    // Instants are frame clock values.
    private var lastUpdateInstantNanos = 0L
    private var lastFrameInstantNanos: Long = 0L

    private val collectedFrameNanoDurations = mutableListOf<Long>()

    var average by mutableStateOf(0)

    fun recordFrame(frameInstantNanos: Long) {
        if (lastFrameInstantNanos != 0L) {
            collectedFrameNanoDurations.add(frameInstantNanos - lastFrameInstantNanos)
        }
        lastFrameInstantNanos = frameInstantNanos

        if ((frameInstantNanos - lastUpdateInstantNanos)
            >= updateIntervalNanos && collectedFrameNanoDurations.isNotEmpty()
        ) {
            average = (NANOS_PER_SECOND / collectedFrameNanoDurations.average()).roundToInt()
            collectedFrameNanoDurations.clear()
            lastUpdateInstantNanos = frameInstantNanos
        }
    }

    fun reset() {
        average = 0
    }
}
