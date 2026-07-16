package org.cescfe.numpairs.data.onboarding

import android.content.Context

data class OnboardingRuntime(val repository: OnboardingRepository, val initializer: OnboardingInitializer)

fun createOnboardingRuntime(context: Context): OnboardingRuntime {
    val applicationContext = context.applicationContext
    val repository = DataStoreOnboardingRepository(applicationContext.onboardingPreferencesDataStore)

    return OnboardingRuntime(
        repository = repository,
        initializer = OnboardingInitializer(
            repository = repository,
            preV6UpgradeMarker = applicationContext.preV6UpgradeMarker()
        )
    )
}
