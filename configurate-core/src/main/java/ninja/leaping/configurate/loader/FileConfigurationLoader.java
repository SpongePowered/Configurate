/**
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
package ninja.leaping.configurate.loader;

import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Base class for many stream-based configuration loaders. This class provides conversion from a variety of input
 * sources to CharSource/Sink objects, providing a consistent API for loaders to read from and write to.
 *
 * Either the source or sink may be null. If this is true, this loader may not support either loading or saving. In
 * this case, implementing classes are expected to throw an IOException.
 */
public abstract class FileConfigurationLoader implements ConfigurationLoader {
    public static final Charset UTF8_CHARSET = Charset.forName("utf-8");
    static {
        assert UTF8_CHARSET != null; // If it is, there is a serious problem w/ this user's jdk installation
    }
    protected final CharSource source;
    protected final CharSink sink;

    public FileConfigurationLoader(File file) {
        this(Files.asCharSource(file, UTF8_CHARSET), Files.asCharSink(file, UTF8_CHARSET));
    }

    public FileConfigurationLoader(URL url) {
        this(Resources.asCharSource(url, UTF8_CHARSET), null);
    }

    public FileConfigurationLoader(CharSource source, CharSink sink) {
        this.source = source;
        this.sink = sink;
    }

    public boolean canLoad() {
        return this.source != null;
    }

    public boolean canSave() {
        return this.sink != null;
    }
}
