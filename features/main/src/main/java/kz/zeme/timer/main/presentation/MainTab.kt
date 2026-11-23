package kz.zeme.timer.main.presentation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MainTab : Parcelable {
    Stopwatch,
    Timer,
    WorldClock,
}
