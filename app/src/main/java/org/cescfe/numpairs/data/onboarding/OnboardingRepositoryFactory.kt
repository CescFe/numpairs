package org.cescfe.numpairs.data.onboarding

import android.content.Context

fun createOnboardingRepository(context: Context): OnboardingRepository = DataStoreOnboardingRepository(
    context.applicationContext.onboardingPreferencesDataStore
)
