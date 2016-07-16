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

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.Setter;
import net.fc2.data.events.ModPackListener;
import net.fc2.gui.panes.ModpacksPane;
import net.fc2.log.Logger;
import net.fc2.util.DownloadUtils;
import net.fc2.util.OSUtils;
import net.fc2.workers.ModpackLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ModPack {
    public final static ArrayList<ModPack> packs = new ArrayList<ModPack>();
    private static List<ModPackListener> listeners = new ArrayList<ModPackListener>();
    private String name, version, url, dir, mcVersion, logoName, imageName, info, maxPermSize, sep = File.separator, xml, message, beta;
    private Image logo, image;
    private int index;
    private boolean updated = false;
    private int[] minJRE;

    public ModPack(String name, String version, String logo, String url, String image, String dir, String mcVersion, String info,
                   String maxPermSize, int idx, String xml, String minJRE, String message, String beta) throws IOException, NoSuchAlgorithmException {
        index = idx;
        this.name = name;
        this.version = version;
        this.dir = dir;
        this.mcVersion = mcVersion;
        this.url = url;
        this.xml = xml;
        this.maxPermSize = maxPermSize;
        this.message = message;
        this.beta = beta;
        String[] tempJRE = minJRE.split("\\.");
        List<Integer> tmpIJre = Lists.newArrayList();
        for (int i = 0; i < tempJRE.length; i++) {
            tmpIJre.add(Integer.parseInt(tempJRE[i]));
        }
        this.minJRE = Ints.toArray(tmpIJre);
        logoName = logo;
        imageName = image;
        this.info = info;
        String installPath = OSUtils.getCacheStorageLocation();
        File tempDir = new File(installPath, "ModPacks" + sep + dir);
        File verFile = new File(tempDir, "version");
        URL url_;
        if (!upToDate(verFile)) {
            url_ = new URL(DownloadUtils.getStaticFriendsCraftLink(logo));
            this.logo = Toolkit.getDefaultToolkit().createImage(url_);
            BufferedImage tempImg = ImageIO.read(url_);
            ImageIO.write(tempImg, "png", new File(tempDir, logo));
            tempImg.flush();
            url_ = new URL(DownloadUtils.getStaticFriendsCraftLink(image));
            this.image = Toolkit.getDefaultToolkit().createImage(url_);
            tempImg = ImageIO.read(url_);
            ImageIO.write(tempImg, "png", new File(tempDir, image));
            tempImg.flush();
        } else {
            if (new File(tempDir, logo).exists()) {
                this.logo = Toolkit.getDefaultToolkit().createImage(tempDir.getPath() + sep + logo);
            } else {
                url_ = new URL(DownloadUtils.getStaticFriendsCraftLink(logo));
                this.logo = Toolkit.getDefaultToolkit().createImage(url_);
                BufferedImage tempImg = ImageIO.read(url_);
                ImageIO.write(tempImg, "png", new File(tempDir, logo));
                tempImg.flush();
            }
            if (new File(tempDir, image).exists()) {
                this.image = Toolkit.getDefaultToolkit().createImage(tempDir.getPath() + sep + image);
            } else {
                url_ = new URL(DownloadUtils.getStaticFriendsCraftLink(image));
                this.image = Toolkit.getDefaultToolkit().createImage(url_);
                BufferedImage tempImg = ImageIO.read(url_);
                ImageIO.write(tempImg, "png", new File(tempDir, image));
                tempImg.flush();
            }
        }
    }

    /**
     * Loads the modpack.xml and adds it to the modpack array in this class
     */
    public static void loadXml(ArrayList<String> xmlFile) {
        ModpackLoader loader = new ModpackLoader(xmlFile);
        loader.start();
    }

    public static void loadXml(String xmlFile) {
        ArrayList<String> temp = new ArrayList<String>();
        temp.add(xmlFile);
        ModpackLoader loader = new ModpackLoader(temp);
        loader.start();
    }

    /**
     * Adds a listener to the listeners array
     *
     * @param listener - the ModPackListener to add
     */
    public static void addListener(ModPackListener listener) {
        packs.clear();
        listeners.clear();
        listeners.add(listener);
    }

    /**
     * Adds modpack to the modpacks array
     *
     * @param pack - a ModPack instance
     */
    public static void addPack(ModPack pack) {
        synchronized (packs) {
            packs.add(pack);
        }
        for (ModPackListener listener : listeners) {
            listener.onModPackAdded(pack);
        }
    }

    public static void clearPacks() {
        packs.clear();
    }

    public static void removePacks(String xml) {
        ArrayList<ModPack> remove = new ArrayList<ModPack>();
        int removed = -1; // TODO: if private xmls ever contain more than one modpack, we need to change this
        for (ModPack pack : packs) {
            if (pack.getParentXml().equalsIgnoreCase(xml)) {
                remove.add(pack);
            }
        }
        for (ModPack pack : remove) {
            removed = pack.getIndex();
            packs.remove(pack);
        }
        for (ModPack pack : packs) {
            if (removed != -1 && pack.getIndex() > removed) {
                pack.setIndex(pack.getIndex() - 1);
            }
        }
    }

    /**
     * Used to get the List of modpacks
     *
     * @return - the array containing all the modpacks
     */
    public static ArrayList<ModPack> getPackArray() {
        return packs;
    }

    /**
     * Gets the ModPack form the array and the given index
     *
     * @param i - the value in the array
     * @return - the ModPack based on the i value
     */
    public static ModPack getPack(int i) {
        return packs.get(i);
    }

    public static ModPack getPack(String dir) {
        for (ModPack pack : packs) {
            if (pack.getDir().equalsIgnoreCase(dir)) {
                return pack;
            }
        }
        return null;
    }

    /**
     * Used to grab the currently selected ModPack based off the selected index from ModPacksPane
     *
     * @return ModPack - the currently selected ModPack
     */
    public static ModPack getSelectedPack() {
        return getPack(ModpacksPane.getSelectedModIndex());
    }

    public static void setVanillaPackMCVersion(String string) {
        for (int i = 0; i < packs.size(); i++) {
            if (packs.get(i).getDir().equals("mojang_vanilla")) {
                ModPack temp = packs.get(i);
                temp.setMcVersion(string);
                packs.remove(i);
                packs.add(i, temp);
                return;
            }
        }
    }

    /**
     * Used to check if the cached items are up to date
     *
     * @param verFile - the version file to check
     * @return checks the version file against the current modpack version
     */
    private boolean upToDate(File verFile) {
        String storedVersion = getStoredVersion(verFile).replace(".", "");
        int storedVersion_ = -1;
        if (!storedVersion.isEmpty()) {
            try {
                storedVersion_ = Integer.parseInt(storedVersion);
            } catch (NumberFormatException e) {
                Logger.logWarn("Automatically fixing malformed version file for " + name, e);
                storedVersion = "";
            }
        }

        if (storedVersion.isEmpty() || storedVersion_ != Integer.parseInt(version.replace(".", ""))) {
            try {
                if (!verFile.exists()) {
                    verFile.getParentFile().mkdirs();
                    verFile.createNewFile();
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(verFile));
                out.write(version);
                out.flush();
                out.close();
                return false;
            } catch (IOException e) {
                Logger.logError(e.getMessage(), e);
                return false;
            }
        }

        return true;
    }

    public boolean needsUpdate(File verFile) {
        return Integer.parseInt(getStoredVersion(verFile).replace(".", "")) != Integer.parseInt(version.replace(".", ""));
    }

    public String getStoredVersion(File verFile) {
        String result = "";
        try {
            if (!verFile.exists()) {
                verFile.getParentFile().mkdirs();
                verFile.createNewFile();
            }
            BufferedReader in = new BufferedReader(new FileReader(verFile));
            String line;
            if ((line = in.readLine()) != null) {
                result = line;
            }
            in.close();
        } catch (IOException e) {
            Logger.logError(e.getMessage(), e);
        }
        return result;
    }


    public String getParentXml() {
        return xml;
    }

}
