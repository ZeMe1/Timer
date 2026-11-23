package kz.zeme.timer.timer.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kz.zeme.timer.core.error.BaseError
import kz.zeme.timer.core.error.UnexpectedError
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.res.R
import kz.zeme.timer.core.res.theme.ContentAlpha
import kz.zeme.timer.core.res.theme.TimerTheme
import kz.zeme.timer.core.res.theme.align
import kz.zeme.timer.core.res.theme.medium
import kz.zeme.timer.core.res.theme.primary
import kz.zeme.timer.core.res.theme.secondary
import kz.zeme.timer.core.timer.domain.TimerStatus
import kz.zeme.timer.core.ui.components.DefaultErrorScreen
import kz.zeme.timer.core.ui.extensions.compose.clickableWithoutRipple
import kz.zeme.timer.core.ui.extensions.shimmer
import kz.zeme.timer.timer.presentation.TimerState.WheelColumn

private const val TABULAR_NUMS_FEATURE = "tnum"
private const val PROGRESS_ANIMATION_MS = 120
private const val FINISHED_PULSE_HALF_DURATION_MS = 550

@Composable
internal fun TimerScreen(
    state: State<ViewState<TimerState>?>,
    controller: TimerController,
) {
    TimerScreen(
        viewState = state.value,
        controller = controller,
    )
}

@Composable
private fun TimerScreen(
    viewState: ViewState<TimerState>?,
    controller: TimerController,
) {
    when (viewState) {
        null,
        is ViewState.Loading.Skeleton -> TimerShimmerView()

        is ViewState.Error -> DefaultErrorView(
            error = viewState.error,
            onRefresh = controller::onRefresh,
        )

        is ViewState.Loading.ProgressBar,
        is ViewState.Data -> viewState.data?.let {
            TimerContent(
                data = it,
                controller = controller,
            )
        } ?: TimerShimmerView()
    }
}

@Composable
private fun DefaultErrorView(
    modifier: Modifier = Modifier,
    error: BaseError? = null,
    onRefresh: () -> Unit,
) {
    DefaultErrorScreen(
        modifier = modifier,
        errorMessage = error?.message ?: stringResource(R.string.shared_something_went_wrong),
        onRefresh = onRefresh,
    )
}

@Composable
private fun TimerShimmerView(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                repeat(3) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Spacer(
                            modifier = Modifier
                                .width(36.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TimerTheme.colors.mainShimmer)
                                .shimmer(),
                        )
                        Spacer(
                            modifier = Modifier
                                .width(68.dp)
                                .height(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TimerTheme.colors.mainShimmer)
                                .shimmer(),
                        )
                        Spacer(
                            modifier = Modifier
                                .width(36.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TimerTheme.colors.mainShimmer)
                                .shimmer(),
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TimerTheme.colors.mainShimmer)
                .shimmer(),
        )
    }
}

@Composable
private fun TimerContent(
    data: TimerState,
    controller: TimerController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        if (data.status == TimerStatus.Idle) TimerWheelPicker(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            hoursWheel = data.hoursWheel,
            minutesWheel = data.minutesWheel,
            secondsWheel = data.secondsWheel,
            controller = controller,
        )
        else TimerCountdownRing(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            status = data.status,
            formattedRemaining = data.formattedRemaining,
            progress = data.progress,
            endsAtClock = data.endsAtClock,
        )

        TimerActionsBar(
            status = data.status,
            canStart = data.canStart,
            controller = controller,
        )
    }
}

@Composable
private fun TimerWheelPicker(
    hoursWheel: WheelColumn,
    minutesWheel: WheelColumn,
    secondsWheel: WheelColumn,
    controller: TimerController,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WheelUnitLabel(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_hours_short),
                )
                Spacer(modifier = Modifier.width(20.dp))
                WheelUnitLabel(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_minutes_short),
                )
                Spacer(modifier = Modifier.width(20.dp))
                WheelUnitLabel(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_seconds_short),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimerWheelColumnView(
                    modifier = Modifier.weight(1f),
                    column = hoursWheel,
                    onDelta = controller::onHoursDelta,
                )
                WheelColon()
                TimerWheelColumnView(
                    modifier = Modifier.weight(1f),
                    column = minutesWheel,
                    onDelta = controller::onMinutesDelta,
                )
                WheelColon()
                TimerWheelColumnView(
                    modifier = Modifier.weight(1f),
                    column = secondsWheel,
                    onDelta = controller::onSecondsDelta,
                )
            }
        }
    }
}

@Composable
private fun WheelUnitLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        style = TimerTheme.typography.labelSmall.secondary()
            .align(TextAlign.Center)
            .copy(letterSpacing = 0.08.em),
    )
}

@Composable
private fun WheelColon() {
    Text(
        modifier = Modifier
            .width(20.dp)
            .alpha(0.6f),
        text = ":",
        style = TimerTheme.typography.titleLarge.secondary()
            .align(TextAlign.Center)
            .copy(
                fontSize = 38.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Normal,
            ),
    )
}

@Composable
private fun TimerWheelColumnView(
    column: WheelColumn,
    onDelta: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val stepThresholdPx = with(LocalDensity.current) { 36.dp.toPx() }
    val currentOnDelta by rememberUpdatedState(onDelta)
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier.pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragEnd = { accumulatedDrag = 0f },
                onDragCancel = { accumulatedDrag = 0f },
            ) { change, dragAmount ->
                change.consume()
                accumulatedDrag += dragAmount
                val steps = (-accumulatedDrag / stepThresholdPx).toInt()
                if (steps != 0) {
                    accumulatedDrag += steps * stepThresholdPx
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    currentOnDelta(steps)
                }
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        WheelStepText(
            text = column.prev2,
            height = 30.dp,
            fontSize = 18.sp,
            alpha = 0.18f,
            onClick = { onDelta(-2) },
        )
        WheelStepText(
            text = column.prev1,
            height = 40.dp,
            fontSize = 26.sp,
            alpha = 0.42f,
            onClick = { onDelta(-1) },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = column.current,
                style = TimerTheme.typography.titleLarge.primary().copy(
                    fontSize = 50.sp,
                    lineHeight = 56.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.02).em,
                    fontFeatureSettings = TABULAR_NUMS_FEATURE,
                ),
            )
        }
        WheelStepText(
            text = column.next1,
            height = 40.dp,
            fontSize = 26.sp,
            alpha = 0.42f,
            onClick = { onDelta(1) },
        )
        WheelStepText(
            text = column.next2,
            height = 30.dp,
            fontSize = 18.sp,
            alpha = 0.18f,
            onClick = { onDelta(2) },
        )
    }
}

@Composable
private fun WheelStepText(
    text: String,
    height: Dp,
    fontSize: TextUnit,
    alpha: Float,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clickableWithoutRipple {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.alpha(alpha),
            text = text,
            style = TimerTheme.typography.bodyLarge.secondary().copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Normal,
                fontFeatureSettings = TABULAR_NUMS_FEATURE,
            ),
        )
    }
}

@Composable
private fun TimerCountdownRing(
    status: TimerStatus,
    formattedRemaining: String,
    progress: Float,
    endsAtClock: String,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = PROGRESS_ANIMATION_MS,
            easing = LinearEasing,
        ),
        label = "timerRingProgress",
    )
    val ringAlpha =
        if (status == TimerStatus.Finished) rememberFinishedRingAlpha()
        else 1f
    val trackColor = TimerTheme.colors.buttonDisabled
    val progressColor = when (status) {
        TimerStatus.Paused -> TimerTheme.colors.basicGray
        TimerStatus.Finished -> TimerTheme.colors.textPrimary.copy(alpha = ringAlpha)
        else -> TimerTheme.colors.textPrimary
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(272.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidthPx = 3.5.dp.toPx()
                val inset = strokeWidthPx / 2f
                val arcSize = Size(
                    width = size.width - strokeWidthPx,
                    height = size.height - strokeWidthPx,
                )

                drawCircle(
                    color = trackColor,
                    radius = (size.minDimension - strokeWidthPx) / 2f,
                    style = Stroke(width = strokeWidthPx),
                )

                val sweepAngle = 360f * animatedProgress
                if (sweepAngle > 0f) drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                    ),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = formattedRemaining,
                    style = TimerTheme.typography.titleLarge.primary().copy(
                        fontSize = 48.sp,
                        lineHeight = 54.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = (-0.025).em,
                        fontFeatureSettings = TABULAR_NUMS_FEATURE,
                    ),
                )

                TimerRingSubtitle(
                    status = status,
                    endsAtClock = endsAtClock,
                )
            }
        }
    }
}

@Composable
private fun rememberFinishedRingAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "timerFinishedPulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = FINISHED_PULSE_HALF_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "timerFinishedAlpha",
    )
    return alpha
}

@Composable
private fun TimerRingSubtitle(
    status: TimerStatus,
    endsAtClock: String,
) {
    val text = when (status) {
        TimerStatus.Running -> stringResource(R.string.timer_ends_at, endsAtClock)
        TimerStatus.Paused -> stringResource(R.string.timer_paused)
        TimerStatus.Finished -> stringResource(R.string.timer_time_up)
        TimerStatus.Idle -> ""
    }
    val baseStyle =
        if (status == TimerStatus.Finished) TimerTheme.typography.bodySmall.medium().primary()
        else TimerTheme.typography.bodySmall.secondary()

    Text(
        text = text,
        style = baseStyle.copy(
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontFeatureSettings = TABULAR_NUMS_FEATURE,
        ),
    )
}

@Composable
private fun TimerActionsBar(
    status: TimerStatus,
    canStart: Boolean,
    controller: TimerController,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (status) {
            TimerStatus.Idle -> TimerActionButton(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.timer_start),
                containerColor = TimerTheme.colors.textPrimary,
                contentColor = TimerTheme.colors.mainBg,
                enabled = canStart,
                onClick = controller::onStartClick,
            )

            TimerStatus.Running -> {
                TimerActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_reset),
                    containerColor = TimerTheme.colors.buttonSecondary,
                    contentColor = TimerTheme.colors.textPrimary,
                    onClick = controller::onResetClick,
                )
                TimerActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_pause),
                    containerColor = TimerTheme.colors.buttonPrimary,
                    contentColor = TimerTheme.colors.textPrimary,
                    onClick = controller::onPauseClick,
                )
            }

            TimerStatus.Paused -> {
                TimerActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_reset),
                    containerColor = TimerTheme.colors.buttonSecondary,
                    contentColor = TimerTheme.colors.textPrimary,
                    onClick = controller::onResetClick,
                )
                TimerActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_resume),
                    containerColor = TimerTheme.colors.textPrimary,
                    contentColor = TimerTheme.colors.mainBg,
                    onClick = controller::onResumeClick,
                )
            }

            TimerStatus.Finished -> {
                TimerActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_repeat),
                    containerColor = TimerTheme.colors.buttonSecondary,
                    contentColor = TimerTheme.colors.textPrimary,
                    onClick = controller::onRepeatClick,
                )
                TimerActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_done),
                    containerColor = TimerTheme.colors.textPrimary,
                    contentColor = TimerTheme.colors.mainBg,
                    onClick = controller::onDoneClick,
                )
            }
        }
    }
}

@Composable
private fun TimerActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val haptic = LocalHapticFeedback.current
    Button(
        modifier = modifier.height(54.dp),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = TimerTheme.colors.buttonDisabled,
            disabledContentColor = TimerTheme.colors.textPrimary.copy(alpha = ContentAlpha.disabled),
        ),
    ) {
        Text(
            text = text,
            style = TimerTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

private val timerControllerStub = object : TimerController {
    override fun onStart() = Unit
    override fun onRefresh() = Unit
    override fun onHoursDelta(delta: Int) = Unit
    override fun onMinutesDelta(delta: Int) = Unit
    override fun onSecondsDelta(delta: Int) = Unit
    override fun onStartClick() = Unit
    override fun onPauseClick() = Unit
    override fun onResumeClick() = Unit
    override fun onResetClick() = Unit
    override fun onRepeatClick() = Unit
    override fun onDoneClick() = Unit
}

private val timerRunningStateStub = TimerState(
    status = TimerStatus.Running,
    formattedRemaining = "03:18",
    progress = 0.66f,
    endsAtClock = "22:05",
)

private data class TimerScreenPreviewParameter(
    val state: ViewState<TimerState>,
)

private class TimerScreenPreviewParameterProvider :
    PreviewParameterProvider<TimerScreenPreviewParameter> {

    override val values: Sequence<TimerScreenPreviewParameter> = sequenceOf(
        TimerScreenPreviewParameter(
            state = ViewState.Data(timerRunningStateStub),
        ),
        TimerScreenPreviewParameter(
            state = ViewState.Data(TimerState()),
        ),
        TimerScreenPreviewParameter(
            state = ViewState.Loading.Skeleton(),
        ),
        TimerScreenPreviewParameter(
            state = ViewState.Error(UnexpectedError()),
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF010101,
)
@Composable
private fun TimerScreenPreview(
    @PreviewParameter(TimerScreenPreviewParameterProvider::class)
    parameter: TimerScreenPreviewParameter,
) {
    TimerScreen(
        viewState = parameter.state,
        controller = timerControllerStub,
    )
}
