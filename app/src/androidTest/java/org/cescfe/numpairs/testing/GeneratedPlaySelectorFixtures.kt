package org.cescfe.numpairs.testing

import org.cescfe.numpairs.feature.generated.GeneratedModes
import org.cescfe.numpairs.feature.generated.GeneratedPlayChallengeSelector

fun fourPairsQuickSelector(): GeneratedPlayChallengeSelector = GeneratedPlayChallengeSelector(
    challengeCatalog = GeneratedModes.catalog,
    quickBucketSource = { 99 }
)

fun threePairsQuickSelector(): GeneratedPlayChallengeSelector = GeneratedPlayChallengeSelector(
    challengeCatalog = GeneratedModes.catalog,
    quickBucketSource = { 0 }
)
