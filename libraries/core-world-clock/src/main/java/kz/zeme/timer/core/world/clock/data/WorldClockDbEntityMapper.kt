package kz.zeme.timer.core.world.clock.data

import kz.zeme.timer.core.world.clock.data.local.db.WorldCityDbEntity
import kz.zeme.timer.core.world.clock.domain.WorldCity

class WorldClockDbEntityMapper {

    fun mapToDomain(
        entity: WorldCityDbEntity,
        cityName: String,
    ): WorldCity = WorldCity(
        zoneId = entity.zoneId,
        name = cityName,
        sortOrder = entity.sortOrder,
    )

    fun mapToDbEntity(
        zoneId: String,
        sortOrder: Int,
    ): WorldCityDbEntity = WorldCityDbEntity(
        zoneId = zoneId,
        sortOrder = sortOrder,
    )
}
