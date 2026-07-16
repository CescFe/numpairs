package org.cescfe.numpairs.data.onboarding

class OnboardingInitializer(
    private val repository: OnboardingRepository,
    private val preV6UpgradeMarker: PreV6UpgradeMarker
) {
    suspend fun initialize() {
        val isPreV6Upgrade = preV6UpgradeMarker.isMarked()
        repository.initialize(
            installationKind = if (isPreV6Upgrade) {
                OnboardingInstallationKind.PRE_V6_UPGRADE
            } else {
                OnboardingInstallationKind.FRESH_INSTALL
            }
        )

        if (isPreV6Upgrade) {
            preV6UpgradeMarker.clear()
        }
    }
}

interface PreV6UpgradeMarker {
    fun isMarked(): Boolean

    fun mark()

    fun clear()
}
