package org.cescfe.numpairs.feature.daily.share

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.core.os.BundleCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import org.cescfe.numpairs.R
import org.cescfe.numpairs.domain.daily.DailyCompletion
import org.cescfe.numpairs.domain.daily.DailyElapsedTime
import org.cescfe.numpairs.domain.daily.DailyMovementCount
import org.cescfe.numpairs.feature.daily.DailyRecipes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DailyCompletionShareAndroidTest {
    @Test
    fun payload_factory_uses_localized_timed_daily_completion_copy_with_movements() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )

        val payload = AndroidDailyCompletionSharePayloadFactory(
            resources = context.resources
        ).create(
            completion = DailyCompletion(
                identity = identity,
                elapsedTime = DailyElapsedTime(125_999),
                movementCount = DailyMovementCount(23)
            )
        )
        val formattedMovements = context.resources.getQuantityString(
            R.plurals.daily_movement_count,
            2,
            "23"
        )

        assertTrue(payload.text.value.startsWith(context.getString(R.string.daily_share_name)))
        assertTrue(
            payload.text.value.contains(
                context.getString(
                    R.string.generated_challenge_title,
                    context.getString(R.string.four_pairs_screen_title),
                    context.getString(R.string.generated_difficulty_low)
                )
            )
        )
        assertTrue(
            payload.text.value.endsWith(
                context.getString(
                    R.string.daily_share_completed_in_status,
                    "02:05 · $formattedMovements"
                )
            )
        )
        assertEquals(
            context.getString(R.string.daily_share_chooser_title),
            payload.chooserTitle
        )
    }

    @Test
    fun payload_factory_keeps_timed_copy_when_movement_count_is_unknown() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )

        val payload = AndroidDailyCompletionSharePayloadFactory(
            resources = context.resources
        ).create(
            completion = DailyCompletion(
                identity = identity,
                elapsedTime = DailyElapsedTime(125_999),
                movementCount = null
            )
        )

        assertTrue(
            payload.text.value.endsWith(
                context.getString(R.string.daily_share_completed_in_status, "02:05")
            )
        )
    }

    @Test
    fun payload_factory_keeps_legacy_completion_copy_without_inventing_a_duration() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val identity = DailyRecipes.FOUR_PAIRS_LOW_V1.identityFor(
            LocalDate.of(2026, 7, 25)
        )

        val payload = AndroidDailyCompletionSharePayloadFactory(
            resources = context.resources
        ).create(
            completion = DailyCompletion(
                identity = identity,
                elapsedTime = null
            )
        )

        assertTrue(
            payload.text.value.endsWith(
                context.getString(R.string.daily_share_completed_status)
            )
        )
        assertFalse(payload.text.value.contains("00:00"))
    }

    @Test
    fun chooser_contains_one_exact_plain_text_send_intent_without_attachments() {
        val payload = DailyCompletionSharePayload(
            text = DailyCompletionShareText(
                "NumPairs Daily · Jul 25, 2026\n4 Pairs · Low · Completed"
            ),
            chooserTitle = "Share Daily result"
        )

        val chooserIntent = DailyCompletionShareIntentFactory.create(payload)
        val sendIntent = requireNotNull(
            BundleCompat.getParcelable(
                chooserIntent.extras ?: error("Chooser extras missing."),
                Intent.EXTRA_INTENT,
                Intent::class.java
            )
        )

        assertEquals(Intent.ACTION_CHOOSER, chooserIntent.action)
        assertEquals(payload.chooserTitle, chooserIntent.getStringExtra(Intent.EXTRA_TITLE))
        assertEquals(Intent.ACTION_SEND, sendIntent.action)
        assertEquals(DAILY_SHARE_MIME_TYPE, sendIntent.type)
        assertEquals(payload.text.value, sendIntent.getStringExtra(Intent.EXTRA_TEXT))
        assertFalse(sendIntent.hasExtra(Intent.EXTRA_STREAM))
        assertEquals(null, sendIntent.clipData)
        assertFalse(sendIntent.hasExtra(Intent.EXTRA_HTML_TEXT))
    }

    @Test
    fun launcher_adds_new_task_for_non_activity_context_and_reports_success() {
        val context = RecordingShareContext(
            base = InstrumentationRegistry.getInstrumentation().targetContext
        )
        val payload = testPayload()

        val result = AndroidDailyCompletionShareLauncher(context).launch(payload)

        assertSame(DailyCompletionShareLaunchResult.Launched, result)
        assertTrue(requireNotNull(context.startedIntent).flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun unavailable_chooser_is_typed_and_does_not_crash() {
        val context = RecordingShareContext(
            base = InstrumentationRegistry.getInstrumentation().targetContext,
            failure = ActivityNotFoundException("No chooser")
        )

        val result = AndroidDailyCompletionShareLauncher(context).launch(testPayload())

        assertSame(DailyCompletionShareLaunchResult.Unavailable, result)
    }
}

private class RecordingShareContext(base: Context, private val failure: ActivityNotFoundException? = null) :
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

private fun testPayload(): DailyCompletionSharePayload = DailyCompletionSharePayload(
    text = DailyCompletionShareText(
        "NumPairs Daily · Jul 25, 2026\n4 Pairs · Low · Completed"
    ),
    chooserTitle = "Share Daily result"
)
