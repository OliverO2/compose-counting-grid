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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MainScene() {
    val selectedGrid = remember { mutableStateOf<GridModel?>(null) }

    LaunchedEffect(selectedGrid.value) {
        log("Grid -> ${selectedGrid.value}")
        while (isActive) {
            drawSeries?.tryToFinish()
            delay(100.milliseconds)
        }
    }

    if (selectedGrid.value == null) {
        GridChoiceScene(selectedGrid)
    } else {
        GridScene(selectedGrid)
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
    val topLevelRecompositionTrigger = remember { mutableStateOf(0) }
    val rowLevelRecompositionTrigger = remember { mutableStateOf(0) }
    val cellLevelRecompositionTrigger = remember { mutableStateOf(0) }

    val grid = selectedGrid.value ?: return

    // Avoiding an unresponsive UI by temporarily hiding the grid when switching the animation setting:
    // Provisioning lots of cells with animations stresses the slot table. Direct switching from an unprovisioned
    // grid to a provisioned one (and vice versa) is much slower than removing the first grid, wait for a slot
    // table update, then adding the second grid.
    var animationsEnabledAfterDelay by remember { mutableStateOf(Configuration.animationsEnabled.value) }
    val showGrid = (
        !Configuration.gridHidingEnabled.value ||
            (animationsEnabledAfterDelay == Configuration.animationsEnabled.value)
        )
    LaunchedEffect(Unit) {
        snapshotFlow { Configuration.animationsEnabled.value }.collect {
            delay(200.milliseconds) // Wait for a slot table update
            animationsEnabledAfterDelay = it
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ControlsAndInfo(
            selectedGrid,
            topLevelRecompositionTrigger,
            rowLevelRecompositionTrigger,
            cellLevelRecompositionTrigger
        )

        if (showGrid) {
            Grid(
                grid,
                topLevelRecompositionTrigger,
                rowLevelRecompositionTrigger,
                cellLevelRecompositionTrigger
            )
        }
    }
}

@Composable
private fun ControlsAndInfo(
    selectedGrid: MutableState<GridModel?>,
    topLevelRecompositionTrigger: MutableState<Int>,
    rowLevelRecompositionTrigger: MutableState<Int>,
    cellLevelRecompositionTrigger: MutableState<Int>
) {
    val grid = selectedGrid.value!!

    var configurationVisible by remember { mutableStateOf(false) }
    val gridUpdateScope = rememberCoroutineScope()
    var gridUpdateJob by remember { mutableStateOf<Job?>(null) }
    var startMoment by remember { mutableStateOf<Instant?>(null) }

    fun startOrStop() {
        if (gridUpdateJob == null) {
            gridUpdateJob = gridUpdateScope.launch {
                log("Starting")
                startMoment = Clock.System.now()
                try {
                    while (isActive) {
                        withFpsCount {
                            grid.updateSingleCell(Configuration.updateTopRowOnlyEnabled.value)
                        }
                        // Force recomposition by changing state on every update.
                        if (Configuration.topLevelRecompositionForced.value) topLevelRecompositionTrigger.value++
                        if (Configuration.rowLevelRecompositionForced.value) rowLevelRecompositionTrigger.value++
                        if (Configuration.cellLevelRecompositionForced.value) cellLevelRecompositionTrigger.value++
                        if (Configuration.pauseOnEachStep.value) {
                            delay(100.milliseconds)
                        }
                    }
                } finally {
                    log("Stopping")
                }
            }
        } else {
            gridUpdateJob?.cancel()
            gridUpdateJob = null
            startMoment = null
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { selectedGrid.value = null }) {
            Text("Back")
        }
        Button(onClick = { startOrStop() }) {
            Text(if (startMoment == null) "Start" else "Stop")
        }
        Button(onClick = { grid.clear() }) {
            Text("Clear")
        }
        Button(onClick = { configurationVisible = !configurationVisible }) {
            Text(if (configurationVisible) "Hide Configuration" else "Show Configuration")
        }
    }

    Info(grid, startMoment, configurationVisible)
}

@Composable
private fun Grid(
    grid: GridModel,
    topLevelRecompositionTrigger: State<Int>,
    rowLevelRecompositionTrigger: State<Int>,
    cellLevelRecompositionTrigger: State<Int>
) {
    sinkHole(topLevelRecompositionTrigger.value)
    Box(Modifier.recomposeHighlighter().border(1.dp, color = Color.LightGray)) {
        Column {
            for (row in grid.rows) {
                Row(modifier = Modifier.recomposeHighlighter()) {
                    sinkHole(rowLevelRecompositionTrigger.value)
                    for (cell in row.cells) {
                        Cell(cell, cellLevelRecompositionTrigger)
                    }
                }
            }
        }
    }
}

@Composable
private fun Cell(cell: CellModel, cellLevelRecompositionTrigger: State<Int>) {
    @Suppress("UnusedReceiverParameter")
    fun Modifier.drawSupervised() = Modifier.drawWithContent {
        drawSeries?.addCellOperation()
        if (Configuration.cellTextDrawingEnabled.value) {
            drawContent()
        }
    }

    Box(
        Modifier.size(22.dp).recomposeHighlighter().border(1.dp, color = Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        sinkHole(cellLevelRecompositionTrigger.value)
        if (Configuration.animationsEnabled.value) {
            @OptIn(ExperimentalAnimationApi::class)
            AnimatedContent(
                cell.content,
                modifier = Modifier.drawSupervised(),
                transitionSpec = {
                    slideInVertically { height -> height } with
                        slideOutVertically { height -> -height }
                }
            ) {
                CellText(it)
            }
        } else {
            CellText(cell.content, modifier = Modifier.drawSupervised())
        }
    }
}

/** Consumes a value in a fashion the compiler (hopefully) would not identify as a no-op (and optimize away). */
fun <T> sinkHole(value: T) {
    require(value.toString().isNotEmpty())
}

@Composable
private fun CellText(cellContent: Int, modifier: Modifier = Modifier) {
    Text(if (cellContent != 0) "$cellContent" else "", modifier = modifier, style = MaterialTheme.typography.h6)
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
        Configuration.elements.chunked(4).forEach { elements ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                elements.forEach {
                    ConfigurationFlag(it)
                }
            }
        }
    }
}

@Composable
private fun VerticalConfigurationSettings() {
    Column {
        Configuration.elements.forEach {
            ConfigurationFlag(it)
        }
    }
}

@Composable
private fun ConfigurationFlag(element: Configuration.Element) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(element.value, onCheckedChange = { element.value = it })
        Text(element.label)
    }
}
