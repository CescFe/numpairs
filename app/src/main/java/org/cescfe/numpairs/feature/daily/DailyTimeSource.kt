package org.cescfe.numpairs.feature.daily

import org.cescfe.numpairs.feature.time.ElapsedTimeReading
import org.cescfe.numpairs.feature.time.ElapsedTimeSource
import org.cescfe.numpairs.feature.time.SystemElapsedTimeSource

typealias DailyTimeReading = ElapsedTimeReading
typealias DailyTimeSource = ElapsedTimeSource

object SystemDailyTimeSource : DailyTimeSource by SystemElapsedTimeSource
