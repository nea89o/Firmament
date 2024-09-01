package moe.nea.firmament.events.subscription

import java.util.ServiceLoader
import kotlin.streams.asSequence
import moe.nea.firmament.Firmament

interface SubscriptionList {
    fun provideSubscriptions(addSubscription: (Subscription<*>) -> Unit)

    companion object {
        val allLists by lazy {
            ServiceLoader.load(SubscriptionList::class.java)
                .stream()
                .asSequence()
                .mapNotNull {
                    kotlin.runCatching { it.get() }
                        .getOrElse { ex ->
                            Firmament.logger.error("Could not load subscriptions from ${it.type()}", ex)
                            null
                        }
                }
                .toList()
        }
    }
}
