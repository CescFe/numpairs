package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.domain.daily.DailyMovementCount

internal object DailyMovementCountFormatter {
    fun format(movementCount: DailyMovementCount): String = movementCount.value.toString()
}

internal object DailyCompletionResultFormatter {
    private const val METRIC_SEPARATOR = " · "

    fun format(formattedElapsedTime: String?, formattedMovementCount: String?): String? =
        listOfNotNull(formattedElapsedTime, formattedMovementCount)
            .joinToString(separator = METRIC_SEPARATOR)
            .ifEmpty { null }
}

internal fun DailyMovementCount.pluralQuantity(): Int = if (value == 1L) 1 else 2
