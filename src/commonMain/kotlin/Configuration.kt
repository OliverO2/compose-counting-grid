import androidx.compose.runtime.mutableStateOf

object Configuration {
    class Element(
        val label: String,
        initialValue: Boolean,
        private val onChange: ((element: Element, newState: Boolean) -> Unit)? = Configuration::logAction
    ) {
        private val _state = mutableStateOf(initialValue)
        var value
            get() = _state.value
            set(value) {
                onChange?.invoke(this, value)
                _state.value = value
            }

        init {
            onChange?.invoke(this, _state.value)
        }
    }

    val pauseOnEachStep = Element("Pause on each step (100ms)", false)
    val updateTopRowOnlyEnabled = Element("Update top row only", false)
    val animationsEnabled = Element("Enable animations", false)
    val recomposeHighlightingEnabled = Element("Highlight recompositions", false)
    val topLevelRecompositionForced = Element("Force top-level recomposition", false)
    val rowLevelRecompositionForced = Element("Force row-level recomposition", false)
    val cellLevelRecompositionForced = Element("Force cell-level recomposition", false)
    val cellTextDrawingEnabled = Element("Draw cell text", true)
    private val trackDrawingEnabled = Element("Track drawing", true) { element, newState ->
        logAction(element, newState)
        drawSeries = if (newState) DrawSeries() else null
    }
    val gridHidingEnabled = Element("Hide grid temporarily when switching animations", true)

    val elements = listOf(
        pauseOnEachStep,
        updateTopRowOnlyEnabled,
        animationsEnabled,
        recomposeHighlightingEnabled,
        topLevelRecompositionForced,
        rowLevelRecompositionForced,
        cellLevelRecompositionForced,
        cellTextDrawingEnabled,
        trackDrawingEnabled,
        gridHidingEnabled
    )

    private fun logAction(element: Element, newState: Boolean) {
        log("${element.label} -> ${if (newState) "ON " else "OFF"}")
    }
}
