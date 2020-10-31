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
package org.spongepowered.configurate.reactive;

/**
 * Indicate that the newly submitted value was invalid, and the transaction that
 * submitted the new value should be marked as a failure.
 *
 * @since 4.0.0
 */
public class TransactionFailedException extends Exception {

    private static final long serialVersionUID = -3726752009864775844L;

    /**
     * Create an exception indicating a transaction has failed for an
     * unknown reason.
     *
     * @since 4.0.0
     */
    public TransactionFailedException() {
    }

    /**
     * Create an exception wrapping the cause of a transaction failure.
     *
     * @param cause the original exception cause
     * @since 4.0.0
     */
    public TransactionFailedException(final Throwable cause) {
        super(cause);
    }

}
