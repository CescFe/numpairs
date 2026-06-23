package org.cescfe.numpairs.domain.puzzle

import org.cescfe.numpairs.domain.puzzle.assignment.ResolvedOperandAssignment
import org.cescfe.numpairs.domain.puzzle.model.Expression
import org.cescfe.numpairs.domain.puzzle.model.Operator
import org.cescfe.numpairs.domain.puzzle.model.Tile

fun hiddenExpression(): Expression = Expression(
    leftOperand = Expression.Operand.Hidden,
    operator = Operator.Hidden,
    rightOperand = Expression.Operand.Hidden
)

fun resolvedTile(
    leftOperand: ResolvedOperandAssignment,
    operator: Operator,
    rightOperand: ResolvedOperandAssignment
): Tile {
    require(operator != Operator.Hidden) {
        "Resolved tiles require a concrete operator."
    }

    return Tile(
        expression = Expression(
            leftOperand = Expression.Operand.Known(
                value = leftOperand.value,
                stripEntryId = leftOperand.stripEntryId
            ),
            operator = operator,
            rightOperand = Expression.Operand.Known(
                value = rightOperand.value,
                stripEntryId = rightOperand.stripEntryId
            )
        ),
        result = operator.apply(
            leftOperand = leftOperand.value,
            rightOperand = rightOperand.value
        )
    )
}

fun Tile.withHiddenExpression(): Tile = copy(expression = hiddenExpression())
