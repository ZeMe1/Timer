package kz.zeme.timer.core.theme.view.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kz.zeme.timer.core.error.BaseError
import kz.zeme.timer.core.error.UnexpectedError
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.res.R
import kz.zeme.timer.core.res.theme.DarkTimerColors
import kz.zeme.timer.core.res.theme.LightTimerColors
import kz.zeme.timer.core.res.theme.TimerTheme
import kz.zeme.timer.core.res.theme.primary
import kz.zeme.timer.core.theme.domain.AppThemeMode
import kz.zeme.timer.core.theme.domain.ThemeOption
import kz.zeme.timer.core.ui.components.DefaultErrorScreen
import kz.zeme.timer.core.ui.compose.views.ModalBottomSheet
import kz.zeme.timer.core.ui.extensions.compose.clickableHaptic
import kz.zeme.timer.core.ui.extensions.shimmer

@Composable
fun ThemePickerScreen(
    state: State<ViewState<List<ThemeOption>>?>,
    controller: ThemePickerController,
) {
    ThemePickerScreen(
        viewState = state.value,
        controller = controller,
    )
}

@Composable
private fun ThemePickerScreen(
    viewState: ViewState<List<ThemeOption>>?,
    controller: ThemePickerController,
) {
    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(R.string.theme_picker_title),
            style = TimerTheme.typography.titleMedium.primary(),
        )

        when (viewState) {
            null,
            is ViewState.Loading.Skeleton -> ThemePickerShimmerView()

            is ViewState.Error -> DefaultErrorView(
                error = viewState.error,
                onRefresh = controller::onStart,
            )

            is ViewState.Loading.ProgressBar,
            is ViewState.Data -> viewState.data?.let {
                ThemeListContent(
                    themes = it,
                    controller = controller,
                )
            } ?: ThemePickerShimmerView()
        }
    }
}

@Composable
private fun DefaultErrorView(
    modifier: Modifier = Modifier,
    error: BaseError? = null,
    onRefresh: () -> Unit,
) {
    DefaultErrorScreen(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        errorMessage = error?.message ?: stringResource(R.string.shared_something_went_wrong),
        onRefresh = onRefresh,
        backgroundColor = TimerTheme.colors.basicDark,
    )
}

@Composable
private fun ThemePickerShimmerView(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(TimerTheme.colors.mainShimmer)
                        .shimmer(),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TimerTheme.colors.mainShimmer)
                        .shimmer(),
                )
                Spacer(modifier = Modifier.width(48.dp))
                Spacer(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(TimerTheme.colors.mainShimmer)
                        .shimmer(),
                )
            }
        }
    }
}

@Composable
private fun ThemeListContent(
    themes: List<ThemeOption>,
    controller: ThemePickerController,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    ) {
        themeOptionsSection(
            themes = themes,
            controller = controller,
        )
    }
}

private fun LazyListScope.themeOptionsSection(
    themes: List<ThemeOption>,
    controller: ThemePickerController,
) {
    items(items = themes) { option ->
        ThemeItemRow(
            option = option,
            onClick = { controller.onThemeClick(option) },
        )
        if (themes.lastOrNull() != option) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
                thickness = 1.dp,
            )
        }
    }
}

@Composable
private fun ThemeItemRow(
    option: ThemeOption,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableHaptic(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 12.dp)
                .size(24.dp)
                .alpha(if (option.isSelected) 1f else 0f),
            painter = painterResource(R.drawable.ic_check_mark),
            contentDescription = null,
            tint = TimerTheme.colors.textPrimary,
        )

        val titleRes = when (option.mode) {
            AppThemeMode.Dark -> R.string.theme_picker_dark
            AppThemeMode.Light -> R.string.theme_picker_light
            AppThemeMode.System -> R.string.theme_picker_system
        }

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(titleRes),
            style = TimerTheme.typography.bodyLarge.primary(),
            textAlign = TextAlign.Start,
        )

        ThemeSwatch(mode = option.mode)
    }
}

@Composable
private fun ThemeSwatch(
    mode: AppThemeMode,
) {
    val borderColor = TimerTheme.colors.gray8

    when (mode) {
        AppThemeMode.Dark -> Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(DarkTimerColors.mainBg)
                .border(width = 1.5.dp, color = borderColor, shape = CircleShape),
        )

        AppThemeMode.Light -> Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(LightTimerColors.mainBg)
                .border(width = 1.5.dp, color = borderColor, shape = CircleShape),
        )

        AppThemeMode.System -> Canvas(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(width = 1.5.dp, color = borderColor, shape = CircleShape),
        ) {
            drawArc(
                color = DarkTimerColors.mainBg,
                startAngle = 135f,
                sweepAngle = 180f,
                useCenter = true,
            )
            drawArc(
                color = LightTimerColors.mainBg,
                startAngle = 315f,
                sweepAngle = 180f,
                useCenter = true,
            )
        }
    }
}

private val themePickerControllerStub = object : ThemePickerController {
    override fun onThemeClick(option: ThemeOption) = Unit
    override fun onClose() = Unit
    override fun onStart() = Unit
}

private val themeListStub = listOf(
    ThemeOption(mode = AppThemeMode.Dark, isSelected = true),
    ThemeOption(mode = AppThemeMode.Light, isSelected = false),
    ThemeOption(mode = AppThemeMode.System, isSelected = false),
)

private data class ThemePickerScreenPreviewParameter(
    val state: ViewState<List<ThemeOption>>,
)

private class ThemePickerScreenPreviewParameterProvider :
    PreviewParameterProvider<ThemePickerScreenPreviewParameter> {

    override val values: Sequence<ThemePickerScreenPreviewParameter> = sequenceOf(
        ThemePickerScreenPreviewParameter(
            state = ViewState.Data(themeListStub),
        ),
        ThemePickerScreenPreviewParameter(
            state = ViewState.Data(emptyList()),
        ),
        ThemePickerScreenPreviewParameter(
            state = ViewState.Loading.Skeleton(),
        ),
        ThemePickerScreenPreviewParameter(
            state = ViewState.Error(UnexpectedError()),
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF010101,
)
@Composable
private fun ThemePickerScreenPreview(
    @PreviewParameter(ThemePickerScreenPreviewParameterProvider::class)
    params: ThemePickerScreenPreviewParameter,
) {
    ThemePickerScreen(
        viewState = params.state,
        controller = themePickerControllerStub,
    )
}
