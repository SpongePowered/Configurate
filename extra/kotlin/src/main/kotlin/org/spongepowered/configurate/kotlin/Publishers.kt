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
package org.spongepowered.configurate.kotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.spongepowered.configurate.reactive.Disposable
import org.spongepowered.configurate.reactive.Publisher
import org.spongepowered.configurate.reactive.Subscriber
import org.spongepowered.configurate.reactive.TransactionFailedException
import org.spongepowered.configurate.util.CheckedFunction
import java.util.concurrent.Executor

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

/**
 * Given a pre-existing flow, expose it as a [Publisher]. This will not change the semantics of the Flow --
 * i.e. if it is "hot" it will stay hot, and vice versa.
 */
suspend fun <V : Any> Flow<V>.asPublisher(): Publisher<V> = coroutineScope {
    FlowPublisher(this@asPublisher, this)
}

private class FlowPublisher<V>(val flow: Flow<V>, val scope: CoroutineScope) : Publisher<V> {
    private val executor = Executor { task -> scope.launch { task.run() } }
    override fun executor(): Executor = this.executor

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun subscribe(subscriber: Subscriber<in V>): Disposable {
        val ret = flow
            .onEach { subscriber.submit(it) }
            .catch { subscriber.onError(it) }
            .onCompletion { subscriber.onClose() }
            .launchIn(scope)
        return Disposable { ret.cancel() }
    }

    override fun hasSubscribers(): Boolean {
        return scope.coroutineContext.isActive
    }

    override fun <R> map(mapper: CheckedFunction<in V, out R, TransactionFailedException>): Publisher<R> {
        return FlowPublisher(flow.map { mapper.apply(it) }, scope)
    }
}
