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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.OperandSlot
import org.cescfe.numpairs.domain.puzzle.StripItem
import org.cescfe.numpairs.feature.game.GameHighlightState
import org.cescfe.numpairs.feature.game.GameInteractionPolicy
import org.cescfe.numpairs.feature.game.GameRoute
import org.cescfe.numpairs.feature.game.GameTileExpressionSlot
import org.cescfe.numpairs.feature.game.GameTileExpressionSlotHighlight
import org.cescfe.numpairs.feature.game.presentation.GameUiState
import org.cescfe.numpairs.feature.tutorial.ui.TutorialScreenTestTags

@Composable
fun TutorialRoute(modifier: Modifier = Modifier, onNavigateBack: () -> Unit = {}) {
    val steps = TutorialMvpContent.steps
    var currentStepIndex by rememberSaveable { mutableIntStateOf(0) }
    var latestGameUiState by remember { mutableStateOf<GameUiState?>(null) }
    val currentStep = steps[currentStepIndex]
    val currentScenario = TutorialMvpContent.scenario(currentStep.scenarioId)

    LaunchedEffect(currentStepIndex, latestGameUiState) {
        val uiState = latestGameUiState ?: return@LaunchedEffect

        if (currentStepIndex < steps.lastIndex && currentStep.isComplete(uiState)) {
            delay(TUTORIAL_STEP_ADVANCE_DELAY)
            currentStepIndex = (currentStepIndex + 1).coerceAtMost(steps.lastIndex)
        }
    }

    GameRoute(
        title = stringResource(R.string.tutorial_screen_title),
        initialPuzzle = currentScenario.initialPuzzle,
        modifier = modifier,
        gameSessionKey = "$TUTORIAL_GAME_SESSION_KEY:${currentScenario.id}",
        puzzleResetKey = currentScenario.id,
        isSuccessOverlayEnabled = currentStep.scenarioId == TutorialScenarioId.FINAL_EASY_FOUR_PAIRS,
        interactionPolicy = currentStep.requiredAction.toInteractionPolicy(),
        highlightState = currentStep.toHighlightState(scenario = currentScenario),
        contentBeforePuzzle = {
            TutorialInstructionSurface(
                currentStep = currentStep,
                totalSteps = steps.size,
                modifier = Modifier.fillMaxWidth()
            )
        },
        onGameUiStateChanged = { uiState ->
            latestGameUiState = uiState
        },
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun TutorialInstructionSurface(currentStep: TutorialStep, totalSteps: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.testTag(TutorialScreenTestTags.INSTRUCTION_SURFACE),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.tutorial_step_indicator,
                    currentStep.order,
                    totalSteps
                ),
                modifier = Modifier.testTag(TutorialScreenTestTags.STEP_INDICATOR),
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = stringResource(currentStep.playerFacingCopyResId),
                modifier = Modifier.testTag(TutorialScreenTestTags.STEP_COPY),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun TutorialRequiredAction.toInteractionPolicy(): GameInteractionPolicy = when (this) {
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
    is TutorialRequiredAction.CompleteTileExpression -> GameInteractionPolicy(
        canTapStripItem = { false },
        canConfirmStripItemEntry = { _, _ -> false },
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
    TutorialRequiredAction.CompleteScenario -> GameInteractionPolicy.AllowAll
}

private fun TutorialRequiredAction.CompleteTileExpression.requiredStripEntryIdFor(slot: OperandSlot): Int =
    when (slot) {
        OperandSlot.LEFT -> leftStripEntryId
        OperandSlot.RIGHT -> rightStripEntryId
    }

private fun TutorialStep.toHighlightState(scenario: TutorialScenario): GameHighlightState {
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
