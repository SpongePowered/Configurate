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
package org.spongepowered.configurate.reference;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates threads whose name is the provided prefix followed by an
 * incrementing number.
 *
 * <p>The threads can optionally be daemon threads.
 */
class PrefixedNameThreadFactory implements ThreadFactory {

    private final String name;
    private final boolean daemon;
    private final AtomicInteger counter = new AtomicInteger();

    PrefixedNameThreadFactory(final String prefix, final boolean daemon) {
        this.name = prefix.endsWith("-") ? prefix : (prefix + "-");
        this.daemon = daemon;
    }
    
    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread ret = new Thread(runnable, this.name + this.counter.getAndIncrement());
        ret.setDaemon(this.daemon);
        return ret;
    }

}
