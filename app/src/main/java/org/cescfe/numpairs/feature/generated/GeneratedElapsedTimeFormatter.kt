package org.cescfe.numpairs.feature.generated

import org.cescfe.numpairs.domain.generated.GeneratedElapsedTime
import org.cescfe.numpairs.feature.time.ElapsedTimeFormatter

internal object GeneratedElapsedTimeFormatter {
    fun format(elapsedTime: GeneratedElapsedTime): String = ElapsedTimeFormatter.format(elapsedTime.milliseconds)
}
