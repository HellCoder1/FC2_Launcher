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
package net.fc2.gui;

import net.fc2.data.Settings;
import net.fc2.log.Logger;
import net.fc2.util.OSUtils;
import net.fc2.util.TrackerUtils;
import net.fc2.util.winreg.JavaInfo;

public class LaunchFrameHelpers {
    public static void printInfo() {
        Logger.logInfo("FriendsCraft2 Launch starting up (version " + LaunchFrame.getVersion() + " Build: " + LaunchFrame.buildNumber + ")");
        Logger.logInfo("Java version: " + System.getProperty("java.version"));
        Logger.logInfo("Java vendor: " + System.getProperty("java.vendor"));
        Logger.logInfo("Java home: " + System.getProperty("java.home"));
        Logger.logInfo("Java specification: " + System.getProperty("java.vm.specification.name") + " version: " + System.getProperty("java.vm.specification.version") + " by "
                + System.getProperty("java.vm.specification.vendor"));
        Logger.logInfo("Java vm: " + System.getProperty("java.vm.name") + " version: " + System.getProperty("java.vm.version") + " by " + System.getProperty("java.vm.vendor"));
        Logger.logInfo("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + (OSUtils.is64BitOS() ? "64-bit" : "32-bit") + ")");
        Logger.logInfo("Launcher Install Dir: " + Settings.getSettings().getInstallPath());
        Logger.logInfo("System memory: " + OSUtils.getOSFreeMemory() + "M free, " + OSUtils.getOSTotalMemory() + "M total");

        //hack: I want to trigger JavaFinder here:
        String selectedJavaPath = Settings.getSettings().getJavaPath();
        //then test if preferred and selected java paths differs
        if (!selectedJavaPath.equals(Settings.getSettings().getDefaultJavaPath())) {
            Logger.logInfo("Using Java path entered by user: " + selectedJavaPath);
        }

        if (!OSUtils.is64BitOS()) {
            Logger.logError("32-bit operating system. 64-bit is required for most mod packs. If you have issues, please try the FC2 1.7.10 pack.");
        }

        if (OSUtils.is64BitOS() && !Settings.getSettings().getCurrentJava().is64bits) {//unfortunately the easy to find DL links are for 32 bit java
            Logger.logError("32-bit Java in 64-bit operating system. 64-bit Java is required for most mod packs. If you have issues, please try the FC2 1.7.10 pack.");
        }

        JavaInfo java = Settings.getSettings().getCurrentJava();
        if (java.getMajor() < 1 || (java.getMajor() == 1 && java.getMinor() < 7)) {
            Logger.logError("Java 6 detected. Java 7 is recommended for most mod packs.");
        }

    }

    public static void googleAnalytics() {
        if (!Settings.getSettings().getLoaded()) {
            TrackerUtils.sendPageView("net/fc2/gui/LaunchFrame.java", "OS: " + System.getProperty("os.name") + " : " + System.getProperty("os.arch"));
            TrackerUtils.sendPageView("net/fc2/gui/LaunchFrame.java", "Unique User (Settings)");
            Settings.getSettings().setLoaded(true);
        }
    }

}
