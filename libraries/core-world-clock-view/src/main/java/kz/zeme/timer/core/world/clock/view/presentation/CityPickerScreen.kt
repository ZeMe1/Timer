package kz.zeme.timer.core.world.clock.view.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
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
import kz.zeme.timer.core.ui.compose.views.ModalBottomSheet
import kz.zeme.timer.core.ui.compose.views.TextField
import kz.zeme.timer.core.ui.extensions.compose.clickableHaptic
import kz.zeme.timer.core.ui.extensions.shimmer

@Composable
fun CityPickerScreen(
    state: State<ViewState<CityPickerState>?>,
    controller: CityPickerController,
) {
    CityPickerScreen(
        viewState = state.value,
        controller = controller,
    )
}

@Composable
private fun CityPickerScreen(
    viewState: ViewState<CityPickerState>?,
    controller: CityPickerController,
) {
    ModalBottomSheet(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = stringResource(R.string.world_clock_add_city),
            style = TimerTheme.typography.titleMedium.primary(),
        )

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            minHeight = 48.dp,
            text = viewState?.data?.query.orEmpty(),
            hint = stringResource(R.string.world_clock_search_city),
            maxLines = 1,
            iconStart = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = TimerTheme.colors.textSecondary,
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
            ),
            onValueChange = controller::onQueryChange,
        )

        when (viewState) {
            null,
            is ViewState.Loading.Skeleton -> CityPickerShimmerView()

            is ViewState.Error -> DefaultErrorView(
                error = viewState.error,
                onRefresh = controller::onStart,
            )

            is ViewState.Loading.ProgressBar,
            is ViewState.Data -> viewState.data?.let {
                CityPickerContent(
                    data = it,
                    controller = controller,
                )
            } ?: CityPickerShimmerView()
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
private fun CityPickerShimmerView(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(5) {
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
                        .width(44.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(TimerTheme.colors.mainShimmer)
                        .shimmer(),
                )
            }
        }
    }
}

@Composable
private fun CityPickerContent(
    data: CityPickerState,
    controller: CityPickerController,
) {
    if (data.items.isEmpty()) {
        CityPickerEmptyView()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 440.dp)
            .padding(bottom = 12.dp),
    ) {
        cityPickerItemsSection(
            items = data.items,
            controller = controller,
        )
    }
}

@Composable
private fun CityPickerEmptyView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.world_clock_empty_search),
            style = TimerTheme.typography.bodyMedium.secondary(),
            textAlign = TextAlign.Center,
        )
    }
}

private fun LazyListScope.cityPickerItemsSection(
    items: ImmutableList<CityPickerItem>,
    controller: CityPickerController,
) {
    itemsIndexed(items = items) { index, item ->
        CityItemRow(
            item = item,
            onClick = { controller.onCityClick(item) },
        )
        if (index < items.lastIndex) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = TimerTheme.colors.buttonDisabled.copy(alpha = 0.5f),
                thickness = 1.dp,
            )
        }
    }
}

@Composable
private fun CityItemRow(
    item: CityPickerItem,
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
                .alpha(if (item.isAdded) 1f else 0f),
            painter = painterResource(R.drawable.ic_check_mark),
            contentDescription = null,
            tint = TimerTheme.colors.textPrimary,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = item.name,
            style = TimerTheme.typography.bodyLarge.primary(),
            textAlign = TextAlign.Start,
        )

        Text(
            text = item.formattedTime,
            style = TimerTheme.typography.bodyMedium.secondary().copy(
                fontFeatureSettings = "tnum",
            ),
        )
    }
}

private val cityPickerControllerStub = object : CityPickerController {
    override fun onStart() = Unit
    override fun onQueryChange(query: String) = Unit
    override fun onCityClick(item: CityPickerItem) = Unit
    override fun onClose() = Unit
}

private val cityPickerStateStub = CityPickerState(
    query = "",
    items = persistentListOf(
        CityPickerItem(
            zoneId = "Asia/Almaty",
            name = "Алматы",
            formattedTime = "22:30",
            isAdded = false,
        ),
        CityPickerItem(
            zoneId = "Europe/London",
            name = "Лондон",
            formattedTime = "17:30",
            isAdded = true,
        ),
        CityPickerItem(
            zoneId = "America/New_York",
            name = "Нью-Йорк",
            formattedTime = "12:30",
            isAdded = true,
        ),
        CityPickerItem(
            zoneId = "Asia/Tokyo",
            name = "Токио",
            formattedTime = "02:30",
            isAdded = true,
        ),
    ),
)

private data class CityPickerScreenPreviewParameter(
    val state: ViewState<CityPickerState>,
)

private class CityPickerScreenPreviewParameterProvider :
    PreviewParameterProvider<CityPickerScreenPreviewParameter> {

    override val values: Sequence<CityPickerScreenPreviewParameter> = sequenceOf(
        CityPickerScreenPreviewParameter(
            state = ViewState.Data(cityPickerStateStub),
        ),
        CityPickerScreenPreviewParameter(
            state = ViewState.Data(CityPickerState(query = "Mars")),
        ),
        CityPickerScreenPreviewParameter(
            state = ViewState.Loading.Skeleton(),
        ),
        CityPickerScreenPreviewParameter(
            state = ViewState.Error(UnexpectedError()),
        ),
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF010101,
)
@Composable
private fun CityPickerScreenPreview(
    @PreviewParameter(CityPickerScreenPreviewParameterProvider::class)
    params: CityPickerScreenPreviewParameter,
) {
    CityPickerScreen(
        viewState = params.state,
        controller = cityPickerControllerStub,
    )
}
