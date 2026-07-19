package org.cescfe.numpairs.feature.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.StripItem
import org.cescfe.numpairs.feature.game.GameHighlightState
import org.cescfe.numpairs.feature.game.GameInteractionPolicy
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.GameTileExpressionSlot
import org.cescfe.numpairs.feature.game.GameTileExpressionSlotHighlight
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.game.presentation.StripItemVisualStyle
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags
import org.cescfe.numpairs.ui.theme.NumPairsComponents

@Composable
fun TutorialRoute(
    modifier: Modifier = Modifier,
    mode: TutorialMode = TutorialMode.LEARN_BASICS,
    guidedStage: GuidedOnboardingStage? = null,
    saveProgressAcrossRecreation: Boolean = true,
    onTutorialCompleted: (() -> Unit)? = null,
    onNavigateBack: () -> Unit = {}
) {
    require(guidedStage == null || mode == TutorialMode.LEARN_BASICS) {
        "Guided onboarding stages are available only in Learn basics mode."
    }
    val playbackKey = guidedStage ?: mode
    val steps = guidedStage?.let(TutorialContent::stepsFor) ?: TutorialContent.stepsFor(mode)
    val currentStepIndexState = if (saveProgressAcrossRecreation) {
        rememberSaveable(playbackKey) { mutableIntStateOf(0) }
    } else {
        remember(playbackKey) { mutableIntStateOf(0) }
    }
    val completionReportedState = if (saveProgressAcrossRecreation) {
        rememberSaveable(playbackKey) { mutableStateOf(false) }
    } else {
        remember(playbackKey) { mutableStateOf(false) }
    }
    var currentStepIndex by currentStepIndexState
    var hasReportedCompletion by completionReportedState
    var latestGameUiSnapshot by remember(playbackKey) { mutableStateOf<TutorialGameUiSnapshot?>(null) }
    val currentOnTutorialCompleted by rememberUpdatedState(onTutorialCompleted)
    val currentStep = steps[currentStepIndex]
    val currentScenario = TutorialContent.scenario(currentStep.scenarioId)
    val latestGameUiState = latestGameUiSnapshot
        ?.takeIf { snapshot -> snapshot.scenarioId == currentScenario.id }
        ?.uiState
    val stripItemEntryGuidance = currentStep.stripEntryGuidanceResId?.let { guidanceResId ->
        stringResource(guidanceResId)
    }

    LaunchedEffect(currentStepIndex, latestGameUiState) {
        val uiState = latestGameUiState ?: return@LaunchedEffect

        if (!currentStep.isComplete(uiState)) {
            return@LaunchedEffect
        }

        if (currentStepIndex < steps.lastIndex) {
            delay(TUTORIAL_STEP_ADVANCE_DELAY)
            currentStepIndex += 1
        } else if (!hasReportedCompletion && currentOnTutorialCompleted != null) {
            delay(TUTORIAL_STEP_ADVANCE_DELAY)
            hasReportedCompletion = true
            currentOnTutorialCompleted?.invoke()
        }
    }

    GameRoute(
        title = stringResource(R.string.tutorial_screen_title),
        initialPuzzle = currentScenario.initialPuzzle,
        modifier = modifier,
        gameSessionKey = "$TUTORIAL_GAME_SESSION_KEY:$playbackKey:${currentScenario.id}",
        puzzleResetKey = playbackKey to currentScenario.id,
        isSuccessOverlayEnabled = currentStepIndex == steps.lastIndex && onTutorialCompleted == null,
        isBoardVisible = currentStep.isBoardVisible,
        stripItemEntryGuidance = stripItemEntryGuidance,
        interactionPolicy = currentStep.toInteractionPolicy(
            scenario = currentScenario,
            uiState = latestGameUiState
        ),
        highlightState = currentStep.toHighlightState(
            scenario = currentScenario,
            uiState = latestGameUiState
        ),
        contentBeforePuzzle = {
            TutorialInstructionSurface(
                currentStep = currentStep,
                currentStepNumber = currentStepIndex + 1,
                totalSteps = steps.size,
                modifier = Modifier.fillMaxWidth()
            )
        },
        onGameUiStateChanged = { uiState ->
            latestGameUiSnapshot = TutorialGameUiSnapshot(
                scenarioId = currentScenario.id,
                uiState = uiState
            )
        },
        onNavigateBack = onNavigateBack
    )
}

private data class TutorialGameUiSnapshot(val scenarioId: TutorialScenarioId, val uiState: GameUiState)

@Composable
private fun TutorialInstructionSurface(
    currentStep: TutorialStep,
    currentStepNumber: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.testTag(TutorialScreenTestTags.INSTRUCTION_SURFACE),
        shape = NumPairsComponents.LargeShape,
        color = NumPairsComponents.raisedSurfaceColor(),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = NumPairsComponents.subtleBorder()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.tutorial_step_indicator,
                    currentStepNumber,
                    totalSteps
                ),
                modifier = Modifier.testTag(TutorialScreenTestTags.STEP_INDICATOR),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(currentStep.playerFacingCopyResId),
                modifier = Modifier.testTag(TutorialScreenTestTags.STEP_COPY),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

internal fun TutorialStep.toInteractionPolicy(
    scenario: TutorialScenario,
    uiState: GameUiState?
): GameInteractionPolicy = requiredAction.toInteractionPolicy(
    scenario = scenario,
    uiState = uiState,
    highlightedStripEntryIds = highlightedStripEntryIds(scenario = scenario)
)

private fun TutorialRequiredAction.toInteractionPolicy(
    scenario: TutorialScenario,
    uiState: GameUiState?,
    highlightedStripEntryIds: Set<Int>
): GameInteractionPolicy = when (this) {
    is TutorialRequiredAction.EnterStripValue -> GameInteractionPolicy(
        canTapStripItem = { index -> index == stripEntryIndex },
        canConfirmStripItemEntry = { index, value ->
            index == stripEntryIndex && value == this.value
        },
        canTapTileLeftOperand = { false },
        canTapTileRightOperand = { false },
        canTapTileOperator = { false },
        canTapTileReset = { false },
        canConfirmTileOperand = { _, _, _ -> false },
        canConfirmTileOperator = { _, _ -> false }
    )
    is TutorialRequiredAction.PlaceTileOperand -> toInteractionPolicy()
    is TutorialRequiredAction.CompleteTileExpression -> toInteractionPolicy(
        scenario = scenario,
        highlightedStripEntryIds = highlightedStripEntryIds
    )
    is TutorialRequiredAction.CompleteTileExpressions -> toInteractionPolicy(
        scenario = scenario,
        highlightedStripEntryIds = highlightedStripEntryIds
    )
    is TutorialRequiredAction.CompleteTileExpressionsInOrder -> toInteractionPolicy(
        scenario = scenario,
        uiState = uiState
    )
    TutorialRequiredAction.CompleteScenario -> GameInteractionPolicy.AllowAll
}

private fun TutorialRequiredAction.PlaceTileOperand.toInteractionPolicy(): GameInteractionPolicy =
    GameInteractionPolicy(
        canTapStripItem = { false },
        canConfirmStripItemEntry = { _, _ -> false },
        canTapTileLeftOperand = { index -> index == tileIndex && slot == OperandSlot.LEFT },
        canTapTileRightOperand = { index -> index == tileIndex && slot == OperandSlot.RIGHT },
        canTapTileOperator = { false },
        canTapTileReset = { false },
        canConfirmTileOperand = { index, selectedSlot, selectedStripEntryId ->
            index == tileIndex && selectedSlot == slot && selectedStripEntryId == stripEntryId
        },
        canConfirmTileOperator = { _, _ -> false }
    )

private fun TutorialRequiredAction.CompleteTileExpression.toInteractionPolicy(
    scenario: TutorialScenario,
    highlightedStripEntryIds: Set<Int>
): GameInteractionPolicy {
    val editableStripEntryIds = editableRequiredStripEntryIds(
        scenario = scenario,
        highlightedStripEntryIds = highlightedStripEntryIds
    )

    return GameInteractionPolicy(
        canTapStripItem = { index -> index in editableStripEntryIds },
        canConfirmStripItemEntry = { index, value ->
            index in editableStripEntryIds && scenario.stripValues.getOrNull(index) == value
        },
        canTapTileLeftOperand = { index -> index == tileIndex },
        canTapTileRightOperand = { index -> index == tileIndex },
        canTapTileOperator = { index -> index == tileIndex },
        canTapTileReset = { false },
        canConfirmTileOperand = { index, slot, stripEntryId ->
            index == tileIndex && stripEntryId == requiredStripEntryIdFor(slot = slot)
        },
        canConfirmTileOperator = { index, operator ->
            index == tileIndex && operator == this.operator
        }
    )
}

private fun TutorialRequiredAction.CompleteTileExpressions.toInteractionPolicy(
    scenario: TutorialScenario,
    highlightedStripEntryIds: Set<Int>
): GameInteractionPolicy {
    val editableStripEntryIds = expressions.flatMap { expression ->
        expression.editableRequiredStripEntryIds(
            scenario = scenario,
            highlightedStripEntryIds = highlightedStripEntryIds
        )
    }.toSet()

    return GameInteractionPolicy(
        canTapStripItem = { index -> index in editableStripEntryIds },
        canConfirmStripItemEntry = { index, value ->
            index in editableStripEntryIds && scenario.stripValues.getOrNull(index) == value
        },
        canTapTileLeftOperand = { index -> expressionFor(tileIndex = index) != null },
        canTapTileRightOperand = { index -> expressionFor(tileIndex = index) != null },
        canTapTileOperator = { index -> expressionFor(tileIndex = index) != null },
        canTapTileReset = { false },
        canConfirmTileOperand = { index, slot, stripEntryId ->
            expressionFor(tileIndex = index)?.requiredStripEntryIdFor(slot = slot) == stripEntryId
        },
        canConfirmTileOperator = { index, operator ->
            expressionFor(tileIndex = index)?.operator == operator
        }
    )
}

private fun TutorialRequiredAction.CompleteTileExpressionsInOrder.toInteractionPolicy(
    scenario: TutorialScenario,
    uiState: GameUiState?
): GameInteractionPolicy {
    val activeExpression = activeExpression(scenario = scenario, uiState = uiState)

    return activeExpression.toInteractionPolicy(
        scenario = scenario,
        highlightedStripEntryIds = activeExpression.hiddenRequiredStripEntryIds(
            scenario = scenario,
            uiState = uiState
        )
    )
}

private fun TutorialRequiredAction.CompleteTileExpressions.expressionFor(
    tileIndex: Int
): TutorialRequiredAction.CompleteTileExpression? = expressions.firstOrNull { expression ->
    expression.tileIndex == tileIndex
}

private fun TutorialRequiredAction.CompleteTileExpression.requiredStripEntryIdFor(slot: OperandSlot): Int =
    when (slot) {
        OperandSlot.LEFT -> leftStripEntryId
        OperandSlot.RIGHT -> rightStripEntryId
    }

private fun TutorialRequiredAction.CompleteTileExpression.editableRequiredStripEntryIds(
    scenario: TutorialScenario,
    highlightedStripEntryIds: Set<Int>
): Set<Int> = setOf(leftStripEntryId, rightStripEntryId).filterTo(mutableSetOf()) { stripEntryId ->
    stripEntryId in highlightedStripEntryIds &&
        scenario.initialPuzzle.strip.items.getOrNull(stripEntryId)?.isEditable == true
}

private val StripItem.isEditable: Boolean
    get() = this == StripItem.Hidden || this is StripItem.PlayerEntered

private fun TutorialRequiredAction.CompleteTileExpressionsInOrder.activeExpression(
    scenario: TutorialScenario,
    uiState: GameUiState?
): TutorialRequiredAction.CompleteTileExpression = expressions.firstOrNull { expression ->
    uiState == null || !expression.isSatisfiedBy(scenario = scenario, uiState = uiState)
} ?: expressions.last()

private fun TutorialRequiredAction.CompleteTileExpression.isSatisfiedBy(
    scenario: TutorialScenario,
    uiState: GameUiState
): Boolean {
    val tile = uiState.tiles.getOrNull(tileIndex) ?: return false
    val leftValue = scenario.stripValues.getOrNull(leftStripEntryId) ?: return false
    val rightValue = scenario.stripValues.getOrNull(rightStripEntryId) ?: return false

    return tile.leftOperandLabel == leftValue.toString() &&
        tile.operatorLabel == operator.symbol &&
        tile.rightOperandLabel == rightValue.toString()
}

private fun TutorialRequiredAction.CompleteTileExpression.hiddenRequiredStripEntryIds(
    scenario: TutorialScenario,
    uiState: GameUiState?
): Set<Int> = setOf(leftStripEntryId, rightStripEntryId).filterTo(mutableSetOf()) { stripEntryId ->
    scenario.initialPuzzle.strip.items.getOrNull(stripEntryId)?.isEditable == true &&
        uiState?.stripItems?.getOrNull(stripEntryId)?.visualStyle != StripItemVisualStyle.PLAYER_ENTERED
}

private fun TutorialStep.highlightedStripEntryIds(scenario: TutorialScenario): Set<Int> = buildSet {
    highlightedTargets.forEach { target ->
        when (target) {
            TutorialHighlightTarget.HiddenStripEntries -> {
                scenario.initialPuzzle.strip.items.forEachIndexed { index, item ->
                    if (item == StripItem.Hidden) {
                        add(index)
                    }
                }
            }
            is TutorialHighlightTarget.StripEntries -> addAll(target.indexes)
            TutorialHighlightTarget.GridArea,
            TutorialHighlightTarget.HiddenTileExpressions,
            TutorialHighlightTarget.StripArea,
            is TutorialHighlightTarget.TileExpressionSlots,
            is TutorialHighlightTarget.TileOperandSlot,
            is TutorialHighlightTarget.Tiles -> Unit
        }
    }
}

internal fun TutorialStep.toHighlightState(scenario: TutorialScenario, uiState: GameUiState?): GameHighlightState {
    val orderedAction = requiredAction as? TutorialRequiredAction.CompleteTileExpressionsInOrder

    if (orderedAction != null) {
        val activeExpression = orderedAction.activeExpression(scenario = scenario, uiState = uiState)

        return GameHighlightState(
            stripEntryIndexes = activeExpression.hiddenRequiredStripEntryIds(
                scenario = scenario,
                uiState = uiState
            ),
            tileExpressionSlots = expressionSlotHighlights(activeExpression.tileIndex)
        )
    }

    val stripEntryIndexes = mutableSetOf<Int>()
    val tileIndexes = mutableSetOf<Int>()
    val tileExpressionSlots = mutableSetOf<GameTileExpressionSlotHighlight>()

    highlightedTargets.forEach { target ->
        when (target) {
            TutorialHighlightTarget.GridArea,
            TutorialHighlightTarget.StripArea -> Unit
            TutorialHighlightTarget.HiddenStripEntries -> {
                scenario.initialPuzzle.strip.items.forEachIndexed { index, item ->
                    if (item == StripItem.Hidden) {
                        stripEntryIndexes += index
                    }
                }
            }
            TutorialHighlightTarget.HiddenTileExpressions -> {
                scenario.initialPuzzle.board.tiles.forEachIndexed { tileIndex, tile ->
                    if (!tile.expression.isFullyKnown) {
                        tileExpressionSlots += expressionSlotHighlights(tileIndex)
                    }
                }
            }
            is TutorialHighlightTarget.StripEntries -> {
                stripEntryIndexes += target.indexes
            }
            is TutorialHighlightTarget.TileExpressionSlots -> {
                tileExpressionSlots += expressionSlotHighlights(target.tileIndex)
            }
            is TutorialHighlightTarget.TileOperandSlot -> {
                tileExpressionSlots += GameTileExpressionSlotHighlight(
                    tileIndex = target.tileIndex,
                    slot = target.slot.toGameTileExpressionSlot()
                )
            }
            is TutorialHighlightTarget.Tiles -> {
                tileIndexes += target.indexes
            }
        }
    }

    return GameHighlightState(
        stripEntryIndexes = stripEntryIndexes,
        tileIndexes = tileIndexes,
        tileExpressionSlots = tileExpressionSlots
    )
}

private fun OperandSlot.toGameTileExpressionSlot(): GameTileExpressionSlot = when (this) {
    OperandSlot.LEFT -> GameTileExpressionSlot.LEFT_OPERAND
    OperandSlot.RIGHT -> GameTileExpressionSlot.RIGHT_OPERAND
}

private fun expressionSlotHighlights(tileIndex: Int): Set<GameTileExpressionSlotHighlight> = setOf(
    GameTileExpressionSlotHighlight(
        tileIndex = tileIndex,
        slot = GameTileExpressionSlot.LEFT_OPERAND
    ),
    GameTileExpressionSlotHighlight(
        tileIndex = tileIndex,
        slot = GameTileExpressionSlot.OPERATOR
    ),
    GameTileExpressionSlotHighlight(
        tileIndex = tileIndex,
        slot = GameTileExpressionSlot.RIGHT_OPERAND
    )
)

private const val TUTORIAL_GAME_SESSION_KEY = "tutorial-walkthrough"
private val TUTORIAL_STEP_ADVANCE_DELAY = 350.milliseconds
