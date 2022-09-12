import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

var drawSeries: DrawSeries? = null

private val IDLE_DURATION = 1.seconds

/**
 * A series of drawing operations, starting with the first cell drawn and ending when idle for [IDLE_DURATION].
 */
class DrawSeries : SynchronizedObject() {
    private var startMoment: Instant? = null
    private var lastOperationMoment = Clock.System.now()
    private var cellOperationCount = 0

    fun addCellOperation() = synchronized(this) {
        lastOperationMoment = Clock.System.now()
        if (startMoment == null) {
            startMoment = lastOperationMoment
        }

        cellOperationCount++
    }

    fun tryToFinish() = synchronized(this) {
        startMoment?.let { firstMoment ->
            val pause = Clock.System.now() - lastOperationMoment

            if (pause >= IDLE_DURATION) {
                val seriesDuration = lastOperationMoment - firstMoment
                val drawnOrNot = if (Configuration.cellTextDrawingEnabled.value) "drawn" else "not drawn"
                log(
                    "$cellOperationCount cells $drawnOrNot in $seriesDuration from $firstMoment to $lastOperationMoment"
                )
                this.startMoment = null
                cellOperationCount = 0
            }
        }
    }
}
