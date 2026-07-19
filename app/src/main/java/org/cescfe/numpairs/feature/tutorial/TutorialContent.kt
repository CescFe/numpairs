package org.cescfe.numpairs.feature.tutorial

object TutorialContent {
    val scenarios: List<TutorialScenario> = LearnBasicsTutorialContent.scenarios +
        listOf(
            StageOneNumberPlacementContent.scenario,
            StageTwoComplementaryPairContent.scenario,
            StageThreeHiddenStripValueContent.scenario,
            FinalValidationContent.scenario,
            SolvingTipsPracticeContent.scenario
        )

    val learnBasicsSteps: List<TutorialStep> = LearnBasicsTutorialContent.steps
    val solvingTipsPracticeSteps: List<TutorialStep> = SolvingTipsPracticeContent.steps
    val steps: List<TutorialStep> = learnBasicsSteps + solvingTipsPracticeSteps

    fun stepsFor(mode: TutorialMode): List<TutorialStep> = when (mode) {
        TutorialMode.LEARN_BASICS -> learnBasicsSteps
        TutorialMode.SOLVING_TIPS_PRACTICE -> solvingTipsPracticeSteps
    }

    fun stepsFor(stage: GuidedOnboardingStage): List<TutorialStep> = when (stage) {
        GuidedOnboardingStage.NUMBER_PLACEMENT -> listOf(StageOneNumberPlacementContent.step)
        GuidedOnboardingStage.COMPLEMENTARY_PAIR -> StageTwoComplementaryPairContent.steps
        GuidedOnboardingStage.HIDDEN_STRIP_VALUE -> listOf(StageThreeHiddenStripValueContent.step)
    }

    fun scenario(id: TutorialScenarioId): TutorialScenario = scenarios.first { scenario -> scenario.id == id }
}
