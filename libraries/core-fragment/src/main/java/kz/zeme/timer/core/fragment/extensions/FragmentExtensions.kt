package kz.zeme.timer.core.fragment.extensions

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kz.zeme.timer.core.activity.extensions.hideKeyboard
import kz.zeme.timer.core.activity.extensions.makeToast
import kz.zeme.timer.core.activity.extensions.showKeyboard
import kz.zeme.timer.core.error.BaseError
import kz.zeme.timer.core.navigation.BaseNavigation

fun Fragment.makeToast(
    error: BaseError,
    duration: Int = Toast.LENGTH_LONG,
) {
    context?.makeToast(error, duration)
}

fun Fragment.makeToast(
    @StringRes messageRes: Int,
    duration: Int = Toast.LENGTH_LONG,
) {
    context?.makeToast(getString(messageRes), duration)
}

fun Fragment.makeToast(
    message: String,
    duration: Int = Toast.LENGTH_LONG,
) {
    context?.makeToast(message, duration)
}

fun Fragment.makeToast(
    data: BaseNavigation.ShowToast,
    duration: Int = Toast.LENGTH_LONG,
) {
    when (data) {
        is BaseNavigation.ShowToast.Message -> makeToast(data.msg, duration)
        is BaseNavigation.ShowToast.MessageErr -> makeToast(data.error, duration)
        is BaseNavigation.ShowToast.MessageRes -> makeToast(data.msgRes, duration)
    }
}

fun Fragment.hideKeyboard() {
    context?.hideKeyboard(requireView())
}

fun Fragment.showKeyboard() {
    context?.showKeyboard()
}

fun Fragment.composeView(
    content: @Composable () -> Unit,
): ComposeView = ComposeView(requireContext()).applyContent(content)

fun Fragment.childFragmentManagerOrNull(): FragmentManager? = try {
    when {
        !isAdded -> null
        context == null -> null
        else -> childFragmentManager
    }
} catch (ex: Exception) {
    null
}

fun ComposeView.applyContent(
    content: @Composable () -> Unit,
): ComposeView = apply {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent { content() }
}
