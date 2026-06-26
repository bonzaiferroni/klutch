package klutch.server

import org.koin.core.Koin
import kotlin.reflect.KClass

interface ProviderScope {
    fun <T : Any> provide(klass: KClass<T>): T
}

class KoinProvider(
    val koin: Koin,
): ProviderScope {
    override fun <T : Any> provide(klass: KClass<T>): T = koin.get(klass)
}

inline fun <reified T : Any> ProviderScope.provide(): T = provide(T::class)
