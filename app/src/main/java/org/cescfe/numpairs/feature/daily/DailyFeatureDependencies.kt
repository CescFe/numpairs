package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.data.daily.session.DailySessionRepository
import org.cescfe.numpairs.domain.daily.DeviceLocalDateSource
import org.cescfe.numpairs.feature.generated.GeneratedPuzzleGenerationUseCaseFactory

data class DailyFeatureDependencies(
    val dailySessionRepository: DailySessionRepository,
    val deviceLocalDateSource: DeviceLocalDateSource,
    val generatedPuzzleGenerationUseCaseFactory: GeneratedPuzzleGenerationUseCaseFactory
)
