import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MainScene() {
    val grid = remember { mutableStateOf<GridModel?>(null) }

    if (grid.value == null) {
        GridChoiceScene(grid)
    } else {
        GridScene(grid)
    }
}

private val grids = listOf(25, 50, 100, 200, 400).map { GridModel(it) }

@Composable
private fun GridChoiceScene(selectedGrid: MutableState<GridModel?>) {
    Row {
        Column {
            for (grid in grids) {
                Button(onClick = { selectedGrid.value = grid }) {
                    Text("$grid")
                }
            }
        }

        Spacer(Modifier.width(24.dp))

        VerticalConfigurationSettings()
    }
}

@Composable
private fun GridScene(selectedGrid: MutableState<GridModel?>) {
    var configurationVisible by remember { mutableStateOf(false) }
    var running by remember { mutableStateOf(false) }
    var startMoment by remember { mutableStateOf<Instant?>(null) }
    var visualizationStepCount by remember { mutableStateOf(0) }

    fun startOrStop() {
        running = !running
        startMoment = if (running) Clock.System.now() else null
    }

    selectedGrid.value?.let { grid ->
        LaunchedEffect(running) {
            while (isActive && running) {
                withFpsCount {
                    grid.updateSingleCell(Configuration.updateTopRowOnlyEnabled.value)
                }
                if (Configuration.topLevelRecompositionForced.value) {
                    // Force recomposition by changing state on every update.
                    visualizationStepCount++
                }
                if (Configuration.pauseOnEachStep.value) {
                    delay(100.milliseconds)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { selectedGrid.value = null }) {
                    Text("Back")
                }
                Button(onClick = { startOrStop() }) {
                    Text(if (running) "Stop" else "Start")
                }
                Button(onClick = { grid.clear() }) {
                    Text("Clear")
                }
                Button(onClick = { configurationVisible = !configurationVisible }) {
                    Text(if (configurationVisible) "Hide Configuration" else "Show Configuration")
                }
            }
            Info(grid, startMoment, configurationVisible)

            Grid(grid, visualizationStepCount)
        }
    }
}

@Composable
private fun Grid(grid: GridModel, visualizationStepCount: Int) {
    sinkHole(visualizationStepCount)
    Box(Modifier.recomposeHighlighter().border(1.dp, color = Color.LightGray)) {
        Column {
            for (row in grid.rows) {
                Row(modifier = Modifier.recomposeHighlighter()) {
                    for (cell in row.cells) {
                        Cell(cell)
                    }
                }
            }
        }
    }
}

fun <T> sinkHole(value: T) {
    require(value.toString().isNotEmpty())
}

@Composable
private fun Cell(cell: CellModel) {
    Box(
        Modifier.size(22.dp).recomposeHighlighter().border(1.dp, color = Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (Configuration.animationsEnabled.value) {
            @OptIn(ExperimentalAnimationApi::class)
            AnimatedContent(cell.content, transitionSpec = {
                slideInVertically { height -> height } with
                    slideOutVertically { height -> -height }
            }) {
                CellText(it)
            }
        } else {
            CellText(cell.content)
        }
    }
}

@Composable
private fun CellText(cellContent: Int) {
    if (cellContent != 0) {
        Text("$cellContent", style = MaterialTheme.typography.h6)
    }
}

@Composable
private fun Info(grid: GridModel, startMoment: Instant?, showConfiguration: Boolean) {
    var secondsElapsed by remember { mutableStateOf(0L) }

    if (startMoment != null) {
        LaunchedEffect(startMoment) {
            FPSCount.reset()
            while (isActive) {
                delay(100.milliseconds)
                secondsElapsed = (Clock.System.now() - startMoment).inWholeSeconds
            }
        }
    }

    if (showConfiguration) {
        HorizontalConfigurationSettings()
    }

    Text("Grid: $grid, $secondsElapsed s, ${FPSCount.average} FPS")
}

@Composable
private fun HorizontalConfigurationSettings() {
    Column {
        Configuration.elements.toList().chunked(3).forEach { elements ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                elements.forEach { (label, flagState) ->
                    Checkbox(flagState.value, onCheckedChange = { flagState.value = it })
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun VerticalConfigurationSettings() {
    Column {
        Configuration.elements.forEach { (label, flagState) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(flagState.value, onCheckedChange = { flagState.value = it })
                Text(label)
            }
        }
    }
}
