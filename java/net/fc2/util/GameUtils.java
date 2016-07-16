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

import net.fc2.gui.LaunchFrame;
import net.fc2.gui.dialogs.YNDialog;
import net.fc2.log.Logger;

public class GameUtils {
    public static void killMC() {
        //if Mc is running
        if (LaunchFrame.MCRunning) {
            //open confirm dialog for closing MC
            YNDialog yn = new YNDialog("KILL_MC_MESSAGE", "KILL_MC_CONFIRM", "KILL_MC_TITLE");
            yn.setVisible(true);
            yn.toFront();

            if (yn.ready && yn.ret && LaunchFrame.MCRunning && LaunchFrame.getProcMonitor() != null) {
                Logger.logWarn("MC Killed by the user!");
                LaunchFrame.getProcMonitor().stop();
            }

            yn.setVisible(false);
        } else {
            Logger.logInfo("No Minecraft Process currently running to kill");
        }
    }
}
