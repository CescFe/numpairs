package org.cescfe.numpairs.feature.personalization

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacyPolicyLauncherAndroidTest {
    @Test
    fun intentFactoryCreatesAnExternalViewRequestForTheCanonicalPolicyUrl() {
        val intent = PrivacyPolicyIntentFactory.create()

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(PRIVACY_POLICY_URL, intent.dataString)
        assertEquals("https", intent.data?.scheme)
    }

    @Test
    fun launcherAddsNewTaskForNonActivityContextAndReportsSuccess() {
        val context = RecordingPrivacyPolicyContext(
            base = InstrumentationRegistry.getInstrumentation().targetContext
        )

        val result = AndroidPrivacyPolicyLauncher(context).launch()

        assertSame(PrivacyPolicyLaunchResult.Launched, result)
        assertEquals(PRIVACY_POLICY_URL, context.startedIntent?.dataString)
        assertTrue(
            requireNotNull(context.startedIntent).flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0
        )
    }

    @Test
    fun unavailableExternalHandlerReturnsTypedFailureWithoutCrashing() {
        val context = RecordingPrivacyPolicyContext(
            base = InstrumentationRegistry.getInstrumentation().targetContext,
            failure = ActivityNotFoundException("No browser")
        )

        val result = AndroidPrivacyPolicyLauncher(context).launch()

        assertSame(PrivacyPolicyLaunchResult.Unavailable, result)
    }
}

private class RecordingPrivacyPolicyContext(base: Context, private val failure: ActivityNotFoundException? = null) :
    ContextWrapper(base) {
    var startedIntent: Intent? = null
        private set

    override fun startActivity(intent: Intent) {
        failure?.let { throwable ->
            throw throwable
        }
        startedIntent = intent
    }
}
