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

package net.fc2.download.workers;

import net.fc2.download.info.DownloadInfo;
import net.fc2.download.info.DownloadInfo.DLType;
import net.fc2.log.Logger;
import net.fc2.util.DownloadUtils;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;

public class AssetDownloader extends SwingWorker<Boolean, Void> {
    private List<DownloadInfo> downloads;
    private int progressIndex = 0;
    private boolean allDownloaded = true;
    private ProgressMonitor monitor;

    private String status;
    private int ready = 0;

    public AssetDownloader(final ProgressMonitor monitor, List<DownloadInfo> downloads) {
        this.monitor = monitor;
        this.downloads = downloads;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        for (int x = 0; x < downloads.size(); x++) {
            if (isCancelled()) {
                return false;
            }
            DownloadInfo asset = downloads.get(x);
            doDownload(asset);
        }
        setStatus(allDownloaded ? "Success" : "Downloads failed");
        return allDownloaded;
    }

    public synchronized void setReady(int newReady) {
        int oldReady = ready;
        ready = newReady;
        firePropertyChange("ready", oldReady, ready);
    }

    public synchronized void setStatus(String newStatus) {
        String oldStatus = status;
        status = newStatus;
        firePropertyChange("note", oldStatus, status);
    }

    private void doDownload(DownloadInfo asset) {
        byte[] buffer = new byte[24000];
        boolean downloadSuccess = false;
        String remoteHash = asset.hash;
        int attempt = 0;
        final int attempts = 5;

        while (!downloadSuccess && (attempt < attempts)) {
            try {
                if (attempt++ > 0) {
                    Logger.logInfo("Connecting.. Try " + attempt + " of " + attempts + " for: " + asset.url);
                }

                // Will this break something?
                //HTTPURLConnection con = (HttpURLConnection) asset.url.openConnection();
                URLConnection con = asset.url.openConnection();
                if (con instanceof HttpURLConnection) {
                    con.setRequestProperty("Cache-Control", "no-cache, no-transform");
                    ((HttpURLConnection) con).setRequestMethod("HEAD");
                    con.connect();
                }

                // gather data for basic checks
                long remoteSize = Long.parseLong(con.getHeaderField("Content-Length"));
                if (asset.hash == null && asset.getPrimaryDLType() == DLType.ETag) {
                    remoteHash = con.getHeaderField("ETag").replace("\"", "");
                }
                if (asset.hash == null && asset.getPrimaryDLType() == DLType.ContentMD5) {
                    remoteHash = con.getHeaderField("Content-MD5").replace("\"", "");
                }
//                Logger.logInfo(asset.name);
//                Logger.logInfo("RemoteSize: " + remoteSize);
//                Logger.logInfo("asset.hash: " + asset.hash);
//                Logger.logInfo("remoteHash: " + remoteHash);
                long size = remoteSize/8;
                monitor.setNote(asset.name + "(" + size + ")" );

                // existing file are only added when we want to check file integrity with force update
                if (asset.local.exists()) {
                    long localSize = asset.local.length();
                    if (con instanceof HttpURLConnection && localSize == remoteSize) {
                        ;// size OK
                    } else {
                        asset.local.delete();
                        Logger.logWarn("Local asset size differs from remote size: " + asset.name + " remote: " + remoteSize + " local: " + localSize);
                    }
                }

                if (asset.local.exists()) {
                    doHashCheck(asset, remoteHash);
                }

                if (asset.local.exists()) {
                    downloadSuccess = true;
                    progressIndex += 1;
                    continue;
                }

                //download if needed
                setProgress(0);
                setStatus("Downloading " + asset.name + "...");
                con = asset.url.openConnection();
                if (con instanceof HttpURLConnection) {
                    con.setRequestProperty("Cache-Control", "no-cache, no-transform");
                    ((HttpURLConnection) con).setRequestMethod("GET");
                    con.connect();
                }
                asset.local.getParentFile().mkdirs();
                int readLen;
                int currentSize = 0;
                InputStream input = con.getInputStream();
                FileOutputStream output = new FileOutputStream(asset.local);
                while ((readLen = input.read(buffer, 0, buffer.length)) != -1) {
                    output.write(buffer, 0, readLen);
                    currentSize += readLen;
                    int prog = (int) ((currentSize / remoteSize) * 100);
                    if (prog > 100)
                        prog = 100;
                    if (prog < 0)
                        prog = 0;

                    setProgress(prog);

                    prog = (progressIndex * 100) + prog;

                    setReady(prog);
                    //monitor.setProgress(prog);
                    //monitor.setNote(status);
                }
                input.close();
                output.close();

                //file downloaded check size
                if (con instanceof HttpURLConnection && currentSize > 0 && currentSize == remoteSize) {
                    ;// size OK
                } else {
                    asset.local.delete();
                    Logger.logWarn("Local asset size differs from remote size: " + asset.name + " remote: " + remoteSize + " local: " + currentSize);
                }

                if (downloadSuccess = doHashCheck(asset, remoteHash)) {
                    progressIndex += 1;
                }
            } catch (Exception e) {
                downloadSuccess = false;
                Logger.logWarn("Connection failed, trying again", e);
            }
        }
        if (!downloadSuccess) {
            allDownloaded = false;
        }
    }

    public boolean doHashCheck(DownloadInfo asset, String remoteHash) throws IOException {
        String hash = DownloadUtils.fileHash(asset.local, asset.hashType).toLowerCase();
        String assetHash = asset.hash;
        if (asset.hash == null) {
            if (remoteHash != null) {
                assetHash = remoteHash;
            } else if (asset.getBackupDLType() == DLType.FTBBackup && DownloadUtils.backupIsValid(asset.local, asset.url.getPath().replace("/FTB2", ""))) {
                remoteHash = asset.hash;
            }
        }
        if ((hash != null && !hash.toLowerCase().equals(assetHash))) {
            Logger.logWarn("Asset hash checking failed: " + asset.name + " " + asset.hashType + " " + hash);
            asset.local.delete();
            return false;
        } else {
            return true;
        }

    }

    public String getStatus() {
        return this.status;
    }

    public int getReady() {
        return this.ready;
    }
}
