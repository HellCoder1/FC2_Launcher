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

package net.fc2.workers;

import net.fc2.gui.LaunchFrame;
import net.fc2.log.Logger;
import net.fc2.util.OSUtils;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

public class CabWorker {

    private final static String USER_AGENT = "FriendsCraft2_Launcher";

    public static String excutePost(String url, String urlParameters) throws Exception {

        URL obj = new URL(url);
        HttpURLConnection con = null;

        con = (HttpURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public static String checkNamePass(String name, String pass) throws Exception {
        System.out.println("asd");
        URL obj = new URL("http://friendscraft2.ru/register/launcherLK.php");
        HttpURLConnection con = null;

        con = (HttpURLConnection) obj.openConnection();
        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes("user=" + URLEncoder.encode(name, "UTF-8") + "&password=" + URLEncoder.encode(pass, "UTF-8") + "&authid=3");
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }

    public static void readUsername() {
        try {
            File lastLogin = new File(OSUtils.getDynamicStorageLocation(), "lastlogin");

            Cipher cipher = getCipher(2, "passwordfile");
            DataInputStream dis;
            if (cipher != null)
                dis = new DataInputStream(new CipherInputStream(new FileInputStream(lastLogin), cipher));
            else {
                dis = new DataInputStream(new FileInputStream(lastLogin));
            }
            LaunchFrame.username.setText(dis.readUTF());
            LaunchFrame.password.setText(dis.readUTF());
            LaunchFrame.savePassword.setSelected(LaunchFrame.password.getPassword().length > 0);
            dis.close();
        } catch (Exception e) {
            Logger.logInfo("Error 228 :D", e);
        }
    }

    public static void writeUsername() {
        try {
            File lastLogin = new File(OSUtils.getDynamicStorageLocation(), "lastlogin");

            Cipher cipher = getCipher(1, "passwordfile");
            DataOutputStream dos;
            if (cipher != null)
                dos = new DataOutputStream(new CipherOutputStream(new FileOutputStream(lastLogin), cipher));
            else {
                dos = new DataOutputStream(new FileOutputStream(lastLogin));
            }
            dos.writeUTF(LaunchFrame.username.getText());
            Logger.logInfo("Save user!");
            dos.writeUTF(LaunchFrame.savePassword.isSelected() ? new String(LaunchFrame.password.getPassword()) : "");
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Cipher getCipher(int mode, String password) throws Exception {
        Random random = new Random(43287234L);
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 5);

        SecretKey pbeKey = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(password.toCharArray()));
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(mode, pbeKey, pbeParamSpec);
        return cipher;
    }
}
