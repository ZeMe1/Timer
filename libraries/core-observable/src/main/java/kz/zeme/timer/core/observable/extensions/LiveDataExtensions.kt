package kz.zeme.timer.core.observable.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kz.zeme.timer.core.domain.Response
import kz.zeme.timer.core.error.UnexpectedError
import kz.zeme.timer.core.observable.ViewState

fun <T : Any> LiveData<ViewState<T>>.getLoadingState(
    data: T? = value?.data,
    loaderDurationMs: Long = ViewState.LOADER_DEFAULT_DURATION_MS,
): ViewState.Loading<T> = data
    ?.let { ViewState.Loading.ProgressBar(data = it, durationMs = loaderDurationMs) }
    ?: ViewState.Loading.Skeleton(durationMs = loaderDurationMs)

suspend fun <T : Any> LiveData<ViewState<T>>.getViewState(
    response: Response<T>,
): ViewState<T> = getViewState(
    response = response,
    transform = { it },
)

suspend fun <X : Any, Y : Any> LiveData<ViewState<X>>.getViewState(
    response: Response<Y>,
    transform: suspend (Y) -> X,
): ViewState<X> {
    val currentState = value
    if (currentState != null) return currentState.getViewState(response, transform)

    return when (response) {
        is Response.Failure -> getErrorState(response)
        is Response.Success -> try {
            ViewState.Data(transform(response.data))
        } catch (ex: Exception) {
            ViewState.Error(UnexpectedError(underlying = ex))
        }
    }
}

fun <T : Any, F : Any> LiveData<ViewState<T>>.getErrorState(
    response: Response.Failure<F>,
): ViewState.Error<T> = ViewState.Error(
    error = response.data,
    data = value?.data,
)

fun <X : Any, Y : Any> LiveData<ViewState<X>>.mapState(
    transform: (X) -> Y,
): ViewState<Y>? = value?.mapState(transform)

fun <T : Any> MutableLiveData<ViewState<T>>.update(block: T.() -> T) {
    val update = (value?.data ?: return).block()
    value = ViewState.Data(update)
}

fun <T : Any> MutableLiveData<ViewState<T>>.postUpdate(block: T.() -> T) {
    val update = (value?.data ?: return).block()
    postValue(ViewState.Data(update))
}

suspend fun <T : Any> MutableLiveData<ViewState<T>>.getResponse(
    response: Response<T>,
) {
    getResponse(
        response = response,
        transform = { it },
    )
}

suspend fun <X : Any, Y : Any> MutableLiveData<ViewState<X>>.getResponse(
    response: Response<Y>,
    transform: suspend (Y) -> X,
) {
    value = getViewState(
        response = response,
        transform = transform,
    )
}

fun <T> MutableLiveData<T>.updateValue(block: T.() -> T) {
    val current = value ?: return
    value = current.block()
}

fun <T> MutableLiveData<T>.postUpdateValue(block: T.() -> T) {
    val current = value ?: return
    postValue(current.block())
}

fun <T> LiveData<T>.observeOnce(
    owner: LifecycleOwner,
    observer: (T) -> Unit,
) {
    observe(
        owner,
        object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                observer(value)
            }
        },
    )
}
