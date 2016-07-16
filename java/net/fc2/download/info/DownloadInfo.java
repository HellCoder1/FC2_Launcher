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

package net.fc2.download.info;

import java.io.File;
import java.net.URL;

public class DownloadInfo {
    public URL url;
    public File local;
    public String name;
    public long size = 0;
    public String hash;
    public String hashType;
    private DLType primaryDLType = DLType.ETag;
    private DLType backupDLType = DLType.NONE;

    public DownloadInfo() {
    }

    public DownloadInfo(URL url, File local, String name, Boolean ftbServers) {
        this(url, local, name, null, "md5");
        if (ftbServers) {
            primaryDLType = DLType.ContentMD5;
            backupDLType = DLType.FTBBackup;
        }
    }

    public DownloadInfo(URL url, File local, String name) {
        this(url, local, name, null, "md5");
    }

    public DownloadInfo(URL url, File local, String name, String hash, String hashType) {
        this.url = url;
        this.local = local;
        this.name = name;
        this.hash = hash;
        this.hashType = hashType;
    }

    public DLType getPrimaryDLType() {
        return this.primaryDLType;
    }

    public DLType getBackupDLType() {
        return this.backupDLType;
    }

    public enum DLType {
        ETag, ContentMD5, FTBBackup, NONE
    }
}