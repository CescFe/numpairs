package org.cescfe.numpairs.domain.puzzle

data class Board(
    val rows: List<List<Tile>>
) {
    init {
        require(rows.size == ROW_COUNT) {
            "Board must contain exactly $ROW_COUNT rows."
        }
        require(rows.all { row -> row.size == COLUMN_COUNT }) {
            "Each board row must contain exactly $COLUMN_COUNT tiles."
        }
    }

    val tiles: List<Tile>
        get() = rows.flatten()

    fun tileAt(rowIndex: Int, columnIndex: Int): Tile = rows[rowIndex][columnIndex]

    companion object {
        const val ROW_COUNT = 2
        const val COLUMN_COUNT = 4
        const val TILE_COUNT = ROW_COUNT * COLUMN_COUNT
    }
}
