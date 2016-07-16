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

package net.fc2.util;

import net.fc2.download.Locations;
import net.fc2.log.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Scanner;

public class DownloadUtils extends Thread {

    public static String getFriendsCraftLink(String file) throws NoSuchAlgorithmException {
        String resolved = Locations.masterRepo + "/launcher_files/" + file;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            connection.setRequestProperty("Cache-Control", "no-transform");
                if (connection.getResponseCode() != 200)
                    connection = (HttpURLConnection) new URL(resolved).openConnection();
        } catch (IOException e) {
        }
        connection.disconnect();
        return resolved;
    }

    public static String getStaticFriendsCraftLink(String file) {
        String resolved = Locations.masterRepo + "/launcher_files/static/" + file;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            connection.setRequestProperty("Cache-Control", "no-transform");
            if (connection.getResponseCode() != 200) {
                if (connection.getResponseCode() != 200)
                connection = (HttpURLConnection) new URL(resolved).openConnection();
            }
        } catch (IOException e) {
        }
        connection.disconnect();
        return resolved;
    }

    public static String getStaticFriendsCraftOrBackup(String file, String backupLink) {
        String resolved = Locations.masterRepo + "/launcher_files/static/" + file;
        HttpURLConnection connection = null;
        boolean good = false;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            connection.setRequestProperty("Cache-Control", "no-transform");
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() != 200) {
                    if (connection.getResponseCode() != 200) {
                        connection = (HttpURLConnection) new URL(resolved).openConnection();
                        connection.setRequestMethod("HEAD");
                    } else {
                        if (connection.getResponseCode() == 200)
                            good = true;
                    }

            } else if (connection.getResponseCode() == 200) {
                good = true;
            }
        } catch (IOException e) {
        }
        connection.disconnect();
        if (good)
            return resolved;
        else {
            Logger.logWarn("Using backupLink for " + file);
            return backupLink;
        }
    }

    public static void downloadToFile(URL url, File file) throws IOException {
        file.getParentFile().mkdirs();
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, 1 << 24);
        fos.close();
    }

    public static boolean backupIsValid(File file, String url) throws IOException {
        Logger.logInfo("Issue with new md5 method, attempting to use backup method.");
        String content = null;
        Scanner scanner = null;
        String resolved = Locations.masterRepo + "/launcher_files/" + url;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(resolved).openConnection();
            connection.setRequestProperty("Cache-Control", "no-transform");
            int response = connection.getResponseCode();
            if (response == 200) {
                scanner = new Scanner(connection.getInputStream());
                scanner.useDelimiter("\\Z");
                content = scanner.next();
            }
            if (response != 200 || (content == null || content.isEmpty())) {
                    resolved = Locations.masterRepo + "/launcher_files/" + url.replace("/", "%5E");
                    connection = (HttpURLConnection) new URL(resolved).openConnection();
                    connection.setRequestProperty("Cache-Control", "no-transform");
                    response = connection.getResponseCode();
                    if (response == 200) {
                        scanner = new Scanner(connection.getInputStream());
                        scanner.useDelimiter("\\Z");
                        content = scanner.next();
                    }
            }
        } catch (IOException e) {
        } finally {
            connection.disconnect();
            if (scanner != null) {
                scanner.close();
            }
        }
        String result = fileMD5(file);
        Logger.logInfo("Local: " + result.toUpperCase());
        Logger.logInfo("Remote: " + content.toUpperCase());
        return content.equalsIgnoreCase(result);
    }

    public static String fileMD5(File file) throws IOException {
        return fileHash(file, "md5");
    }

    public static String fileSHA(File file) throws IOException {
        return fileHash(file, "sha1").toLowerCase();
    }

    public static String fileHash(File file, String type) throws IOException {
        if (!file.exists()) {
            return "";
        }
        URL fileUrl = file.toURI().toURL();
        MessageDigest dgest = null;
        try {
            dgest = MessageDigest.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
        }
        InputStream str = fileUrl.openStream();
        byte[] buffer = new byte[65536];
        int readLen;
        while ((readLen = str.read(buffer, 0, buffer.length)) != -1) {
            dgest.update(buffer, 0, readLen);
        }
        str.close();
        Formatter fmt = new Formatter();
        for (byte b : dgest.digest()) {
            fmt.format("%02X", b);
        }
        String result = fmt.toString();
        fmt.close();
        return result;
    }
}