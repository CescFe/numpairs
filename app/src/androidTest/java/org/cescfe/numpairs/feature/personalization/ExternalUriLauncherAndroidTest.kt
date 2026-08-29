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
class ExternalUriLauncherAndroidTest {
    @Test
    fun intentFactoryCreatesExternalViewRequestsForBothCanonicalUrls() {
        listOf(
            OPEN_SOURCE_REPOSITORY_URL,
            PRIVACY_POLICY_URL
        ).forEach { uri ->
            val intent = ExternalUriIntentFactory.create(uri)

            assertEquals(Intent.ACTION_VIEW, intent.action)
            assertEquals(uri, intent.dataString)
            assertEquals("https", intent.data?.scheme)
        }
    }

    @Test
    fun launcherAddsNewTaskForNonActivityContextAndReportsSuccess() {
        val context = RecordingExternalUriContext(
            base = InstrumentationRegistry.getInstrumentation().targetContext
        )

        val result = AndroidExternalUriLauncher(context).launch(OPEN_SOURCE_REPOSITORY_URL)

        assertSame(ExternalUriLaunchResult.Launched, result)
        assertEquals(OPEN_SOURCE_REPOSITORY_URL, context.startedIntent?.dataString)
        assertTrue(
            requireNotNull(context.startedIntent).flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0
        )
    }

    @Test
    fun unavailableExternalHandlerReturnsTypedFailureWithoutCrashing() {
        val context = RecordingExternalUriContext(
            base = InstrumentationRegistry.getInstrumentation().targetContext,
            failure = ActivityNotFoundException("No browser")
        )

        val result = AndroidExternalUriLauncher(context).launch(OPEN_SOURCE_REPOSITORY_URL)

        assertSame(ExternalUriLaunchResult.Unavailable, result)
    }
}

private class RecordingExternalUriContext(base: Context, private val failure: ActivityNotFoundException? = null) :
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
