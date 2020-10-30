/*
 * Configurate
 * Copyright (C) zml and Configurate contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spongepowered.configurate.kotlin.extensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.spongepowered.configurate.reactive.Publisher
import org.spongepowered.configurate.reactive.Subscriber

/**
 * Given an [Publisher] instance, return a new [Flow] emitting values from the Flow
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <V : Any> Publisher<V>.asFlow(): Flow<V> = callbackFlow {
    val observer = object : Subscriber<V> {
        override fun submit(item: V) {
            sendBlocking(item)
        }

        override fun onError(thrown: Throwable) {
            close(thrown)
        }

        override fun onClose() {
            close()
        }
    }
    val dispose = subscribe(observer)
    awaitClose {
        dispose.dispose()
    }
}
