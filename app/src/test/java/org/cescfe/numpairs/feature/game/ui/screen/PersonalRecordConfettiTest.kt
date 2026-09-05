package org.cescfe.numpairs.feature.game.ui.screen

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalRecordConfettiTest {
    @Test
    fun configuration_matches_the_shared_personal_record_effect() {
        val particles = personalRecordConfettiParticles()

        assertEquals(1_500, PERSONAL_RECORD_CONFETTI_DURATION_MILLIS)
        assertEquals(36, particles.size)
        assertEquals(6.dp, CONFETTI_PARTICLE_WIDTH)
        assertEquals(12.dp, CONFETTI_PARTICLE_HEIGHT)
        assertEquals(0.6f, CONFETTI_OPAQUE_TRAVEL_FRACTION)
        assertEquals(0.08f, CONFETTI_MAX_HORIZONTAL_DRIFT_FRACTION)
        assertTrue(
            particles.all { particle ->
                particle.horizontalDriftFraction in
                    -CONFETTI_MAX_HORIZONTAL_DRIFT_FRACTION..CONFETTI_MAX_HORIZONTAL_DRIFT_FRACTION
            }
        )
        assertTrue(
            particles.any { particle ->
                kotlin.math.abs(particle.horizontalDriftFraction) ==
                    CONFETTI_MAX_HORIZONTAL_DRIFT_FRACTION
            }
        )
    }

    @Test
    fun particles_stay_opaque_for_sixty_percent_then_fade_smoothly() {
        assertEquals(1f, personalRecordConfettiAlpha(0f), FLOAT_TOLERANCE)
        assertEquals(1f, personalRecordConfettiAlpha(0.6f), FLOAT_TOLERANCE)
        assertEquals(0.5f, personalRecordConfettiAlpha(0.8f), FLOAT_TOLERANCE)
        assertEquals(0f, personalRecordConfettiAlpha(1f), FLOAT_TOLERANCE)
    }

    @Test
    fun particles_travel_from_above_to_below_the_full_viewport() {
        val viewportHeight = 800f
        val particleHeight = 12f

        assertEquals(
            -particleHeight / 2f,
            confettiVerticalCenter(viewportHeight, particleHeight, progress = 0f),
            FLOAT_TOLERANCE
        )
        assertEquals(
            viewportHeight + (particleHeight / 2f),
            confettiVerticalCenter(viewportHeight, particleHeight, progress = 1f),
            FLOAT_TOLERANCE
        )
    }

    private companion object {
        const val FLOAT_TOLERANCE = 0.0001f
    }
}
