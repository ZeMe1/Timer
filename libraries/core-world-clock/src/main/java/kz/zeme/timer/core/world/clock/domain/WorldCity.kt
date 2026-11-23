package kz.zeme.timer.core.world.clock.domain

data class WorldCity(
    val zoneId: String,
    val name: String,
    val sortOrder: Int = 0,
)
