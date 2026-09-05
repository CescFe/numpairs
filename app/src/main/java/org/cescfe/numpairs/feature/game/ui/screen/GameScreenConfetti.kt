package org.cescfe.numpairs.feature.game.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import org.cescfe.numpairs.ui.theme.numPairsSemanticColors

@Composable
internal fun PersonalRecordConfetti(celebrationId: Long?, animationEnabled: Boolean, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(celebrationId, animationEnabled) {
        if (celebrationId == null || !animationEnabled) {
            isVisible = false
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        progress.snapTo(0f)
        isVisible = true
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = PERSONAL_RECORD_CONFETTI_DURATION_MILLIS,
                easing = FastOutSlowInEasing
            )
        )
        isVisible = false
    }

    if (!isVisible) {
        return
    }

    val semanticColors = MaterialTheme.numPairsSemanticColors
    val primaryColor = MaterialTheme.colorScheme.primary
    val particleColors = remember(
        semanticColors.record,
        semanticColors.onRecordContainer,
        primaryColor
    ) {
        listOf(
            semanticColors.record,
            semanticColors.onRecordContainer,
            primaryColor
        )
    }
    val particles = remember { personalRecordConfettiParticles() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clearAndSetSemantics {}
            .testTag(GameScreenTestTags.SUCCESS_OVERLAY_CONFETTI)
    ) {
        val baseWidth = CONFETTI_PARTICLE_WIDTH.toPx()
        val baseHeight = CONFETTI_PARTICLE_HEIGHT.toPx()
        particles.forEachIndexed { index, particle ->
            val particleProgress = (
                (progress.value - particle.delayFraction) /
                    (1f - particle.delayFraction)
                ).coerceIn(0f, 1f)
            if (particleProgress <= 0f || particleProgress >= 1f) {
                return@forEachIndexed
            }

            val center = Offset(
                x = size.width * (
                    particle.startXFraction +
                        (particle.horizontalDriftFraction * particleProgress)
                    ),
                y = confettiVerticalCenter(
                    viewportHeight = size.height,
                    particleHeight = baseHeight,
                    progress = particleProgress
                )
            )
            rotate(
                degrees = particle.rotationDegrees * particleProgress,
                pivot = center
            ) {
                drawRoundRect(
                    color = particleColors[index % particleColors.size],
                    topLeft = Offset(
                        x = center.x - (baseWidth / 2f),
                        y = center.y - (baseHeight / 2f)
                    ),
                    size = Size(baseWidth, baseHeight),
                    cornerRadius = CornerRadius(baseWidth / 3f),
                    alpha = personalRecordConfettiAlpha(particleProgress)
                )
            }
        }
    }
}

internal data class ConfettiParticle(
    val startXFraction: Float,
    val horizontalDriftFraction: Float,
    val delayFraction: Float,
    val rotationDegrees: Float
)

internal fun personalRecordConfettiParticles(): List<ConfettiParticle> = List(CONFETTI_PARTICLE_COUNT) { index ->
    ConfettiParticle(
        startXFraction = 0.06f + (((index * 37) % 89) / 100f),
        horizontalDriftFraction = ((index % 5) - 2) * (CONFETTI_MAX_HORIZONTAL_DRIFT_FRACTION / 2f),
        delayFraction = (index % 4) * 0.045f,
        rotationDegrees = 150f + ((index % 6) * 45f)
    )
}

internal fun personalRecordConfettiAlpha(progress: Float): Float =
    ((1f - progress.coerceIn(0f, 1f)) / (1f - CONFETTI_OPAQUE_TRAVEL_FRACTION))
        .coerceIn(0f, 1f)

internal fun confettiVerticalCenter(viewportHeight: Float, particleHeight: Float, progress: Float): Float =
    (-particleHeight / 2f) +
        ((viewportHeight + particleHeight) * progress.coerceIn(0f, 1f))

internal const val CONFETTI_PARTICLE_COUNT = 36
internal val CONFETTI_PARTICLE_WIDTH = 6.dp
internal val CONFETTI_PARTICLE_HEIGHT = 12.dp
internal const val CONFETTI_OPAQUE_TRAVEL_FRACTION = 0.6f
internal const val CONFETTI_MAX_HORIZONTAL_DRIFT_FRACTION = 0.08f
