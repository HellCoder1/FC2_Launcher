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
package net.fc2.tools;

import net.fc2.log.Logger;

public class ProcessMonitor implements Runnable {

    private final Process proc;
    private final Runnable onComplete;

    private ProcessMonitor(Process proc, Runnable onComplete) {
        this.proc = proc;
        this.onComplete = onComplete;
    }

    public static ProcessMonitor create(Process proc, Runnable onComplete) {
        ProcessMonitor processMonitor = new ProcessMonitor(proc, onComplete);
        Thread monitorThread = new Thread(processMonitor);
        monitorThread.start();
        return processMonitor;
    }

    public void run() {
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            Logger.logError(e.getMessage(), e);
        }
        onComplete.run();
    }

    public void stop() {
        if (proc != null)
            proc.destroy();
    }

}
