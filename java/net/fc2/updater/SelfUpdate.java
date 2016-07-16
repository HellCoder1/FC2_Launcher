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
package net.fc2.updater;

import net.fc2.log.Logger;
import net.fc2.util.FileUtils;
import net.fc2.util.OSUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelfUpdate {
    public static void runUpdate(String currentPath, String temporaryUpdatePath) {
        List<String> arguments = new ArrayList<String>();

        String separator = System.getProperty("file.separator");
        String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        arguments.add(path);
        arguments.add("-cp");
        arguments.add(temporaryUpdatePath);
        arguments.add(SelfUpdate.class.getCanonicalName());
        arguments.add(currentPath);
        arguments.add(temporaryUpdatePath);

        Logger.logInfo("Would update with: " + arguments);
        Logger.logInfo("c: " + currentPath);
        Logger.logInfo("n: " + temporaryUpdatePath);
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);
        try {
            processBuilder.start();
        } catch (IOException e) {
            Logger.logError("Failed to start self-update process", e);
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            if (OSUtils.getCurrentOS() != OSUtils.OS.UNIX) {
                Thread.sleep(4000);
            }
        } catch (InterruptedException ignored) {
            Logger.logError(ignored.getMessage(), ignored);
        }
        String launcherPath = args[0];
        String temporaryUpdatePath = args[1];
        File launcher = new File(launcherPath);
        File temporaryUpdate = new File(temporaryUpdatePath);
        try {
            FileUtils.delete(launcher);
            FileUtils.copyFile(temporaryUpdate, launcher);
        } catch (IOException e) {
            Logger.logError("Auto Updating Failed", e);
        }

        List<String> arguments = new ArrayList<String>();

        String separator = System.getProperty("file.separator");
        String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        arguments.add(path);
        arguments.add("-jar");
        arguments.add(launcherPath);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);
        try {
            processBuilder.start();
        } catch (IOException e) {
            Logger.logError("Failed to start launcher process after updating", e);
        }
    }
}
