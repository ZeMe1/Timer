package kz.zeme.timer.core.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class RequestCode : Parcelable {
    RC_DEFAULT,
    RC_CITY_PICKER,
}
