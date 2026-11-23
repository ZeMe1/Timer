package kz.zeme.timer.core.world.clock.domain

data class LocalClockSnapshot(
    val formattedTime: String,
    val dateText: String,
    val utcOffsetText: String,
)
