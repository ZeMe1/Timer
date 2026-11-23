package kz.zeme.timer.stopwatch.presentation

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import kz.zeme.timer.core.ui.extensions.shimmer
import kz.zeme.timer.stopwatch.presentation.StopwatchState.StopwatchLapItem

private const val TABULAR_NUMS_FEATURE = "tnum"

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        val dialModifier =
            if (data.laps.isEmpty()) Modifier.weight(1f).fillMaxWidth()
            else Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 16.dp)

        StopwatchDial(
            modifier = dialModifier,
            mainTime = data.mainTime,
            centiseconds = data.centiseconds,
            currentLapNumber = data.currentLapNumber,
            currentLapTime = data.currentLapTime,
        )

        if (data.laps.isNotEmpty()) StopwatchLapsList(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            laps = data.laps,
            lazyListState = lazyListState,
        )

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
            if (currentLapNumber != null && currentLapTime != null) Text(
                text = stringResource(
                    R.string.stopwatch_lap_live,
                    currentLapNumber,
                    currentLapTime,
                ),
                style = TimerTheme.typography.bodySmall.secondary().copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFeatureSettings = TABULAR_NUMS_FEATURE,
                ),
            )
        }
    }
}

@Composable
private fun StopwatchLapsList(
    laps: ImmutableList<StopwatchLapItem>,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier,
) {
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
        StopwatchLapRow(lap = lap)
        HorizontalDivider(
            color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
            thickness = 1.dp,
        )
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (status) {
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

@Composable
private fun StopwatchActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Button(
        modifier = modifier.height(54.dp),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(14.dp),
        elevation = null,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
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
