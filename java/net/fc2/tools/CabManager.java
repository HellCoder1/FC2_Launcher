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


import lombok.Getter;
import net.fc2.gui.LaunchFrame;
import net.fc2.locale.I18N;
import net.fc2.log.Logger;
import net.fc2.workers.CabWorker;

import java.net.URLEncoder;

public class CabManager {

    @Getter
    private static String balanse;
    @Getter
    private static String name;
    @Getter
    private static String group;
    @Getter
    private static String groupnum;

    @Getter
    private static int games;

    private static String main_url = "http://friendscraft2.ru/register/launcherLK.php";

    public CabManager() {
        super();
    }

    public static void init() {
        setUserName();
        setUserBalanse();
        setUserGroupName();
    }

    public static void setUserName() {
        name = LaunchFrame.getUserName();
    }

    public static void setUserBalanse() {
        try {
            balanse = CabWorker.excutePost(main_url, "user=" + URLEncoder.encode(name, "UTF-8") + "&authid=1");
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }

    public static void setUserGroupName() {
        try {
            groupnum = CabWorker.excutePost(main_url, "user=" + URLEncoder.encode(name, "UTF-8") + "&authid=2");
            group = I18N.getLocaleString("GROUP_NAME_" + groupnum);
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }

    public static void getGameNum(){
        try{
            String value = CabWorker.excutePost(main_url, "user=" + URLEncoder.encode(name, "UTF-8") + "&authid=5");
            games = Integer.valueOf(value);
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }

//    public static void setGameNum(int g){
//        try{
//            CabWorker.excutePost(main_url, "user=" + URLEncoder.encode(name, "UTF-8") + "&authid=6" + "&number=" + g);
//        } catch (Exception e) {
//            Logger.logError(e.getMessage(), e);
//        }
//    }
}
