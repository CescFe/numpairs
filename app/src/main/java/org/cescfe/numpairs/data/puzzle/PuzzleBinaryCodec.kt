package org.cescfe.numpairs.data.puzzle

import java.io.DataInputStream
import java.io.DataOutputStream
import org.cescfe.numpairs.domain.puzzle.model.Board
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
import org.cescfe.numpairs.domain.puzzle.model.Strip
import org.cescfe.numpairs.domain.puzzle.model.StripEntry
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.domain.puzzle.model.Tile

internal fun DataOutputStream.writePuzzleSnapshot(puzzle: Puzzle) {
    writeInt(puzzle.board.tiles.size)
    puzzle.board.tiles.forEach(::writeTile)
    writeInt(puzzle.strip.entries.size)
    puzzle.strip.entries.forEach(::writeStripEntry)
}

private fun DataOutputStream.writeTile(tile: Tile) {
    writeOperand(tile.expression.leftOperand)
    writeOperator(tile.expression.operator)
    writeOperand(tile.expression.rightOperand)
    writeInt(tile.result)
}

private fun DataOutputStream.writeOperand(operand: Expression.Operand) {
    when (operand) {
        Expression.Operand.Hidden -> writeByte(OPERAND_HIDDEN)

        is Expression.Operand.Known -> {
            writeByte(OPERAND_KNOWN)
            writeInt(operand.value)
            writeBoolean(operand.stripEntryId != null)
            operand.stripEntryId?.let(::writeInt)
        }
    }
}

private fun DataOutputStream.writeOperator(operator: Operator) {
    val encodedOperator = when (operator) {
        Operator.Hidden -> OPERATOR_HIDDEN
        Operator.Addition -> OPERATOR_ADDITION
        Operator.Multiplication -> OPERATOR_MULTIPLICATION
    }
    writeByte(encodedOperator)
}

private fun DataOutputStream.writeStripEntry(entry: StripEntry) {
    writeInt(entry.id)
    when (val item = entry.item) {
        StripItem.Hidden -> writeByte(STRIP_ITEM_HIDDEN)

        is StripItem.Known -> {
            writeByte(STRIP_ITEM_KNOWN)
            writeInt(item.value)
        }

        is StripItem.PlayerEntered -> {
            writeByte(STRIP_ITEM_PLAYER_ENTERED)
            writeInt(item.value)
        }
    }
}

internal fun DataInputStream.readPuzzleSnapshot(): Puzzle {
    val tileCount = readPuzzleElementCount()
    val board = Board(
        tiles = List(tileCount) {
            readTile()
        }
    )
    val stripEntryCount = readPuzzleElementCount()
    val strip = Strip.fromEntries(
        entries = List(stripEntryCount) {
            readStripEntry()
        }
    )

    return Puzzle(
        board = board,
        strip = strip
    )
}

private fun DataInputStream.readTile(): Tile = Tile(
    expression = Expression(
        leftOperand = readOperand(),
        operator = readOperator(),
        rightOperand = readOperand()
    ),
    result = readInt()
)

private fun DataInputStream.readOperand(): Expression.Operand = when (readUnsignedByte()) {
    OPERAND_HIDDEN -> Expression.Operand.Hidden

    OPERAND_KNOWN -> {
        val value = readInt()
        val stripEntryId = if (readBoolean()) {
            readInt()
        } else {
            null
        }
        Expression.Operand.Known(
            value = value,
            stripEntryId = stripEntryId
        )
    }

    else -> throw InvalidPuzzleBinaryData()
}

private fun DataInputStream.readOperator(): Operator = when (readUnsignedByte()) {
    OPERATOR_HIDDEN -> Operator.Hidden
    OPERATOR_ADDITION -> Operator.ADDITION
    OPERATOR_MULTIPLICATION -> Operator.MULTIPLICATION
    else -> throw InvalidPuzzleBinaryData()
}

private fun DataInputStream.readStripEntry(): StripEntry {
    val id = readInt()
    val item = when (readUnsignedByte()) {
        STRIP_ITEM_HIDDEN -> StripItem.Hidden
        STRIP_ITEM_KNOWN -> StripItem.Known(readInt())
        STRIP_ITEM_PLAYER_ENTERED -> StripItem.PlayerEntered(readInt())
        else -> throw InvalidPuzzleBinaryData()
    }

    return StripEntry(
        id = id,
        item = item
    )
}

private fun DataInputStream.readPuzzleElementCount(): Int = readInt().also { count ->
    if (count !in MIN_PUZZLE_ELEMENT_COUNT..MAX_PUZZLE_ELEMENT_COUNT || count % 2 != 0) {
        throw InvalidPuzzleBinaryData()
    }
}

private class InvalidPuzzleBinaryData : IllegalArgumentException()

private const val MIN_PUZZLE_ELEMENT_COUNT = 2
private const val MAX_PUZZLE_ELEMENT_COUNT = 128
private const val OPERAND_HIDDEN = 0
private const val OPERAND_KNOWN = 1
private const val OPERATOR_HIDDEN = 0
private const val OPERATOR_ADDITION = 1
private const val OPERATOR_MULTIPLICATION = 2
private const val STRIP_ITEM_HIDDEN = 0
private const val STRIP_ITEM_KNOWN = 1
private const val STRIP_ITEM_PLAYER_ENTERED = 2
