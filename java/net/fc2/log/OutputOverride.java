/*
 * Copyright © 2014.
 * This file is part of Friendscraft2 Launcher.
 * Friendscraft2 Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.fc2.log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OutputOverride extends PrintStream {
    final LogLevel level;

    public OutputOverride(OutputStream str, LogLevel type) {
        super(str);
        this.level = type;
    }

    @Override
    public void write (@SuppressWarnings("NullableProblems") byte[] b) throws IOException {
        //super.write(b);
        String text = new String(b).trim();
        if (!text.equals("") && !text.equals("\n")) {
            Logger.log("From Console: " + text, level, null);
        }
    }

    @Override
    public void write (@SuppressWarnings("NullableProblems") byte[] buf, int off, int len) {
        //super.write(buf, off, len);
        String text = new String(buf, off, len).trim();
        if (!text.equals("") && !text.equals("\n")) {
            Logger.log("From Console: " + text, level, null);
        }
    }

    @Override
    public void write (int b) {
        throw new UnsupportedOperationException("Write(int) is not supported by OutputOverride.");
    }
}
