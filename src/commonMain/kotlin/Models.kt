import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

class CellModel {
    var content by mutableStateOf(0)
}

class RowModel(columnCount: Int) {
    val cells = Array(columnCount) { CellModel() }
}

class GridModel(private val rowCount: Int, private val columnCount: Int = rowCount) {
    val rows = Array(rowCount) { RowModel(columnCount) }

    private fun cell(rowIndex: Int, columnIndex: Int) = rows[rowIndex].cells[columnIndex]

    fun updateSingleCell(updateTopRowOnly: Boolean) {
        val target = cell(if (updateTopRowOnly) 0 else Random.nextInt(rowCount), Random.nextInt(columnCount))
        target.content = (target.content + 1).mod(10)
    }

    fun clear() {
        for (row in rows) {
            for (cell in row.cells) {
                cell.content = 0
            }
        }
    }

    override fun toString(): String = "${rowCount}x$columnCount (${rowCount * columnCount} cells)"
}
