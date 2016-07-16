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

import net.fc2.gui.LaunchFrame;
import net.fc2.log.Logger;
import net.fc2.util.OSUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PastebinPoster extends Thread {
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        HttpURLConnection conn = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            URL url = new URL("http://paste.feed-the-beast.com/api/create?apikey=b6a30d5f030ce86a2b0723ed0b494cdd");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Cache-Control", "no-transform");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            out = conn.getOutputStream();

            out.write(("text=" + URLEncoder.encode(Logger.getLogs(), "utf-8")
                    + "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]" + " Post created"
                    + "&private=" + URLEncoder.encode("0", "utf-8")
                    + "&title=" + URLEncoder.encode("Version: " + LaunchFrame.version, "utf-8")
                    + "&lang=" + URLEncoder.encode("FTB Logs", "utf-8")
                    + "&name=" + URLEncoder.encode("Launcher")).getBytes());
            out.flush();
            out.close();

            if (conn.getResponseCode() == 200) {
                in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    response.append("\r\n");
                }
                reader.close();
                String result = response.toString().trim();
                if (result.matches("^https?://.*")) {
                    Logger.logInfo(result.trim());
                    OSUtils.browse(result.trim());
                } else {
                    String err = result.trim();
                    if (err.length() > 100) {
                        err = err.substring(0, 100);
                    }
                    Logger.logError(err);
                }
            } else {
                Logger.logError("didn't get a 200 response code!");
            }
        } catch (IOException e) {
            Logger.logError(e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
