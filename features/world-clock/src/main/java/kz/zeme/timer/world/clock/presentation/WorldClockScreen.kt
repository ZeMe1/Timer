package kz.zeme.timer.world.clock.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
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
import kz.zeme.timer.core.res.theme.primary
import kz.zeme.timer.core.res.theme.secondary
import kz.zeme.timer.core.ui.components.DefaultErrorScreen
import kz.zeme.timer.core.ui.extensions.compose.clickableHaptic
import kz.zeme.timer.core.ui.extensions.shimmer
import kz.zeme.timer.core.world.clock.domain.DayOffset
import kz.zeme.timer.core.world.clock.domain.WorldCityTime

private const val TABULAR_NUMS_FEATURE = "tnum"

@Composable
internal fun WorldClockScreen(
    state: State<ViewState<WorldClockState>?>,
    controller: WorldClockController,
) {
    WorldClockScreen(
        viewState = state.value,
        controller = controller,
    )
}

@Composable
private fun WorldClockScreen(
    viewState: ViewState<WorldClockState>?,
    controller: WorldClockController,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    when (viewState) {
        null,
        is ViewState.Loading.Skeleton -> WorldClockShimmerView()

        is ViewState.Error -> DefaultErrorView(
            error = viewState.error,
            onRefresh = controller::onRefresh,
        )

        is ViewState.Loading.ProgressBar,
        is ViewState.Data -> viewState.data?.let {
            WorldClockContent(
                data = it,
                controller = controller,
                lazyListState = lazyListState,
            )
        } ?: WorldClockShimmerView()
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
private fun WorldClockShimmerView(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier
                    .width(188.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TimerTheme.colors.mainShimmer)
                    .shimmer(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Spacer(
                modifier = Modifier
                    .width(132.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TimerTheme.colors.mainShimmer)
                    .shimmer(),
            )
        }

        repeat(4) { index ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Spacer(
                        modifier = Modifier
                            .width(116.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TimerTheme.colors.mainShimmer)
                            .shimmer(),
                    )
                    Spacer(
                        modifier = Modifier
                            .width(84.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TimerTheme.colors.mainShimmer)
                            .shimmer(),
                    )
                }

                Spacer(
                    modifier = Modifier
                        .width(76.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TimerTheme.colors.mainShimmer)
                        .shimmer(),
                )
            }

            if (index < 3) HorizontalDivider(
                color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
                thickness = 1.dp,
            )
        }
    }
}

@Composable
private fun WorldClockContent(
    data: WorldClockState,
    controller: WorldClockController,
    lazyListState: LazyListState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
    ) {
        WorldClockLocalHero(
            localTime = data.localTime,
            localDateUtc = data.localDateUtc,
        )

        if (data.cities.isEmpty()) WorldClockEmptyView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        else LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = lazyListState,
        ) {
            worldCitiesSection(
                cities = data.cities,
                canRemoveCities = data.canRemoveCities,
                onRemoveClick = controller::onRemoveCityClick,
            )
        }
    }
}

@Composable
private fun WorldClockLocalHero(
    localTime: String,
    localDateUtc: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = localTime,
            style = TimerTheme.typography.titleLarge.primary().copy(
                fontSize = 48.sp,
                lineHeight = 54.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = (-0.025).em,
                fontFeatureSettings = TABULAR_NUMS_FEATURE,
            ),
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = localDateUtc,
            style = TimerTheme.typography.bodySmall.secondary().copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontFeatureSettings = TABULAR_NUMS_FEATURE,
            ),
        )
    }
}

@Composable
private fun WorldClockEmptyView(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.world_clock_empty_cities),
            style = TimerTheme.typography.bodyMedium.secondary(),
            textAlign = TextAlign.Center,
        )
    }
}

private fun LazyListScope.worldCitiesSection(
    cities: ImmutableList<WorldCityTime>,
    canRemoveCities: Boolean,
    onRemoveClick: (String) -> Unit,
) {
    itemsIndexed(
        items = cities,
        key = { _, city -> city.zoneId },
    ) { index, city ->
        WorldCityRow(
            city = city,
            canRemove = canRemoveCities,
            onRemoveClick = { onRemoveClick(city.zoneId) },
        )

        if (index < cities.lastIndex) HorizontalDivider(
            color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
            thickness = 1.dp,
        )
    }
}

@Composable
private fun WorldCityRow(
    city: WorldCityTime,
    canRemove: Boolean,
    onRemoveClick: () -> Unit,
) {
    val dayLabelRes = when (city.dayOffset) {
        DayOffset.Yesterday -> R.string.world_clock_yesterday
        DayOffset.Today -> R.string.world_clock_today
        DayOffset.Tomorrow -> R.string.world_clock_tomorrow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = city.cityName,
                style = TimerTheme.typography.titleSmall.primary(),
            )
            Text(
                text = stringResource(
                    R.string.world_clock_offset_format,
                    stringResource(dayLabelRes),
                    city.hourDiffText,
                ),
                style = TimerTheme.typography.bodySmall.secondary().copy(
                    fontFeatureSettings = TABULAR_NUMS_FEATURE,
                ),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = city.formattedTime,
                style = TimerTheme.typography.titleLarge.primary().copy(
                    fontSize = 32.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.02).em,
                    fontFeatureSettings = TABULAR_NUMS_FEATURE,
                ),
            )

            if (canRemove) Icon(
                modifier = Modifier
                    .size(24.dp)
                    .alpha(0.55f)
                    .clickableHaptic(onClick = onRemoveClick),
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = TimerTheme.colors.textSecondary,
            )
        }
    }
}

private val worldClockControllerStub = object : WorldClockController {
    override fun onStart() = Unit
    override fun onRefresh() = Unit
    override fun onRemoveCityClick(zoneId: String) = Unit
}

private val worldClockStateStub = WorldClockState(
    localTime = "22:31:50",
    localDateUtc = "Вс, 22 нояб. · UTC+5",
    canRemoveCities = true,
    cities = persistentListOf(
        WorldCityTime(
            zoneId = "Europe/London",
            cityName = "Лондон",
            formattedTime = "17:31",
            dayOffset = DayOffset.Today,
            hourDiffText = "-5",
        ),
        WorldCityTime(
            zoneId = "America/New_York",
            cityName = "Нью-Йорк",
            formattedTime = "12:31",
            dayOffset = DayOffset.Today,
            hourDiffText = "-10",
        ),
        WorldCityTime(
            zoneId = "Asia/Tokyo",
            cityName = "Токио",
            formattedTime = "02:31",
            dayOffset = DayOffset.Tomorrow,
            hourDiffText = "+4",
        ),
        WorldCityTime(
            zoneId = "Asia/Dubai",
            cityName = "Дубай",
            formattedTime = "21:31",
            dayOffset = DayOffset.Today,
            hourDiffText = "-1",
        ),
    ),
)

private val worldClockEmptyStateStub = WorldClockState(
    localTime = "22:31:50",
    localDateUtc = "Вс, 22 нояб. · UTC+5",
)

private data class WorldClockScreenPreviewParameter(
    val state: ViewState<WorldClockState>,
    val lazyListState: LazyListState = LazyListState(),
)

private class WorldClockScreenPreviewParameterProvider :
    PreviewParameterProvider<WorldClockScreenPreviewParameter> {

    override val values: Sequence<WorldClockScreenPreviewParameter> = sequenceOf(
        WorldClockScreenPreviewParameter(
            state = ViewState.Data(worldClockStateStub),
        ),
        WorldClockScreenPreviewParameter(
            state = ViewState.Data(worldClockEmptyStateStub),
        ),
        WorldClockScreenPreviewParameter(
            state = ViewState.Loading.Skeleton(),
        ),
        WorldClockScreenPreviewParameter(
            state = ViewState.Error(UnexpectedError()),
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF010101,
)
@Composable
private fun WorldClockScreenPreview(
    @PreviewParameter(WorldClockScreenPreviewParameterProvider::class)
    parameter: WorldClockScreenPreviewParameter,
) {
    WorldClockScreen(
        viewState = parameter.state,
        controller = worldClockControllerStub,
        lazyListState = parameter.lazyListState,
    )
}
