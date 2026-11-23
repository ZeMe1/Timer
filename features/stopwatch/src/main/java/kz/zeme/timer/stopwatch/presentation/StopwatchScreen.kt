package kz.zeme.timer.stopwatch.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kz.zeme.timer.core.error.BaseError
import kz.zeme.timer.core.error.UnexpectedError
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.res.R
import kz.zeme.timer.core.res.theme.TimerTheme
import kz.zeme.timer.core.res.theme.align
import kz.zeme.timer.core.res.theme.medium
import kz.zeme.timer.core.res.theme.primary
import kz.zeme.timer.core.res.theme.secondary
import kz.zeme.timer.core.stopwatch.domain.StopwatchStatus
import kz.zeme.timer.core.ui.components.DefaultErrorScreen
import kz.zeme.timer.core.ui.extensions.compose.clickableHaptic
import kz.zeme.timer.core.ui.extensions.shimmer
import kz.zeme.timer.stopwatch.presentation.StopwatchState.StopwatchLapItem

private const val TABULAR_NUMS_FEATURE = "tnum"
private const val LAPS_EXPAND_DURATION_MS = 320
private const val LAP_ITEM_FADE_DURATION_MS = 220
private const val LAP_CAPTION_ENTER_MS = 220
private const val LAP_CAPTION_EXIT_MS = 150
private const val ACTIONS_ENTER_DURATION_MS = 220
private const val ACTIONS_EXIT_DURATION_MS = 160

@Composable
internal fun StopwatchScreen(
    state: State<ViewState<StopwatchState>?>,
    controller: StopwatchController,
) {
    StopwatchScreen(
        viewState = state.value,
        controller = controller,
    )
}

@Composable
private fun StopwatchScreen(
    viewState: ViewState<StopwatchState>?,
    controller: StopwatchController,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    when (viewState) {
        null,
        is ViewState.Loading.Skeleton -> StopwatchShimmerView()

        is ViewState.Error -> DefaultErrorView(
            error = viewState.error,
            onRefresh = controller::onRefresh,
        )

        is ViewState.Loading.ProgressBar,
        is ViewState.Data -> viewState.data?.let {
            StopwatchContent(
                data = it,
                controller = controller,
                lazyListState = lazyListState,
            )
        } ?: StopwatchShimmerView()
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
private fun StopwatchShimmerView(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(
                    modifier = Modifier
                        .width(164.dp)
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TimerTheme.colors.mainShimmer)
                        .shimmer(),
                )
                Spacer(
                    modifier = Modifier
                        .width(56.dp)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TimerTheme.colors.mainShimmer)
                        .shimmer(),
                )
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
private fun StopwatchContent(
    data: StopwatchState,
    controller: StopwatchController,
    lazyListState: LazyListState,
) {
    val hasLaps = data.laps.isNotEmpty()
    val expandProgress by animateFloatAsState(
        targetValue = if (hasLaps) 1f else 0f,
        animationSpec = tween(
            durationMillis = LAPS_EXPAND_DURATION_MS,
            easing = FastOutSlowInEasing,
        ),
        label = "stopwatchLapsExpand",
    )
    val dialTopPadding by animateDpAsState(
        targetValue = if (hasLaps) 28.dp else 0.dp,
        animationSpec = tween(
            durationMillis = LAPS_EXPAND_DURATION_MS,
            easing = FastOutSlowInEasing,
        ),
        label = "stopwatchDialTopPadding",
    )
    val dialBottomPadding by animateDpAsState(
        targetValue = if (hasLaps) 16.dp else 0.dp,
        animationSpec = tween(
            durationMillis = LAPS_EXPAND_DURATION_MS,
            easing = FastOutSlowInEasing,
        ),
        label = "stopwatchDialBottomPadding",
    )
    val slideOffsetPx = with(LocalDensity.current) { 24.dp.toPx() }
    var renderedLaps by remember { mutableStateOf(data.laps) }

    if (hasLaps) renderedLaps = data.laps
    else if (expandProgress <= 0.01f && renderedLaps.isNotEmpty()) renderedLaps = persistentListOf()

    val topWeight = (1f - expandProgress) * 0.5f
    val bottomWeight = 1f - topWeight

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (topWeight > 0.001f) Spacer(modifier = Modifier.weight(topWeight))

            StopwatchDial(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dialTopPadding, bottom = dialBottomPadding),
                mainTime = data.mainTime,
                centiseconds = data.centiseconds,
                currentLapNumber = data.currentLapNumber,
                currentLapTime = data.currentLapTime,
            )

            Box(
                modifier = Modifier
                    .weight(bottomWeight)
                    .fillMaxWidth(),
            ) {
                if (expandProgress > 0.01f && renderedLaps.isNotEmpty()) StopwatchLapsList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = 16.dp)
                        .graphicsLayer {
                            alpha = expandProgress
                            translationY = (1f - expandProgress) * slideOffsetPx
                        },
                    laps = renderedLaps,
                    lazyListState = lazyListState,
                )
            }
        }

        StopwatchActionsBar(
            status = data.status,
            controller = controller,
        )
    }
}

@Composable
private fun StopwatchDial(
    mainTime: String,
    centiseconds: String,
    currentLapNumber: String?,
    currentLapTime: String?,
    modifier: Modifier = Modifier,
) {
    val hasLapCaption = currentLapNumber != null && currentLapTime != null
    var lastLapNumber by remember { mutableStateOf(currentLapNumber) }
    var lastLapTime by remember { mutableStateOf(currentLapTime) }

    if (hasLapCaption) {
        lastLapNumber = currentLapNumber
        lastLapTime = currentLapTime
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.alignByBaseline(),
                text = mainTime,
                style = TimerTheme.typography.titleLarge.primary().copy(
                    fontSize = 56.sp,
                    lineHeight = 62.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.025).em,
                    fontFeatureSettings = TABULAR_NUMS_FEATURE,
                ),
            )
            Text(
                modifier = Modifier
                    .alignByBaseline()
                    .padding(start = 2.dp),
                text = centiseconds,
                style = TimerTheme.typography.titleLarge.secondary().copy(
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Normal,
                    fontFeatureSettings = TABULAR_NUMS_FEATURE,
                ),
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .heightIn(min = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            StopwatchLapCaption(
                visible = hasLapCaption,
                lapNumber = lastLapNumber,
                lapTime = lastLapTime,
            )
        }
    }
}

@Composable
private fun StopwatchLapCaption(
    visible: Boolean,
    lapNumber: String?,
    lapTime: String?,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(LAP_CAPTION_ENTER_MS, easing = FastOutSlowInEasing)),
        exit = fadeOut(tween(LAP_CAPTION_EXIT_MS, easing = FastOutSlowInEasing)),
    ) {
        if (lapNumber != null && lapTime != null) Text(
            text = stringResource(
                R.string.stopwatch_lap_live,
                lapNumber,
                lapTime,
            ),
            style = TimerTheme.typography.bodySmall.secondary().copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontFeatureSettings = TABULAR_NUMS_FEATURE,
            ),
        )
    }
}

@Composable
private fun StopwatchLapsList(
    laps: ImmutableList<StopwatchLapItem>,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
    val latestLapNumber = laps.firstOrNull()?.number
    LaunchedEffect(latestLapNumber) {
        if (latestLapNumber != null && lazyListState.firstVisibleItemIndex > 0)
            lazyListState.animateScrollToItem(0)
    }

    Column(modifier = modifier) {
        HorizontalDivider(
            color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
            thickness = 1.dp,
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
        ) {
            stopwatchLapsSection(laps = laps)
        }
    }
}

private fun LazyListScope.stopwatchLapsSection(
    laps: ImmutableList<StopwatchLapItem>,
) {
    items(
        items = laps,
        key = { it.number },
    ) { lap ->
        Column(
            modifier = Modifier.animateItem(
                fadeInSpec = tween(
                    durationMillis = LAP_ITEM_FADE_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
                placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                fadeOutSpec = tween(
                    durationMillis = LAP_ITEM_FADE_DURATION_MS,
                    easing = FastOutSlowInEasing,
                ),
            ),
        ) {
            StopwatchLapRow(lap = lap)
            HorizontalDivider(
                color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
                thickness = 1.dp,
            )
        }
    }
}

@Composable
private fun StopwatchLapRow(
    lap: StopwatchLapItem,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.width(88.dp),
            text = stringResource(R.string.stopwatch_lap_item, lap.numberText),
            style = TimerTheme.typography.bodyMedium.medium().secondary().copy(
                fontFeatureSettings = TABULAR_NUMS_FEATURE,
            ),
        )
        Text(
            modifier = Modifier.weight(1f),
            text = lap.deltaText,
            style = TimerTheme.typography.bodyMedium.secondary()
                .align(TextAlign.Center)
                .copy(fontFeatureSettings = TABULAR_NUMS_FEATURE),
        )
        Text(
            text = lap.totalText,
            style = TimerTheme.typography.bodyLarge.primary().copy(
                fontFeatureSettings = TABULAR_NUMS_FEATURE,
            ),
        )
    }
}

@Composable
private fun StopwatchActionsBar(
    status: StopwatchStatus,
    controller: StopwatchController,
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
        label = "stopwatchActionsTransition",
    ) { targetStatus ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (targetStatus) {
                StopwatchStatus.Idle -> StopwatchActionButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.timer_start),
                    containerColor = TimerTheme.colors.textPrimary,
                    contentColor = TimerTheme.colors.mainBg,
                    onClick = controller::onStartClick,
                )

                StopwatchStatus.Running -> {
                    StopwatchActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.stopwatch_lap),
                        containerColor = TimerTheme.colors.buttonSecondary,
                        contentColor = TimerTheme.colors.textPrimary,
                        onClick = controller::onLapClick,
                    )
                    StopwatchActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.timer_pause),
                        containerColor = TimerTheme.colors.buttonPrimary,
                        contentColor = TimerTheme.colors.textPrimary,
                        onClick = controller::onPauseClick,
                    )
                }

                StopwatchStatus.Paused -> {
                    StopwatchActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.timer_reset),
                        containerColor = TimerTheme.colors.buttonSecondary,
                        contentColor = TimerTheme.colors.textPrimary,
                        onClick = controller::onResetClick,
                    )
                    StopwatchActionButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.timer_resume),
                        containerColor = TimerTheme.colors.textPrimary,
                        contentColor = TimerTheme.colors.mainBg,
                        onClick = controller::onResumeClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun StopwatchActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickableHaptic(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TimerTheme.typography.bodyLarge.copy(
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

private val stopwatchControllerStub = object : StopwatchController {
    override fun onStart() = Unit
    override fun onRefresh() = Unit
    override fun onStartClick() = Unit
    override fun onPauseClick() = Unit
    override fun onResumeClick() = Unit
    override fun onResetClick() = Unit
    override fun onLapClick() = Unit
}

private val stopwatchStateStub = StopwatchState(
    status = StopwatchStatus.Running,
    mainTime = "00:42",
    centiseconds = ".31",
    currentLapNumber = "04",
    currentLapTime = "00:04.87",
    laps = persistentListOf(
        StopwatchLapItem(
            number = 3,
            numberText = "03",
            deltaText = "+00:15.84",
            totalText = "00:37.44",
        ),
        StopwatchLapItem(
            number = 2,
            numberText = "02",
            deltaText = "+00:09.12",
            totalText = "00:21.60",
        ),
        StopwatchLapItem(
            number = 1,
            numberText = "01",
            deltaText = "+00:12.48",
            totalText = "00:12.48",
        ),
    ),
)

private data class StopwatchScreenPreviewParameter(
    val state: ViewState<StopwatchState>,
    val lazyListState: LazyListState = LazyListState(),
)

private class StopwatchScreenPreviewParameterProvider :
    PreviewParameterProvider<StopwatchScreenPreviewParameter> {

    override val values: Sequence<StopwatchScreenPreviewParameter> = sequenceOf(
        StopwatchScreenPreviewParameter(
            state = ViewState.Data(stopwatchStateStub),
        ),
        StopwatchScreenPreviewParameter(
            state = ViewState.Data(StopwatchState()),
        ),
        StopwatchScreenPreviewParameter(
            state = ViewState.Loading.Skeleton(),
        ),
        StopwatchScreenPreviewParameter(
            state = ViewState.Error(UnexpectedError()),
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF010101,
)
@Composable
private fun StopwatchScreenPreview(
    @PreviewParameter(StopwatchScreenPreviewParameterProvider::class)
    parameter: StopwatchScreenPreviewParameter,
) {
    StopwatchScreen(
        viewState = parameter.state,
        controller = stopwatchControllerStub,
        lazyListState = parameter.lazyListState,
    )
}
