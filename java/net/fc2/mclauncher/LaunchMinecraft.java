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
package net.fc2.mclauncher;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import net.fc2.data.ModPack;
import net.fc2.data.Settings;
import net.fc2.log.Logger;
import net.fc2.util.DownloadUtils;
import net.fc2.util.FileUtils;
import net.fc2.util.OSUtils;
import net.friendscraft_2_launch.launcher.json.JsonFactory;
import net.friendscraft_2_launch.launcher.json.assets.AssetIndex;
import net.friendscraft_2_launch.launcher.json.assets.AssetIndex.Asset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class LaunchMinecraft {
    public static Process launchMinecraft(String javaPath, File gameDir, File assetDir, File nativesDir, List<File> classpath, String username, String password, String mainClass, String args,
                                          String assetIndex, String rmax, String maxPermSize, String version) throws IOException {

        assetDir = syncAssets(assetDir, assetIndex);

        StringBuilder cpb = new StringBuilder("");
        for (File f : classpath) {
            cpb.append(OSUtils.getJavaDelimiter());
            cpb.append(f.getAbsolutePath());
        }
        //Logger.logInfo("ClassPath: " + cpb.toString());

        List<String> arguments = new ArrayList<String>();

        Logger.logInfo("Java Path: " + javaPath);
        Logger.logInfo("Pack: " + ModPack.getSelectedPack().getName() + " " + version);
        arguments.add(javaPath);

        setMemory(arguments, rmax);

        if (OSUtils.getCurrentOS().equals(OSUtils.OS.WINDOWS)) {
            if (!OSUtils.is64BitWindows()) {
                if (maxPermSize == null || maxPermSize.isEmpty()) {
                    if (OSUtils.getOSTotalMemory() > 2046) {
                        maxPermSize = "192m";
                        Logger.logInfo("Defaulting PermSize to 192m");
                    } else {
                        maxPermSize = "128m";
                        Logger.logInfo("Defaulting PermSize to 128m");
                    }
                }
            }
        }

        if (maxPermSize == null || maxPermSize.isEmpty()) {
            // 64-bit or Non-Windows
            maxPermSize = "256m";
            Logger.logInfo("Defaulting PermSize to 256m");
        }

        //arguments.add("-XX:+UseConcMarkSweepGC");
        //arguments.add("-XX:+CMSIncrementalMode");
        //arguments.add("-XX:+AggressiveOpts");
        //arguments.add("-XX:+CMSClassUnloadingEnabled");
        arguments.add("-XX:PermSize=" + maxPermSize);
        arguments.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
        arguments.add("-Dorg.lwjgl.librarypath=" + nativesDir.getAbsolutePath());
        arguments.add("-Dnet.java.games.input.librarypath=" + nativesDir.getAbsolutePath());
        arguments.add("-Duser.home=" + gameDir.getParentFile().getAbsolutePath());

        // Use IPv4 when possible, only use IPv6 when connecting to IPv6 only addresses
        arguments.add("-Djava.net.preferIPv4Stack=true");

        if (Settings.getSettings().getUseSystemProxy()) {
            arguments.add("-Djava.net.useSystemProxies=true");
        }
        arguments.add("-cp");
        arguments.add(cpb.toString() + System.getProperty("java.class.path"));//our libs come 1st!

        String additionalOptions = Settings.getSettings().getAdditionalJavaOptions();
        if (!additionalOptions.isEmpty()) {
            Logger.logInfo("Additional java parameters: " + additionalOptions);
            Collections.addAll(arguments, additionalOptions.split("\\s+"));
        }
        if (Settings.getSettings().getOptJavaArgs()) {
            Logger.logInfo("Adding Optimization Arguments");
            Collections.addAll(arguments, "-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CICompilerCountPerCPU -XX:+TieredCompilation".split("\\s+"));
        }

        //Undocumented environment variable to control JVM
        String additionalEnvVar = System.getenv("_JAVA_OPTIONS");
        if (additionalEnvVar != null && !additionalEnvVar.isEmpty()) {
            Logger.logInfo("_JAVA_OPTIONS defined: " + additionalEnvVar);
        }
        //Documented environment variable to control JVM
        additionalEnvVar = System.getenv("JAVA_TOOL_OPTIONS");
        if (additionalEnvVar != null && !additionalEnvVar.isEmpty()) {
            Logger.logInfo("JAVA_TOOL_OPTIONS defined: " + additionalEnvVar);
        }

        arguments.add(mainClass);
        for (String s : args.split(" ")) {
            if (s.equals("${auth_player_name}"))
                arguments.add(username);
            else if (s.equals("${auth_session}") || s.equals("${auth_access_token}"))
                arguments.add(password);
            else if (s.equals("${version_name}"))
                arguments.add(version);
            else if (s.equals("${game_directory}"))
                arguments.add(gameDir.getAbsolutePath());
            else if (s.equals("${game_assets}") || s.equals("${assets_root}"))
                arguments.add(assetDir.getAbsolutePath());
            else if (s.equals("${assets_index_name}"))
                arguments.add(assetIndex);
            else if (s.equals("${auth_uuid}"))
                arguments.add("12345");
            else if (s.equals("${user_properties}"))
                arguments.add("{}");
            else
                arguments.add(s);
        }
        ProcessBuilder builder = new ProcessBuilder(arguments);
        /*StringBuilder tmp = new StringBuilder();
        for (String a : builder.command())
            tmp.append(a).append(' ');
        Logger.logInfo("Launching: " + tmp.toString());*/
        builder.directory(gameDir);
        builder.redirectErrorStream(true);
        OSUtils.cleanEnvVars(builder.environment());
        return builder.start();
    }

    private static void setMemory(List<String> arguments, String rmax) {
        boolean memorySet = false;
        try {
            int min = 256;
            if (rmax != null && Integer.parseInt(rmax) > 0) {
                arguments.add("-Xms" + min + "M");
                Logger.logInfo("Setting MinMemory to " + min);
                arguments.add("-Xmx" + rmax + "M");
                Logger.logInfo("Setting MaxMemory to " + rmax);
                memorySet = true;
            }
        } catch (Exception e) {
            Logger.logError("Error parsing memory settings", e);
        }
        if (!memorySet) {
            arguments.add("-Xms" + 256 + "M");
            Logger.logInfo("Defaulting MinMemory to " + 256);
            arguments.add("-Xmx" + 1024 + "M");
            Logger.logInfo("Defaulting MaxMemory to " + 1024);
        }
    }

    private static File syncAssets(File assetDir, String indexName) throws JsonSyntaxException, JsonIOException, IOException {
        Logger.logInfo("Syncing Assets:");
        File objects = new File(assetDir, "objects");
        AssetIndex index = JsonFactory.loadAssetIndex(new File(assetDir, "indexes/{INDEX}.json".replace("{INDEX}", indexName)));

        if (!index.virtual)
            return assetDir;

        File targetDir = new File(assetDir, "virtual/" + indexName);

        Set<File> old = FileUtils.listFiles(targetDir);

        for (Entry<String, Asset> e : index.objects.entrySet()) {
            Asset asset = e.getValue();
            File local = new File(targetDir, e.getKey());
            File object = new File(objects, asset.hash.substring(0, 2) + "/" + asset.hash);

            old.remove(local);

            if (local.exists() && !DownloadUtils.fileSHA(local).equals(asset.hash)) {
                Logger.logInfo("  Changed: " + e.getKey());
                FileUtils.copyFile(object, local, true);
            } else if (!local.exists()) {
                Logger.logInfo("  Added: " + e.getKey());
                FileUtils.copyFile(object, local);
            }
        }

        for (File f : old) {
            String name = f.getAbsolutePath().replace(targetDir.getAbsolutePath(), "");
            Logger.logInfo("  Removed: " + name.substring(1));
            f.delete();
        }

        return targetDir;
    }
}