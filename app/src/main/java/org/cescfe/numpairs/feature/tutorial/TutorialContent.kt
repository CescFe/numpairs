package org.cescfe.numpairs.feature.tutorial

object TutorialContent {
    val scenarios: List<TutorialScenario> = listOf(
        StageOneNumberPlacementContent.scenario,
        StageTwoComplementaryPairContent.scenario,
        LearnBasicsTutorialContent.scenario,
        SolvingTipsPracticeContent.scenario
    )

    val learnBasicsSteps: List<TutorialStep> = listOf(StageOneNumberPlacementContent.step) +
        StageTwoComplementaryPairContent.steps
    val solvingTipsPracticeSteps: List<TutorialStep> = SolvingTipsPracticeContent.steps
    val steps: List<TutorialStep> = learnBasicsSteps + solvingTipsPracticeSteps

    fun stepsFor(mode: TutorialMode): List<TutorialStep> = when (mode) {
        TutorialMode.LEARN_BASICS -> learnBasicsSteps
        TutorialMode.SOLVING_TIPS_PRACTICE -> solvingTipsPracticeSteps
    }

    fun scenario(id: TutorialScenarioId): TutorialScenario = scenarios.first { scenario -> scenario.id == id }
}
