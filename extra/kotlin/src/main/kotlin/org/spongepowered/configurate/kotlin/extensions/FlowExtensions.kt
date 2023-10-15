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

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.spongepowered.configurate.kotlin.FlowPublisher
import org.spongepowered.configurate.reactive.Publisher

/**
 * Given a pre-existing flow, expose it as a [Publisher].
 *
 * This will not change the semantics of the Flow -- i.e. if it is "hot" it will stay hot, and vice
 * versa.
 */
suspend fun <V : Any> Flow<V>.asPublisher(): Publisher<V> = coroutineScope {
    FlowPublisher(this@asPublisher, this)
}
