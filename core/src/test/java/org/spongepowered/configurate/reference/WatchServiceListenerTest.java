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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.reactive.Disposable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WatchServiceListenerTest {

    private static @MonotonicNonNull WatchServiceListener listener;

    @BeforeAll
    public static void setUpClass() throws IOException {
        listener = WatchServiceListener.create();
    }

    @AfterAll
    public static void tearDownClass() throws IOException {
        listener.close();
    }

    @Test
    public void testListenToRelativePath() throws IOException {
        // We test that this doesn't throw any exceptions
        listener.listenToFile(Paths.get("testfile.conf"), event -> {}).dispose();
    }

    @Test
    public void testListenToPath() throws IOException {
        final Path tempFolder = Files.createTempDirectory("configurate-test");
        final Path testFile = tempFolder.resolve("listenPath.txt");
        Files.write(testFile, Collections.singleton("version one"), StandardOpenOption.SYNC,
            StandardOpenOption.CREATE);

        final AtomicBoolean notified = new AtomicBoolean();
        final AtomicInteger callCount = new AtomicInteger(0);
        final Object condition = new Object();
        final AtomicReference<Disposable> disposer = new AtomicReference<>();
        disposer.set(listener.listenToFile(testFile, event -> {
            synchronized (condition) {
                final int oldVal = callCount.getAndIncrement();
                if (oldVal > 1) {
                    disposer.get().dispose();
                    return;
                }
                notified.set(true);
                condition.notify();
                if (oldVal >= 1) {
                    disposer.get().dispose();
                }
            }
        }));

        assertEquals(0, callCount.get());


        assertTimeout(Duration.ofMinutes(1), () -> {
            synchronized (condition) {
                Files.write(testFile, Collections.singleton("version two"), StandardOpenOption.SYNC);
                while (!notified.get()) {
                    condition.wait();
                }
                notified.set(false);
                assertEquals(1, callCount.get());

                Files.write(testFile, Collections.singleton("version three"), StandardOpenOption.SYNC);
                while (!notified.get()) {
                    condition.wait();
                }
                notified.set(false);
                assertEquals(2, callCount.get());
            }

            Files.write(testFile, Collections.singleton("version four"), StandardOpenOption.SYNC);
            assertEquals(2, callCount.get());
        });
    }

    @Test
    @Disabled
    public void testListenToDirectory() throws IOException {
        final Path tempFolder = Files.createTempDirectory("configurate-test");
        final Path test1 = tempFolder.resolve("test1");
        final Path test2 = tempFolder.resolve("test2");
        Files.createFile(test1);
        Files.createFile(test2);

        final AtomicReference<Path> lastPath = new AtomicReference<>();
        final CyclicBarrier barrier = new CyclicBarrier(2);

        listener.listenToDirectory(tempFolder, event -> {
            if (event.context() instanceof Path) {
                lastPath.set(((Path) event.context()));
            } else {
                throw new RuntimeException("Event " + event + " received, was not expected");
            }
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        });

        assertTimeout(Duration.ofMinutes(1), () -> { // in case of locking issues
            Files.write(test1, Collections.singleton("version one"), StandardOpenOption.SYNC);

            barrier.await();
            assertEquals(test1.getFileName(), lastPath.get());
            barrier.reset();

            Files.write(test2, Collections.singleton("version two"), StandardOpenOption.SYNC);
            barrier.await();
            assertEquals(test2.getFileName(), lastPath.get());
        });
    }

}
