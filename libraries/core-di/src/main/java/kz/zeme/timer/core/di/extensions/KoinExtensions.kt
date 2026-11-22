package kz.zeme.timer.core.di.extensions

import kotlin.reflect.KClass
import org.koin.core.definition.KoinDefinition
import org.koin.dsl.binds

inline fun <reified T> KoinDefinition<T>.binds(
    vararg classes: KClass<*>,
): KoinDefinition<T> {
    binds(arrayOf(*classes))
    return this
}
