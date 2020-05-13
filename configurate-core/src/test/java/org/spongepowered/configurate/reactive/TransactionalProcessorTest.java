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

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TransactionalProcessorTest {

    private <V> Processor.TransactionalIso<V> create() {
        return Processor.createTransactional(Runnable::run);
    }

    @Test
    public void testTransaction() {
        final Processor.TransactionalIso<String> proc = create();
        final SubscriberTransactionalTest subject = new SubscriberTransactionalTest();

        proc.subscribe(subject);
        proc.submit("test");
        assertEquals("test", subject.value);
        assertNull(subject.nextValue);
    }

    @Test
    public void testFailureRollsBack() {
        final Processor.TransactionalIso<String> proc = create();
        final SubscriberTransactionalTest subject = new SubscriberTransactionalTest();

        proc.subscribe(subject);
        proc.submit("test");
        subject.shouldThrow = true;
        proc.submit("Won't take effect");
        assertEquals("test", subject.value);
        assertNull(subject.nextValue);
    }

    @Test
    public void testFailurePreventsCommits() {
        final Processor.TransactionalIso<String> proc = create();
        final SubscriberTransactionalTest subject1 = new SubscriberTransactionalTest();
        final SubscriberTransactionalTest subject2 = new SubscriberTransactionalTest();
        proc.subscribe(subject1);
        proc.subscribe(subject2);

        proc.submit("yeet");
        assertEquals("yeet", subject1.value);
        assertEquals("yeet", subject2.value);

        subject1.shouldThrow = true;
        proc.submit("yoink");
        assertEquals("yeet", subject1.value);
        assertEquals("yeet", subject2.value);
        assertNull(subject1.nextValue);
        assertNull(subject2.nextValue);
        assertEquals(1, subject1.rollBackCount);
        assertEquals(1, subject2.rollBackCount);
    }

    static class SubscriberTransactionalTest implements TransactionalSubscriber<String> {
        boolean shouldThrow = false;
        @MonotonicNonNull String value;
        @Nullable String nextValue;
        int rollBackCount = 0;


        @Override
        public void beginTransaction(String newValue) throws TransactionFailedException {
            if (shouldThrow) {
                throw new TransactionFailedException();
            }
            this.nextValue = newValue;
        }

        @Override
        public void commit() {
            if (this.nextValue != null) {
                this.value = this.nextValue;
                this.nextValue = null;
            }
        }

        @Override
        public void rollback() {
            ++rollBackCount;
            this.nextValue = null;
        }

        @Override
        public void onError(Throwable e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }
}
