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

package net.friendscraft_2_launch.launcher.json.versions;

import net.fc2.data.Settings;
import net.fc2.download.Locations;

import java.util.List;
import java.util.Map;

public class Library {
    public String name;
    public List<OSRule> rules;
    public Map<OS, String> natives;
    public ExtractRule extract;
    public String url;
    public boolean localRepo;//when true the DL will be grabbed from the FTB Repo's

    private Action _applies = null;
    private Artifact _artifact = null;

    public boolean applies() {
        if (_applies == null) {
            _applies = Action.DISALLOW;
            if (rules == null) {
                _applies = Action.ALLOW;
            } else {
                for (OSRule rule : rules) {
                    if (rule.applies())
                        _applies = rule.action;
                }
            }
        }
        return _applies == Action.ALLOW;
    }

    public String getPath() {
        if (_artifact == null) {
            _artifact = new Artifact(name);
        }
        return _artifact.getPath();
    }

    public String getPathNatives() {
        if (natives == null)
            return null;
        if (_artifact == null) {
            _artifact = new Artifact(name);
        }
        return _artifact.getPath(natives.get(OS.CURRENT).replace("${arch}", (Settings.getSettings().getCurrentJava().is64bits ? "64" : "32")));
    }

    public String getUrl() {
        return (url == null ? (localRepo ? "http://ftb.cursecdn.com/FTB2/" + Locations.maven : Locations.mc_libs) : url);
    }

    @Override
    public String toString() {
        return name;
    }

    private class Artifact {
        private String domain;
        private String name;
        private String version;
        private String classifier;
        private String ext = "jar";

        public Artifact(String rep) {
            String[] pts = rep.split(":");
            int idx = pts[pts.length - 1].indexOf('@');
            if (idx != -1) {
                ext = pts[pts.length - 1].substring(idx + 1);
                pts[pts.length - 1] = pts[pts.length - 1].substring(0, idx);
            }
            domain = pts[0];
            name = pts[1];
            version = pts[2];
            if (pts.length > 3)
                classifier = pts[3];
        }

        public String getPath() {
            return getPath(classifier);
        }

        public String getPath(String classifier) {
            String ret = String.format("%s/%s/%s/%s-%s", domain.replace('.', '/'), name, version, name, version);
            if (classifier != null)
                ret += "-" + classifier;
            return ret + "." + ext;
        }
    }
}
