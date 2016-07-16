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

import net.fc2.gui.LaunchFrame;
import net.fc2.log.Logger;
import net.fc2.util.AppUtils;
import net.fc2.util.DownloadUtils;
import net.fc2.util.FileUtils;
import net.fc2.util.OSUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

public class UpdateChecker extends SwingWorker<Boolean, Void> {
    public static String verString = "";
    private int version;
    private int latest;
    private String downloadAddress = "";

    public UpdateChecker(int version) {
        this.version = version;
    }

    @Override
    protected Boolean doInBackground() {
        //try{Thread.sleep(10000);}catch (Exception e){}
        loadInfo();
        try {
            FileUtils.delete(new File(OSUtils.getCacheStorageLocation(), "updatetemp"));
        } catch (Exception ignored) {
            Logger.logError(ignored.getMessage(), ignored);
        }
        return this.shouldUpdate();
    }

    private void loadInfo() {
        try {
            Document doc = AppUtils.downloadXML(new URL(DownloadUtils.getStaticFriendsCraftLink("version.xml")));
            if (doc == null) {
                return;
            }
            NamedNodeMap updateAttributes = doc.getDocumentElement().getAttributes();
            latest = Integer.parseInt(updateAttributes.getNamedItem("currentBuild").getTextContent());
            char[] temp = String.valueOf(latest).toCharArray();
            for (int i = 0; i < (temp.length - 1); i++) {
                verString += temp[i] + ".";
            }
            verString += temp[temp.length - 1];
            downloadAddress = updateAttributes.getNamedItem("downloadURL").getTextContent();
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }

    public boolean shouldUpdate() {
        if (version < latest) {
            Logger.logInfo("New version found. version: " + version + ", latest: " + latest);
            return true;
        } else {
            return false;
        }
    }

    public void update() {
        String path = null;
        try {
            path = new File(LaunchFrame.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
        } catch (IOException e) {
            Logger.logError("Couldn't get path to current launcher jar/exe", e);
        }
        String temporaryUpdatePath = OSUtils.getCacheStorageLocation() + File.separator + "updatetemp" + File.separator + path.substring(path.lastIndexOf(File.separator) + 1);
        String extension = path.substring(path.lastIndexOf('.') + 1);
        extension = "exe".equalsIgnoreCase(extension) ? extension : "jar";
        try {
            URL updateURL = new URL(DownloadUtils.getFriendsCraftLink(downloadAddress + "." + extension));
            File temporaryUpdate = new File(temporaryUpdatePath);
            temporaryUpdate.getParentFile().mkdir();
            DownloadUtils.downloadToFile(updateURL, temporaryUpdate);
            SelfUpdate.runUpdate(path, temporaryUpdatePath);
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }
}
