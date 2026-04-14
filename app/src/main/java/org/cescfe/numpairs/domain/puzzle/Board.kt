package org.cescfe.numpairs.domain.puzzle

data class Board(val tileRows: List<List<Tile>>) {
    init {
        require(tileRows.size == ROW_COUNT) {
            "Board must contain exactly $ROW_COUNT rows."
        }
        require(tileRows.all { row -> row.size == COLUMN_COUNT }) {
            "Each board row must contain exactly $COLUMN_COUNT tiles."
        }
    }

    val tiles: List<Tile>
        get() = tileRows.flatten()

    fun tileAt(rowIndex: Int, columnIndex: Int): Tile = tileRows[rowIndex][columnIndex]

    companion object {
        const val ROW_COUNT = 2
        const val COLUMN_COUNT = 4
        const val TILE_COUNT = ROW_COUNT * COLUMN_COUNT
    }
}
