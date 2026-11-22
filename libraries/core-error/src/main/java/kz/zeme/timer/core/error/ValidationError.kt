package kz.zeme.timer.core.error

import kotlinx.parcelize.Parcelize

@Parcelize
open class ValidationError(
    override val code: ErrorCode = DefaultErrorCode,
    override val underlying: Throwable? = null,
    override val message: String? = null,
) : BaseError(
    code = code,
    underlying = underlying,
    message = message,
)
