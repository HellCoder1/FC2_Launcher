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

import net.fc2.data.ModPack;
import net.fc2.data.Settings;
import net.fc2.log.Logger;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MinecraftVersionDetector {
    public MinecraftVersionDetector() {
    }

    /**
     * Finds out using some clever tricks the current minecraft version version
     *
     * @param jarFilePath The .minecraft directory
     * @return The version of the jar file
     */
    public String getMinecraftVersion(String jarFilePath) {
        try {
            ZipInputStream file = new ZipInputStream(new FileInputStream(new File(jarFilePath, "bin/" + "minecraft.jar")));
            ZipEntry ent;
            ent = file.getNextEntry();
            while (ent != null) {
                if (ent.getName().contains("Minecraft.class")) {
                    StringBuilder sb = new StringBuilder();
                    for (int c = file.read(); c != -1; c = file.read()) {
                        sb.append((char) c);
                    }
                    String data = sb.toString();
                    String search = "Minecraft 1";
                    file.closeEntry();
                    file.close();
                    return data.substring(data.indexOf(search) + 10, data.indexOf(search) + search.length() + 4);
                }
                file.closeEntry();
                ent = file.getNextEntry();
            }
            file.close();
        } catch (IOException e1) {
            Logger.logError(e1.getMessage(), e1);
            return "unknown";
        }
        return "unknown";
    }

    public boolean shouldUpdate(String jarFilePath) {
        String requiredVersion = ModPack.getSelectedPack().getMcVersion();
        if (Settings.getSettings().isForceUpdateEnabled()) {
            return true;
        }
        String version = getMinecraftVersion(jarFilePath);
        if (version.equals("unknown")) {
            return false;
        }
        File mcVersion = new File(jarFilePath, "bin/version");
        if (mcVersion.exists()) {
            BufferedReader in;
            try {
                in = new BufferedReader(new FileReader(mcVersion));
                requiredVersion = in.readLine();
                in.close();
            } catch (IOException e) {
            }
        }
        Logger.logInfo("Current: " + version);
        Logger.logInfo("Required: " + requiredVersion);
        ModPack.getSelectedPack().setMcVersion(requiredVersion);
        return !version.equals(requiredVersion);
    }
}
