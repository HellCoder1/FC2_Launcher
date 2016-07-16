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

package net.fc2.util.winreg;

import com.google.common.collect.Lists;
import net.fc2.log.Logger;
import net.fc2.util.OSUtils;
import net.fc2.util.OSUtils.OS;

import java.io.File;
import java.util.List;

/**
 * Windows-specific java versions finder
 * ***************************************************************************
 */
public class JavaFinder {
    public static boolean java8Found = false;
    private static JavaInfo preferred;
    private static JavaInfo backup;

    /**
     * @param wow64     0 for standard registry access (32-bits for 32-bit app, 64-bits for 64-bits app)
     *                  or WinRegistry.KEY_WOW64_32KEY to force access to 32-bit registry view,
     *                  or WinRegistry.KEY_WOW64_64KEY to force access to 64-bit registry view
     * @param previous: Insert all entries from this list at the beggining of the results
     *                  ***********************************************************************
     * @return: A list of javaExec paths found under this registry key (rooted at HKEY_LOCAL_MACHINE)
     */
    private static List<String> searchRegistry(String key, int wow64, final List<String> previous) {
        List<String> result = previous;
        try {
            List<String> entries = WinRegistry.readStringSubKeys(WinRegistry.HKEY_LOCAL_MACHINE, key, wow64);
            for (int i = 0; entries != null && i < entries.size(); i++) {
                String val = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, key + "\\" + entries.get(i), "JavaHome", wow64);
                if (!result.contains(val + "\\bin\\java.exe")) {
                    result.add(val + "\\bin\\java.exe");
                }
            }
        } catch (Throwable t) {
            Logger.logError("Error Searching windows registry for java versions", t);
        }
        return result;
    }

    /**
     * @return: A list of JavaInfo with informations about all javas installed on this machine
     * Searches and returns results in this order:
     * HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment (32-bits view)
     * HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment (64-bits view)
     * HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit     (32-bits view)
     * HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit     (64-bits view)
     * WINDIR\system32
     * WINDIR\SysWOW64
     * **************************************************************************
     */
    public static List<JavaInfo> findJavas() {
        if (OSUtils.getCurrentOS() == OS.MACOSX)
            return findMacJavas();

        if (OSUtils.getCurrentOS() == OS.WINDOWS)
            return findWinJavas();

        return Lists.newArrayList();
    }

    protected static List<JavaInfo> findWinJavas() {
        List<String> javaExecs = Lists.newArrayList();

        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment", WinRegistry.KEY_WOW64_32KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Runtime Environment", WinRegistry.KEY_WOW64_64KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit", WinRegistry.KEY_WOW64_32KEY, javaExecs);
        javaExecs = JavaFinder.searchRegistry("SOFTWARE\\JavaSoft\\Java Development Kit", WinRegistry.KEY_WOW64_64KEY, javaExecs);

        javaExecs.add(System.getenv("WINDIR") + "\\system32\\java.exe");
        javaExecs.add(System.getenv("WINDIR") + "\\SysWOW64\\java.exe");
        javaExecs.add(System.getProperty("java.home") + "\\bin\\java.exe");

        List<JavaInfo> result = Lists.newArrayList();
        for (String javaPath : javaExecs) {
            if (!(new File(javaPath).exists()))
                continue;
            try {
                result.add(new JavaInfo(javaPath));
            } catch (Exception e) {
                Logger.logError("Error while creating JavaInfo", e);
            }
        }
        return result;
    }

    protected static String getMacJavaPath(String javaVersion) {
        String versionInfo;

        versionInfo = RuntimeStreamer.execute(new String[]{"/usr/libexec/java_home", "-v " + javaVersion});

        // Unable to find any JVMs matching version "1.7"
        if (versionInfo.contains("version \"" + javaVersion + "\"")) {
            return null;
        }

        return versionInfo.trim();
    }

    protected static List<JavaInfo> findMacJavas() {
        List<String> javaExecs = Lists.newArrayList();
        String javaVersion;

        javaVersion = getMacJavaPath("1.6");
        if (javaVersion != null)
            javaExecs.add(javaVersion + "/bin/java");

        javaVersion = getMacJavaPath("1.7");
        if (javaVersion != null)
            javaExecs.add(javaVersion + "/bin/java");

        javaVersion = getMacJavaPath("1.8");
        if (javaVersion != null)
            javaExecs.add(javaVersion + "/bin/java");

        javaExecs.add("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
        javaExecs.add(System.getProperty("java.home") + "/bin/java");

        List<JavaInfo> result = Lists.newArrayList();
        for (String javaPath : javaExecs) {
            File javaFile = new File(javaPath);
            if (!javaFile.exists() || !javaFile.canExecute())
                continue;

            try {
                result.add(new JavaInfo(javaPath));
            } catch (Exception e) {
                Logger.logError("Error while creating JavaInfo", e);
            }
        }
        return result;
    }

    /**
     * @return: The path to a java.exe that has the same bitness as the OS
     * (or null if no matching java is found)
     * **************************************************************************
     */
    public static String getOSBitnessJava() {
        boolean isOS64 = OSUtils.is64BitWindows();

        List<JavaInfo> javas = JavaFinder.findJavas();
        for (JavaInfo java : javas) {
            if (java.is64bits == isOS64)
                return java.path;
        }
        return null;
    }

    public static JavaInfo parseJavaVersion() {
        // TODO: add command line argument to control this
        return parseJavaVersion(true);
    }

    /**
     * Standalone testing - lists all Javas in the system
     * **************************************************************************
     */
    public static JavaInfo parseJavaVersion(boolean canUseJava8) {
        if (preferred == null) {
            List<JavaInfo> javas = JavaFinder.findJavas();
            List<JavaInfo> java32 = Lists.newArrayList();
            List<JavaInfo> java64 = Lists.newArrayList();

            Logger.logInfo("The FriendsCraft2 launcher Launcher has found the following Java versions installed:");
            for (JavaInfo java : javas) {
                Logger.logInfo(java.toString());
                if (java.isJava8()) {
                    java8Found = true;
                }
                if (java.supportedVersion) {
                    if (preferred == null)
                        preferred = java;
                    if (java.is64bits)
                        java64.add(java);
                    else
                        java32.add(java);
                }
            }

            if (java64.size() > 0) {
                for (JavaInfo aJava64 : java64) {
                    if (!preferred.is64bits || aJava64.compareTo(preferred) == 1)
                        preferred = aJava64;
                    if (backup == null && !aJava64.isJava8())
                        backup = aJava64;
                    if (backup != null && !aJava64.isJava8() && aJava64.compareTo(backup) == 1)
                        backup = aJava64;
                }
            }

            if (java32.size() > 0) {
                for (JavaInfo aJava32 : java32) {
                    if (!preferred.is64bits && aJava32.compareTo(preferred) == 1)
                        preferred = aJava32;
                    if (backup == null && !aJava32.isJava8())
                        backup = aJava32;
                    if (backup != null && !aJava32.isJava8() && aJava32.compareTo(backup) == 1)
                        backup = aJava32;

                }
            }
            Logger.logInfo("Preferred: " + preferred.toString());
            //Logger.logDebug("Backup: " + backup.toString());
        }

        if (preferred != null) {
            if (backup != null && preferred.isJava8() && !canUseJava8) {
                Logger.logDebug("Selected JVM: " + backup.toString());
                return backup;
            } else {
                Logger.logDebug("Selected JVM: " + preferred.toString());
                return preferred;
            }
        } else {
            Logger.logError("No Java versions found!");
            return null;
        }
    }
}
