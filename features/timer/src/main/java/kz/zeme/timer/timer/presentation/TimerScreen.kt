package kz.zeme.timer.timer.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
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
import kz.zeme.timer.core.timer.domain.TimerTimeFormatter
import kz.zeme.timer.core.ui.components.DefaultErrorScreen
import kz.zeme.timer.core.ui.extensions.compose.clickableHaptic
import kz.zeme.timer.core.ui.extensions.compose.clickableWithoutRipple
import kz.zeme.timer.core.ui.extensions.shimmer

private const val TABULAR_NUMS_FEATURE = "tnum"
private const val PROGRESS_ANIMATION_MS = 120
private const val FINISHED_PULSE_HALF_DURATION_MS = 550
private const val RING_COLOR_ANIMATION_MS = 250
private const val MODE_ENTER_DURATION_MS = 320
private const val MODE_EXIT_DURATION_MS = 220
private const val ACTIONS_ENTER_DURATION_MS = 220
private const val ACTIONS_EXIT_DURATION_MS = 160
private const val WHEEL_COLOR_ANIMATION_MS = 120
private const val WHEEL_VISIBLE_COUNT = 5
private const val WHEEL_VIRTUAL_COUNT = 24_000
private const val WHEEL_VIRTUAL_MIDDLE = 12_000
private val WHEEL_ITEM_HEIGHT = 48.dp
private val WHEEL_VIEWPORT_HEIGHT = WHEEL_ITEM_HEIGHT * WHEEL_VISIBLE_COUNT
private val WHEEL_VERTICAL_PADDING = WHEEL_ITEM_HEIGHT * (WHEEL_VISIBLE_COUNT / 2)

private data class ActiveCountdownSnapshot(
    val status: TimerStatus = TimerStatus.Running,
    val formattedRemaining: String = "05:00",
    val progress: Float = 1f,
    val endsAtClock: String = "",
)

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
    var activeSnapshot by remember {
        mutableStateOf(
            ActiveCountdownSnapshot(
                status = if (data.status == TimerStatus.Idle) TimerStatus.Running else data.status,
                formattedRemaining = data.formattedRemaining,
                progress = data.progress,
                endsAtClock = data.endsAtClock,
            ),
        )
    }

    if (data.status != TimerStatus.Idle) activeSnapshot = ActiveCountdownSnapshot(
        status = data.status,
        formattedRemaining = data.formattedRemaining,
        progress = data.progress,
        endsAtClock = data.endsAtClock,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        AnimatedContent(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            targetState = data.status == TimerStatus.Idle,
            transitionSpec = {
                val enterScale = if (targetState) 1.06f else 0.92f
                val exitScale = if (targetState) 0.92f else 1.06f

                (
                    fadeIn(tween(MODE_ENTER_DURATION_MS, easing = FastOutSlowInEasing)) +
                        scaleIn(
                            initialScale = enterScale,
                            animationSpec = tween(MODE_ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                        )
                    ) togetherWith (
                    fadeOut(tween(MODE_EXIT_DURATION_MS, easing = FastOutSlowInEasing)) +
                        scaleOut(
                            targetScale = exitScale,
                            animationSpec = tween(MODE_EXIT_DURATION_MS, easing = FastOutSlowInEasing),
                        )
                    ) using SizeTransform(clip = false)
            },
            label = "timerModeTransition",
        ) { isIdle ->
            if (isIdle) TimerWheelPicker(
                modifier = Modifier.fillMaxSize(),
                hoursItems = data.hoursItems,
                minutesItems = data.minutesItems,
                secondsItems = data.secondsItems,
                selectedHours = data.selectedHours,
                selectedMinutes = data.selectedMinutes,
                selectedSeconds = data.selectedSeconds,
                controller = controller,
            )
            else TimerCountdownRing(
                modifier = Modifier.fillMaxSize(),
                status = activeSnapshot.status,
                formattedRemaining = activeSnapshot.formattedRemaining,
                progress = activeSnapshot.progress,
                endsAtClock = activeSnapshot.endsAtClock,
            )
        }

        TimerActionsBar(
            status = data.status,
            canStart = data.canStart,
            controller = controller,
        )
    }
}

@Composable
private fun TimerWheelPicker(
    hoursItems: ImmutableList<String>,
    minutesItems: ImmutableList<String>,
    secondsItems: ImmutableList<String>,
    selectedHours: Int,
    selectedMinutes: Int,
    selectedSeconds: Int,
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
                    items = hoursItems,
                    selectedValue = selectedHours,
                    onValueSelected = controller::onHoursSelected,
                )
                WheelColon()
                TimerWheelColumnView(
                    modifier = Modifier.weight(1f),
                    items = minutesItems,
                    selectedValue = selectedMinutes,
                    onValueSelected = controller::onMinutesSelected,
                )
                WheelColon()
                TimerWheelColumnView(
                    modifier = Modifier.weight(1f),
                    items = secondsItems,
                    selectedValue = selectedSeconds,
                    onValueSelected = controller::onSecondsSelected,
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
    items: ImmutableList<String>,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val count = items.size
    val middleBase = WHEEL_VIRTUAL_MIDDLE - (WHEEL_VIRTUAL_MIDDLE % count)
    val initialIndex = middleBase + selectedValue.coerceIn(0, count - 1)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapFlingBehavior = rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center,
    )
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val itemHeightPx = with(LocalDensity.current) { WHEEL_ITEM_HEIGHT.toPx() }
    val currentSelectedValue by rememberUpdatedState(selectedValue)
    val currentOnValueSelected by rememberUpdatedState(onValueSelected)

    val centeredIndex by remember(listState, initialIndex, itemHeightPx) {
        derivedStateOf {
            listState.findCenteredIndexOrNull(itemHeightPx) ?: initialIndex
        }
    }

    LaunchedEffect(listState, count, itemHeightPx) {
        snapshotFlow { listState.findCenteredIndexOrNull(itemHeightPx) }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { index ->
                val resolvedValue = Math.floorMod(index, count)
                if (resolvedValue != currentSelectedValue) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    currentOnValueSelected(resolvedValue)
                }
            }
    }

    LaunchedEffect(selectedValue, count, itemHeightPx) {
        if (listState.isScrollInProgress) return@LaunchedEffect

        val currentCenter = listState.findCenteredIndexOrNull(itemHeightPx) ?: initialIndex
        if (Math.floorMod(currentCenter, count) != selectedValue)
            listState.scrollToItem(middleBase + selectedValue.coerceIn(0, count - 1))
    }

    LazyColumn(
        modifier = modifier.height(WHEEL_VIEWPORT_HEIGHT),
        state = listState,
        contentPadding = PaddingValues(vertical = WHEEL_VERTICAL_PADDING),
        flingBehavior = snapFlingBehavior,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        timerWheelItemsSection(
            items = items,
            listState = listState,
            initialIndex = initialIndex,
            centeredIndexProvider = { centeredIndex },
            itemHeightPx = itemHeightPx,
            onItemClick = { virtualIndex ->
                coroutineScope.launch {
                    listState.animateScrollToItem(virtualIndex)
                }
            },
        )
    }
}

private fun LazyListScope.timerWheelItemsSection(
    items: ImmutableList<String>,
    listState: LazyListState,
    initialIndex: Int,
    centeredIndexProvider: () -> Int,
    itemHeightPx: Float,
    onItemClick: (Int) -> Unit,
) {
    val count = items.size
    items(count = WHEEL_VIRTUAL_COUNT) { virtualIndex ->
        TimerWheelItem(
            label = items[virtualIndex % count],
            virtualIndex = virtualIndex,
            initialIndex = initialIndex,
            listState = listState,
            centeredIndexProvider = centeredIndexProvider,
            itemHeightPx = itemHeightPx,
            onClick = { onItemClick(virtualIndex) },
        )
    }
}

@Composable
private fun TimerWheelItem(
    label: String,
    virtualIndex: Int,
    initialIndex: Int,
    listState: LazyListState,
    centeredIndexProvider: () -> Int,
    itemHeightPx: Float,
    onClick: () -> Unit,
) {
    val isCentered by remember(virtualIndex) {
        derivedStateOf { centeredIndexProvider() == virtualIndex }
    }
    val textColor by animateColorAsState(
        targetValue = if (isCentered) TimerTheme.colors.textPrimary else TimerTheme.colors.textSecondary,
        animationSpec = tween(durationMillis = WHEEL_COLOR_ANIMATION_MS),
        label = "wheelItemColor",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WHEEL_ITEM_HEIGHT)
            .clickableWithoutRipple(onClick = onClick)
            .graphicsLayer {
                val distance = listState.getDistanceFromCenter(
                    virtualIndex = virtualIndex,
                    initialIndex = initialIndex,
                    fallbackItemHeightPx = itemHeightPx,
                )
                val scale = when {
                    distance <= 1f -> lerp(1f, 0.52f, distance)
                    distance <= 2f -> lerp(0.52f, 0.36f, distance - 1f)
                    else -> 0.36f
                }
                val itemAlpha = when {
                    distance <= 1f -> lerp(1f, 0.42f, distance)
                    distance <= 2f -> lerp(0.42f, 0.18f, distance - 1f)
                    distance <= 2.5f -> lerp(0.18f, 0f, (distance - 2f) * 2f)
                    else -> 0f
                }

                scaleX = scale
                scaleY = scale
                alpha = itemAlpha
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.wrapContentHeight(unbounded = true),
            text = label,
            style = TimerTheme.typography.titleLarge.copy(
                color = textColor,
                fontSize = 50.sp,
                lineHeight = 52.sp,
                fontWeight = if (isCentered) FontWeight.Medium else FontWeight.Normal,
                letterSpacing = (-0.02).em,
                fontFeatureSettings = TABULAR_NUMS_FEATURE,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            ),
        )
    }
}

private fun LazyListState.findCenteredIndexOrNull(
    fallbackItemHeightPx: Float,
): Int? {
    val info = layoutInfo
    val visibleItems = info.visibleItemsInfo
    if (visibleItems.isEmpty()) return null

    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
    val anchor = visibleItems[0]
    val itemSize = if (anchor.size > 0) anchor.size.toFloat() else fallbackItemHeightPx
    if (itemSize <= 0f) return anchor.index

    val centerOffset = (viewportCenter - anchor.offset - itemSize / 2f) / itemSize
    return anchor.index + centerOffset.roundToInt()
}

private fun LazyListState.getDistanceFromCenter(
    virtualIndex: Int,
    initialIndex: Int,
    fallbackItemHeightPx: Float,
): Float {
    val info = layoutInfo
    val visibleItems = info.visibleItemsInfo
    if (visibleItems.isEmpty()) return abs(virtualIndex - initialIndex).toFloat()

    val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2f
    val anchor = visibleItems[0]
    val itemSize = if (anchor.size > 0) anchor.size.toFloat() else fallbackItemHeightPx
    if (itemSize <= 0f) return abs(virtualIndex - initialIndex).toFloat()

    val itemCenter = anchor.offset + (virtualIndex - anchor.index) * itemSize + itemSize / 2f
    return abs(itemCenter - viewportCenter) / itemSize
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
    val animatedStrokeColor by animateColorAsState(
        targetValue = if (status == TimerStatus.Paused) TimerTheme.colors.basicGray else TimerTheme.colors.textPrimary,
        animationSpec = tween(durationMillis = RING_COLOR_ANIMATION_MS, easing = FastOutSlowInEasing),
        label = "timerRingColor",
    )
    val progressColor =
        if (status == TimerStatus.Finished) animatedStrokeColor.copy(alpha = ringAlpha)
        else animatedStrokeColor

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
    AnimatedContent(
        modifier = modifier.fillMaxWidth(),
        targetState = status,
        transitionSpec = {
            (
                fadeIn(tween(ACTIONS_ENTER_DURATION_MS, easing = FastOutSlowInEasing)) +
                    scaleIn(
                        initialScale = 0.96f,
                        animationSpec = tween(ACTIONS_ENTER_DURATION_MS, easing = FastOutSlowInEasing),
                    )
                ) togetherWith (
                fadeOut(tween(ACTIONS_EXIT_DURATION_MS, easing = FastOutSlowInEasing)) +
                    scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(ACTIONS_EXIT_DURATION_MS, easing = FastOutSlowInEasing),
                    )
                ) using SizeTransform(clip = false)
        },
        label = "timerActionsTransition",
    ) { targetStatus ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (targetStatus) {
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
    val actualContainerColor =
        if (enabled) containerColor
        else TimerTheme.colors.buttonDisabled
    val actualContentColor =
        if (enabled) contentColor
        else TimerTheme.colors.textPrimary.copy(alpha = ContentAlpha.disabled)

    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(actualContainerColor)
            .clickableHaptic(
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TimerTheme.typography.bodyLarge.copy(
                color = actualContentColor,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

private val timerControllerStub = object : TimerController {
    override fun onStart() = Unit
    override fun onRefresh() = Unit
    override fun onHoursSelected(hours: Int) = Unit
    override fun onMinutesSelected(minutes: Int) = Unit
    override fun onSecondsSelected(seconds: Int) = Unit
    override fun onStartClick() = Unit
    override fun onPauseClick() = Unit
    override fun onResumeClick() = Unit
    override fun onResetClick() = Unit
    override fun onRepeatClick() = Unit
    override fun onDoneClick() = Unit
}

private val timerPreviewFormatter = TimerTimeFormatter()

private val timerIdleStateStub = TimerState(
    status = TimerStatus.Idle,
    hoursItems = timerPreviewFormatter.hoursWheelItems,
    minutesItems = timerPreviewFormatter.minutesWheelItems,
    secondsItems = timerPreviewFormatter.secondsWheelItems,
    selectedHours = 0,
    selectedMinutes = 5,
    selectedSeconds = 0,
)

private val timerRunningStateStub = TimerState(
    status = TimerStatus.Running,
    hoursItems = timerPreviewFormatter.hoursWheelItems,
    minutesItems = timerPreviewFormatter.minutesWheelItems,
    secondsItems = timerPreviewFormatter.secondsWheelItems,
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
            state = ViewState.Data(timerIdleStateStub),
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
