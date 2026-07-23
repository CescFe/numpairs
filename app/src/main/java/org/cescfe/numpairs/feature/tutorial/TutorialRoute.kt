package org.cescfe.numpairs.feature.tutorial

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.puzzle.model.OperandSlot
import org.cescfe.numpairs.domain.puzzle.model.Puzzle
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
    startStepIndex: Int = 0,
    saveProgressAcrossRecreation: Boolean = true,
    onProgressCheckpointReached: suspend (TutorialProgressCheckpoint) -> Unit = {},
    onTutorialCompleted: (() -> Unit)? = null,
    onSkipTutorialRequested: (() -> Unit)? = null,
    onNavigateBack: () -> Unit = {}
) {
    val playbackKey = mode
    val steps = TutorialContent.stepsFor(mode)
    require(startStepIndex in steps.indices) {
        "Tutorial start step index must reference an available step."
    }
    val currentStepIndexState = if (saveProgressAcrossRecreation) {
        rememberSaveable(playbackKey, startStepIndex) { mutableIntStateOf(startStepIndex) }
    } else {
        remember(playbackKey, startStepIndex) { mutableIntStateOf(startStepIndex) }
    }
    val completionReportedState = if (saveProgressAcrossRecreation) {
        rememberSaveable(playbackKey) { mutableStateOf(false) }
    } else {
        remember(playbackKey) { mutableStateOf(false) }
    }
    var currentStepIndex by currentStepIndexState
    var hasReportedCompletion by completionReportedState
    var lastReportedStepIndex by remember(playbackKey, startStepIndex) {
        mutableIntStateOf(startStepIndex - 1)
    }
    var latestGameUiSnapshot by remember(playbackKey) { mutableStateOf<TutorialGameUiSnapshot?>(null) }
    var latestPuzzleSnapshot by remember(playbackKey) { mutableStateOf<TutorialPuzzleSnapshot?>(null) }
    var retainedPracticePuzzle by remember(playbackKey) { mutableStateOf<Puzzle?>(null) }
    var wasPracticeCueDismissed by remember(playbackKey) { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val currentOnProgressCheckpointReached by rememberUpdatedState(onProgressCheckpointReached)
    val currentOnTutorialCompleted by rememberUpdatedState(onTutorialCompleted)
    val currentStep = steps[currentStepIndex]
    val currentScenario = TutorialContent.scenario(currentStep.scenarioId)
    var isCheckpointNavigationInProgress by remember(playbackKey, currentStepIndex) {
        mutableStateOf(false)
    }
    var hasCurrentStepPuzzleChanged by remember(playbackKey, currentStepIndex) {
        mutableStateOf(
            currentStep.scenarioId == TutorialScenarioId.REPEATED_VALUE_PRACTICE &&
                wasPracticeCueDismissed
        )
    }
    val currentEntryPuzzle = currentStep.entryPuzzle
        ?: retainedPracticePuzzle.takeIf {
            currentStep.scenarioId == TutorialScenarioId.REPEATED_VALUE_PRACTICE
        }
        ?: currentScenario.initialPuzzle
    val latestGameUiState = latestGameUiSnapshot
        ?.takeIf { snapshot -> snapshot.scenarioId == currentScenario.id }
        ?.uiState
    val isManualAdvanceStep = currentStep.completionPredicate == TutorialStepCompletionPredicate.ManualAdvance
    val showPreviousStepAction = currentStepIndex > 0 &&
        (
            isManualAdvanceStep ||
                currentStep.scenarioId == TutorialScenarioId.REPEATED_VALUE_PRACTICE
            )

    LaunchedEffect(currentStepIndex, latestGameUiState) {
        val uiState = latestGameUiState ?: return@LaunchedEffect

        if (!currentStep.isComplete(uiState)) {
            return@LaunchedEffect
        }
        if (lastReportedStepIndex >= currentStepIndex) {
            return@LaunchedEffect
        }

        delay(TUTORIAL_STEP_ADVANCE_DELAY)
        lastReportedStepIndex = currentStepIndex
        currentStep.progressCheckpoint?.let { checkpoint ->
            currentOnProgressCheckpointReached(checkpoint)
        }
        if (currentStepIndex < steps.lastIndex) {
            currentStepIndex += 1
        } else if (!hasReportedCompletion && currentOnTutorialCompleted != null) {
            hasReportedCompletion = true
            currentOnTutorialCompleted?.invoke()
        }
    }

    GameRoute(
        title = stringResource(R.string.tutorial_screen_title),
        initialPuzzle = currentEntryPuzzle,
        modifier = modifier,
        gameSessionKey = "$TUTORIAL_GAME_SESSION_KEY:$playbackKey:${currentScenario.id}",
        puzzleResetKey = playbackKey to currentEntryPuzzle,
        isSuccessOverlayEnabled = isTutorialSuccessOverlayEnabled(
            mode = mode,
            isFinalStep = currentStepIndex == steps.lastIndex,
            currentScenarioId = currentScenario.id,
            observedScenarioId = latestGameUiSnapshot?.scenarioId,
            isRequiredPlayback = onTutorialCompleted != null
        ),
        isBoardVisible = currentStep.isBoardVisible,
        interactionPolicy = currentStep.toInteractionPolicy(
            scenario = currentScenario,
            uiState = latestGameUiState
        ),
        highlightState = currentStep.toHighlightState(
            scenario = currentScenario,
            uiState = latestGameUiState,
            hasPuzzleChanged = hasCurrentStepPuzzleChanged
        ),
        bottomBar = {
            onSkipTutorialRequested?.let { onSkipRequested ->
                TutorialSkipBottomBar(onSkipRequested = onSkipRequested)
            }
        },
        contentBeforePuzzle = {
            TutorialInstructionSurface(
                currentStep = currentStep,
                currentStepNumber = currentStepIndex + 1,
                totalSteps = steps.size,
                showNavigateBack = showPreviousStepAction,
                showNavigateNext = isManualAdvanceStep,
                canNavigateBack = showPreviousStepAction && !isCheckpointNavigationInProgress,
                canNavigateNext = !isCheckpointNavigationInProgress,
                onNavigateBack = {
                    if (currentStep.scenarioId == TutorialScenarioId.REPEATED_VALUE_PRACTICE) {
                        retainedPracticePuzzle = latestPuzzleSnapshot
                            ?.takeIf { snapshot -> snapshot.scenarioId == currentScenario.id }
                            ?.puzzle
                            ?: currentEntryPuzzle
                        wasPracticeCueDismissed = hasCurrentStepPuzzleChanged
                    }
                    currentStepIndex -= 1
                },
                onNavigateNext = {
                    val progressCheckpoint = currentStep.progressCheckpoint

                    if (progressCheckpoint == null && currentStepIndex < steps.lastIndex) {
                        currentStepIndex += 1
                    } else if (lastReportedStepIndex >= currentStepIndex && currentStepIndex < steps.lastIndex) {
                        currentStepIndex += 1
                    } else if (!isCheckpointNavigationInProgress && currentStepIndex < steps.lastIndex) {
                        isCheckpointNavigationInProgress = true
                        coroutineScope.launch {
                            currentOnProgressCheckpointReached(requireNotNull(progressCheckpoint))
                            lastReportedStepIndex = currentStepIndex
                            currentStepIndex += 1
                            isCheckpointNavigationInProgress = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        onGameUiStateChanged = { uiState ->
            latestGameUiSnapshot = TutorialGameUiSnapshot(
                scenarioId = currentScenario.id,
                uiState = uiState
            )
        },
        onPuzzleChanged = { puzzle ->
            latestPuzzleSnapshot = TutorialPuzzleSnapshot(
                scenarioId = currentScenario.id,
                puzzle = puzzle
            )
            if (puzzle != currentEntryPuzzle) {
                hasCurrentStepPuzzleChanged = true
            }
        },
        onNavigateBack = onNavigateBack
    )
}

private data class TutorialGameUiSnapshot(val scenarioId: TutorialScenarioId, val uiState: GameUiState)

private data class TutorialPuzzleSnapshot(val scenarioId: TutorialScenarioId, val puzzle: Puzzle)

internal fun isTutorialSuccessOverlayEnabled(
    mode: TutorialMode,
    isFinalStep: Boolean,
    currentScenarioId: TutorialScenarioId,
    observedScenarioId: TutorialScenarioId?,
    isRequiredPlayback: Boolean
): Boolean = mode == TutorialMode.SOLVING_TIPS_PRACTICE &&
    isFinalStep &&
    observedScenarioId == currentScenarioId &&
    !isRequiredPlayback

@Composable
private fun TutorialInstructionSurface(
    currentStep: TutorialStep,
    currentStepNumber: Int,
    totalSteps: Int,
    showNavigateBack: Boolean,
    showNavigateNext: Boolean,
    canNavigateBack: Boolean,
    canNavigateNext: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
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
            if (showNavigateBack || showNavigateNext) {
                TutorialStepNavigation(
                    showNavigateBack = showNavigateBack,
                    showNavigateNext = showNavigateNext,
                    canNavigateBack = canNavigateBack,
                    canNavigateNext = canNavigateNext,
                    onNavigateBack = onNavigateBack,
                    onNavigateNext = onNavigateNext
                )
            }
        }
    }
}

@Composable
private fun TutorialStepNavigation(
    showNavigateBack: Boolean,
    showNavigateNext: Boolean,
    canNavigateBack: Boolean,
    canNavigateNext: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = when {
            showNavigateBack && showNavigateNext -> Arrangement.SpaceBetween
            showNavigateBack -> Arrangement.Start
            else -> Arrangement.End
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showNavigateBack) {
            TextButton(
                onClick = onNavigateBack,
                enabled = canNavigateBack,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(TutorialScreenTestTags.PREVIOUS_STEP_ACTION)
            ) {
                Text(
                    text = stringResource(R.string.tutorial_previous_step_action),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        if (showNavigateNext) {
            TextButton(
                onClick = onNavigateNext,
                enabled = canNavigateNext,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag(TutorialScreenTestTags.NEXT_STEP_ACTION)
            ) {
                Text(
                    text = stringResource(R.string.tutorial_next_step_action),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun TutorialSkipBottomBar(onSkipRequested: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = stringResource(R.string.onboarding_skip_tutorial_action),
            modifier = Modifier
                .testTag(TutorialScreenTestTags.SKIP_ACTION)
                .clickable(
                    role = Role.Button,
                    onClick = onSkipRequested
                )
                .padding(vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
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
    TutorialRequiredAction.NoInteraction -> noInteractionPolicy()
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

private fun noInteractionPolicy(): GameInteractionPolicy = GameInteractionPolicy(
    canTapStripItem = { false },
    canConfirmStripItemEntry = { _, _ -> false },
    canTapTileLeftOperand = { false },
    canTapTileRightOperand = { false },
    canTapTileOperator = { false },
    canTapTileReset = { false },
    canConfirmTileOperand = { _, _, _ -> false },
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
            TutorialHighlightTarget.WholeStrip -> Unit
            TutorialHighlightTarget.HiddenStripEntries -> {
                scenario.initialPuzzle.strip.items.forEachIndexed { index, item ->
                    if (item == StripItem.Hidden) {
                        add(index)
                    }
                }
            }
            is TutorialHighlightTarget.StripEntries -> addAll(target.indexes)
            TutorialHighlightTarget.HiddenTileExpressions,
            is TutorialHighlightTarget.TileExpressionSlots,
            is TutorialHighlightTarget.WholeTile -> Unit
        }
    }
}

internal fun TutorialStep.toHighlightState(
    scenario: TutorialScenario,
    uiState: GameUiState?,
    hasPuzzleChanged: Boolean = false
): GameHighlightState {
    if (dismissHighlightsAfterFirstPuzzleChange && hasPuzzleChanged) {
        return GameHighlightState.None
    }

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
    var isStripHighlighted = false

    highlightedTargets.forEach { target ->
        when (target) {
            TutorialHighlightTarget.WholeStrip -> {
                isStripHighlighted = true
            }
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
            is TutorialHighlightTarget.WholeTile -> {
                tileIndexes += target.tileIndex
            }
        }
    }

    return GameHighlightState(
        isStripHighlighted = isStripHighlighted,
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
