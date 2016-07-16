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

import net.fc2.data.Map;
import net.fc2.gui.panes.BigMapsDialog;
import net.fc2.log.Logger;
import net.fc2.util.AppUtils;
import net.fc2.util.DownloadUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;

public class MapLoader extends Thread {
    private static String MAPFILE;

    public MapLoader() {
    }

    @Override
    public void run() {
        try {//TODO ASAP THREAD THIS!!!
            Logger.logInfo("loading map information...");
            MAPFILE = DownloadUtils.getStaticFriendsCraftLink("maps.xml");
            Document doc = AppUtils.downloadXML(new URL(MAPFILE));
            if (doc == null) {
                Logger.logError("Error: Could not load map data!");
            }
            NodeList maps = doc.getElementsByTagName("map");
            for (int i = 0; i < maps.getLength(); i++) {
                Node map = maps.item(i);
                NamedNodeMap mapAttr = map.getAttributes();
                Map.addMap(new Map(mapAttr.getNamedItem("name").getTextContent(), mapAttr.getNamedItem("author").getTextContent(), mapAttr.getNamedItem("version").getTextContent(), mapAttr
                        .getNamedItem("url").getTextContent(), mapAttr.getNamedItem("logo").getTextContent(), mapAttr.getNamedItem("compatible")
                        .getTextContent(), mapAttr.getNamedItem("mcversion").getTextContent(), mapAttr.getNamedItem("mapname").getTextContent(), mapAttr.getNamedItem("description").getTextContent(),
                        i));
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        } finally {
            BigMapsDialog.loaded = true;
        }
    }
}
