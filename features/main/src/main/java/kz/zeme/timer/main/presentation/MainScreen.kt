package kz.zeme.timer.main.presentation

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import kz.zeme.timer.core.error.BaseError
import kz.zeme.timer.core.error.UnexpectedError
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.res.R
import kz.zeme.timer.core.res.theme.TimerTheme
import kz.zeme.timer.core.res.theme.primary
import kz.zeme.timer.core.ui.components.DefaultErrorScreen
import kz.zeme.timer.core.ui.extensions.compose.clickableHaptic
import kz.zeme.timer.core.ui.extensions.shimmer

private const val LIVE_DOT_PULSE_HALF_DURATION_MS = 700

@Composable
internal fun MainScreen(
    state: State<ViewState<MainState>?>,
    controller: MainController,
    tabContainerView: FragmentContainerView? = null,
) {
    MainScreen(
        viewState = state.value,
        controller = controller,
        tabContainerView = tabContainerView,
    )
}

@Composable
private fun MainScreen(
    viewState: ViewState<MainState>?,
    controller: MainController,
    tabContainerView: FragmentContainerView? = null,
) {
    when (viewState) {
        null,
        is ViewState.Loading.Skeleton -> MainShimmerView()

        is ViewState.Error -> DefaultErrorView(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            error = viewState.error,
            onRefresh = controller::onRefresh,
        )

        is ViewState.Loading.ProgressBar,
        is ViewState.Data -> viewState.data?.let {
            MainContent(
                data = it,
                controller = controller,
                tabContainerView = tabContainerView,
            )
        } ?: MainShimmerView()
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
private fun MainShimmerView(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                modifier = Modifier
                    .width(120.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TimerTheme.colors.mainShimmer)
                    .shimmer(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                repeat(2) {
                    Spacer(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TimerTheme.colors.mainShimmer)
                            .shimmer(),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
            thickness = 1.dp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(3) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(TimerTheme.colors.mainShimmer)
                            .shimmer(),
                    )
                    Spacer(
                        modifier = Modifier
                            .width(56.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TimerTheme.colors.mainShimmer)
                            .shimmer(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    data: MainState,
    controller: MainController,
    tabContainerView: FragmentContainerView?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TimerTheme.colors.mainBg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        MainTopBar(
            selectedTab = data.selectedTab,
            isDarkTheme = data.isDarkTheme,
            showAddCityAction = data.showAddCityAction,
            controller = controller,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (tabContainerView != null) AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    (tabContainerView.parent as? ViewGroup)?.removeView(tabContainerView)
                    tabContainerView
                },
            )
        }

        MainBottomBar(
            selectedTab = data.selectedTab,
            isStopwatchRunning = data.isStopwatchRunning,
            isTimerRunning = data.isTimerRunning,
            onTabSelected = controller::onTabSelected,
        )
    }
}

@Composable
private fun MainTopBar(
    selectedTab: MainTab,
    isDarkTheme: Boolean,
    showAddCityAction: Boolean,
    controller: MainController,
) {
    val titleRes = when (selectedTab) {
        MainTab.Stopwatch -> R.string.tab_stopwatch
        MainTab.Timer -> R.string.tab_timer
        MainTab.WorldClock -> R.string.tab_world_clock
    }
    val themeIconRes =
        if (isDarkTheme) R.drawable.ic_theme_dark
        else R.drawable.ic_theme_light

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(titleRes),
            style = TimerTheme.typography.titleLarge.primary(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showAddCityAction) TopBarActionIcon(
                iconRes = R.drawable.ic_add,
                onClick = controller::onAddCityClick,
            )

            TopBarActionIcon(
                iconRes = themeIconRes,
                onClick = controller::onThemeClick,
            )

            TopBarActionIcon(
                iconRes = R.drawable.ic_language,
                onClick = controller::onLanguageClick,
            )
        }
    }
}

@Composable
private fun TopBarActionIcon(
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
) {
    Icon(
        modifier = Modifier
            .size(24.dp)
            .clickableHaptic(onClick = onClick),
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = TimerTheme.colors.textSecondary,
    )
}

@Composable
private fun MainBottomBar(
    selectedTab: MainTab,
    isStopwatchRunning: Boolean,
    isTimerRunning: Boolean,
    onTabSelected: (MainTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TimerTheme.colors.mainBg),
    ) {
        HorizontalDivider(
            color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
            thickness = 1.dp,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BottomBarTab(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_stopwatch,
                label = stringResource(R.string.tab_stopwatch),
                isSelected = selectedTab == MainTab.Stopwatch,
                showLiveDot = isStopwatchRunning,
                onClick = { onTabSelected(MainTab.Stopwatch) },
            )
            BottomBarTab(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_timer,
                label = stringResource(R.string.tab_timer),
                isSelected = selectedTab == MainTab.Timer,
                showLiveDot = isTimerRunning,
                onClick = { onTabSelected(MainTab.Timer) },
            )
            BottomBarTab(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_world_clock,
                label = stringResource(R.string.tab_world_clock),
                isSelected = selectedTab == MainTab.WorldClock,
                showLiveDot = false,
                onClick = { onTabSelected(MainTab.WorldClock) },
            )
        }
    }
}

@Composable
private fun BottomBarTab(
    @DrawableRes iconRes: Int,
    label: String,
    isSelected: Boolean,
    showLiveDot: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor =
        if (isSelected) TimerTheme.colors.textPrimary
        else TimerTheme.colors.textSecondary

    Column(
        modifier = modifier
            .clickableHaptic(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = contentColor,
            )
            if (showLiveDot) LiveIndicatorDot(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-2).dp),
            )
        }

        Text(
            text = label,
            style = TimerTheme.typography.labelSmall.copy(color = contentColor),
            maxLines = 1,
        )
    }
}

@Composable
private fun LiveIndicatorDot(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "liveDotPulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = LIVE_DOT_PULSE_HALF_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liveDotAlpha",
    )
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = LIVE_DOT_PULSE_HALF_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "liveDotScale",
    )

    Box(
        modifier = modifier
            .size(5.dp)
            .scale(scale)
            .alpha(alpha)
            .clip(CircleShape)
            .background(TimerTheme.colors.textPrimary),
    )
}

private val mainControllerStub = object : MainController {
    override fun onStart() = Unit
    override fun onRefresh() = Unit
    override fun onTabSelected(tab: MainTab) = Unit
    override fun onThemeClick() = Unit
    override fun onLanguageClick() = Unit
    override fun onAddCityClick() = Unit
}

private val mainStateTimerStub = MainState(
    selectedTab = MainTab.Timer,
    isStopwatchRunning = true,
    isTimerRunning = true,
    isDarkTheme = true,
    showAddCityAction = false,
)

private val mainStateWorldClockStub = MainState(
    selectedTab = MainTab.WorldClock,
    isStopwatchRunning = false,
    isTimerRunning = false,
    isDarkTheme = true,
    showAddCityAction = true,
)

private data class MainScreenPreviewParameter(
    val state: ViewState<MainState>,
)

private class MainScreenPreviewParameterProvider :
    PreviewParameterProvider<MainScreenPreviewParameter> {

    override val values: Sequence<MainScreenPreviewParameter> = sequenceOf(
        MainScreenPreviewParameter(
            state = ViewState.Data(mainStateTimerStub),
        ),
        MainScreenPreviewParameter(
            state = ViewState.Data(mainStateWorldClockStub),
        ),
        MainScreenPreviewParameter(
            state = ViewState.Loading.Skeleton(),
        ),
        MainScreenPreviewParameter(
            state = ViewState.Error(UnexpectedError()),
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF010101,
)
@Composable
private fun MainScreenPreview(
    @PreviewParameter(MainScreenPreviewParameterProvider::class)
    parameter: MainScreenPreviewParameter,
) {
    MainScreen(
        viewState = parameter.state,
        controller = mainControllerStub,
    )
}
