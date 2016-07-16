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

package net.fc2.data;

import net.fc2.gui.LaunchFrame;
import net.fc2.log.Logger;
import net.fc2.util.ErrorUtils;
import net.fc2.util.OSUtils;
import net.fc2.util.OSUtils.OS;
import net.fc2.util.winreg.JavaFinder;
import net.fc2.util.winreg.JavaInfo;

import java.io.*;
import java.util.Properties;

public class Settings extends Properties {

    private static Settings settings;

    private static JavaInfo currentJava = null;
    private File configFile;
    boolean forceUpdateEnabled = false;

    static {
        try {
            settings = new Settings(new File(OSUtils.getDynamicStorageLocation(), "fc2launch.cfg"));
        } catch (IOException e) {
            Logger.logError("Failed to load settings", e);
        }
    }

    public Settings(File file) throws IOException {
        configFile = file;
        if (file.exists()) {
            load(new FileInputStream(file));
        } else {
            LaunchFrame.noConfig = true;
        }
    }

    public static Settings getSettings() {
        return Settings.settings;
    }

    public void save() {
        try {
            FileOutputStream fos = new FileOutputStream(configFile);
            store(fos, "FriendsCraft2 launcher Config File");
            fos.close();
        } catch (IOException e) {
            Logger.logError("Failed to save settings", e);
        }
    }

    public String getRamMax() {
        if (getCurrentJava().is64bits && OSUtils.getOSTotalMemory() > 6144)//6gb or more default to 2gb of ram for MC
            return getProperty("ramMax", Integer.toString(2048));
        else if (getCurrentJava().is64bits)//on 64 bit java default to 1.5gb newer pack's need more than a gig
            return getProperty("ramMax", Integer.toString(1536));
        return getProperty("ramMax", Integer.toString(1024));
    }

    public void setRamMax(String max) {
        setProperty("ramMax", max);
    }

    public String getInstallPath() {
        return getProperty("installPath", OSUtils.getDefInstallPath());
    }

    public void setInstallPath(String path) {
        setProperty("installPath", path);
    }

    public String getJavaPath() {
        String javaPath = getProperty("javaPath", null);
        if (javaPath == null || !new File(javaPath).isFile())
            remove("javaPath");

        javaPath = getProperty("javaPath", getDefaultJavaPath());
        if (javaPath == null || !new File(javaPath).isFile())
            ErrorUtils.tossError("Unable to find java; point to java executable file in Advanced Options or game will fail to launch.");
        return javaPath;
    }

    /**
     * Returns user selected or automatically selected JVM's
     * JavaInfo object.
     */
    public JavaInfo getCurrentJava() {
        if (currentJava == null) {
            try {
                currentJava = new JavaInfo(getJavaPath());
            } catch (Exception e) {
                Logger.logError("Error while creating JavaInfo", e);
            }
        }
        return currentJava;
    }

    public String getDefaultJavaPath() {
        String separator = System.getProperty("file.separator");
        JavaInfo javaVersion;

        if (OSUtils.getCurrentOS() == OS.MACOSX) {
            javaVersion = JavaFinder.parseJavaVersion();

            if (javaVersion != null && javaVersion.path != null)
                return javaVersion.path;
        } else if (OSUtils.getCurrentOS() == OS.WINDOWS) {
            javaVersion = JavaFinder.parseJavaVersion();

            if (javaVersion != null && javaVersion.path != null)
                return javaVersion.path.replace(".exe", "w.exe");
        }

        return System.getProperty("java.home") + ("/bin/java" + (OSUtils.getCurrentOS() == OS.WINDOWS ? "w" : "")).replace("/", separator);
    }

    public File getConfigFile() {
        return configFile;
    }

    public Boolean getUseSystemProxy() {
        return Boolean.valueOf(getProperty("useSystemProxy", "false"));
    }

    public void setUseSystemProxy(Boolean flag) {
        setProperty("useSystemProxy", String.valueOf(flag));
    }

    public String getLastPack() {
        return getProperty("lastPack", ModPack.getPack(0).getDir());
    }

    public void setLastPack(String name) {
        setProperty("lastPack", name);
    }

    public boolean getConsoleActive() {
        return Boolean.valueOf(getProperty("consoleActive", "true"));
    }

    public void setConsoleActive(boolean console) {
        setProperty("consoleActive", String.valueOf(console));
    }

    public boolean getOptJavaArgs() {
        return Boolean.valueOf(getProperty("optJavaArgs", "false"));
    }

    public void setOptJavaArgs(boolean console) {
        setProperty("optJavaArgs", String.valueOf(console));
    }

    public String getPackVer() {
        return getProperty(ModPack.getSelectedPack().getDir(), "Recommended Version");
    }

    public boolean getKeepLauncherOpen() {
        return Boolean.parseBoolean(getProperty("keepLauncherOpen", "false"));
    }

    public void setKeepLauncherOpen(boolean state) {
        setProperty("keepLauncherOpen", String.valueOf(state));
    }

    public boolean getSnooper() {
        return Boolean.parseBoolean(getProperty("snooperDisable", "false"));
    }

    public boolean getLoaded() {
        return Boolean.parseBoolean(getProperty("loaded", "false"));
    }

    public void setLoaded(boolean state) {
        setProperty("loaded", String.valueOf(state));
    }

    public String getAdditionalJavaOptions() {
        return getProperty("additionalJavaOptions", "");
    }

    public boolean isForceUpdateEnabled() {
        return this.forceUpdateEnabled;
    }
}
