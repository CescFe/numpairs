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
    val particles = remember { dailyRecordConfettiParticles() }

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
                y = size.height * (-0.04f + (0.82f * particleProgress))
            )
            val particleWidth = baseWidth * particle.sizeScale
            val particleHeight = baseHeight * particle.sizeScale
            rotate(
                degrees = particle.rotationDegrees * particleProgress,
                pivot = center
            ) {
                drawRoundRect(
                    color = particleColors[index % particleColors.size],
                    topLeft = Offset(
                        x = center.x - (particleWidth / 2f),
                        y = center.y - (particleHeight / 2f)
                    ),
                    size = Size(particleWidth, particleHeight),
                    cornerRadius = CornerRadius(particleWidth / 3f),
                    alpha = (1f - particleProgress).coerceAtLeast(0f)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val startXFraction: Float,
    val horizontalDriftFraction: Float,
    val delayFraction: Float,
    val rotationDegrees: Float,
    val sizeScale: Float
)

private fun dailyRecordConfettiParticles(): List<ConfettiParticle> = List(CONFETTI_PARTICLE_COUNT) { index ->
    ConfettiParticle(
        startXFraction = 0.06f + (((index * 37) % 89) / 100f),
        horizontalDriftFraction = ((index % 5) - 2) * 0.025f,
        delayFraction = (index % 4) * 0.045f,
        rotationDegrees = 150f + ((index % 6) * 45f),
        sizeScale = 0.75f + ((index % 3) * 0.15f)
    )
}

private const val CONFETTI_PARTICLE_COUNT = 18
private val CONFETTI_PARTICLE_WIDTH = 5.dp
private val CONFETTI_PARTICLE_HEIGHT = 10.dp
