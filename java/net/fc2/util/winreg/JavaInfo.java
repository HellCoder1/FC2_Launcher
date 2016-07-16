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

package net.fc2.util.winreg;

import net.fc2.util.OSUtils;
import net.fc2.util.OSUtils.OS;

import java.util.regex.Pattern;

/**
 * Java Finder by petrucio@stackoverflow(828681) is licensed under a Creative Commons Attribution 3.0 Unported License.
 * Needs WinRegistry.java. Get it at: http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 *
 * JavaFinder - Windows-specific classes to search for all installed versions of java on this system
 * Author: petrucio@stackoverflow (828681)
 *****************************************************************************/

/**
 * Helper struct to hold information about one installed java version
 * **************************************************************************
 */
public class JavaInfo implements Comparable<JavaInfo> {
    private static String regex = new String("[^\\d_.-]");
    public String path; //! Full path to java.exe executable file
    public String version; //! Version string.
    public String origVersion = new String();
    public boolean supportedVersion = false;
    public boolean is64bits; //! true for 64-bit javas, false for 32
    private int major, minor, revision, build;

    /**
     * Calls 'javaPath -version' and parses the results
     *
     * @param javaPath: path to a java.exe executable
     *                  **************************************************************************
     */
    public JavaInfo(String javaPath) {
        String versionInfo = RuntimeStreamer.execute(new String[]{javaPath, "-version"});
        String[] tokens = versionInfo.split("\"");
        if (tokens.length < 2)
            this.version = "0.0.0_00";
        else
            this.version = tokens[1];
        this.origVersion = version;
        this.version = Pattern.compile(regex).matcher(this.version).replaceAll("0");
        this.is64bits = versionInfo.toUpperCase().contains("64-");
        this.path = javaPath;

        String[] s = this.version.split("[._-]");
        this.major = Integer.parseInt(s[0]);
        this.minor = s.length > 1 ? Integer.parseInt(s[1]) : 0;
        this.revision = s.length > 2 ? Integer.parseInt(s[2]) : 0;
        this.build = s.length > 3 ? Integer.parseInt(s[3]) : 0;

        if (OSUtils.getCurrentOS() == OS.MACOSX) {
            if (this.major == 1 && (this.minor == 7 || this.minor == 6))
                this.supportedVersion = true;
        } else {
            this.supportedVersion = true;
        }
    }

    public boolean isJava8() {
        return this.major == 1 && this.minor == 8;
    }

    /**
     * @return Human-readable contents of this JavaInfo instance
     * **************************************************************************
     */
    public String toString() {
        return "Java Version: " + origVersion + " sorted as: " + this.verToString() + " " + (this.is64bits ? "64" : "32") + " Bit Java at : " + this.path + (this.supportedVersion ? "" : " (UNSUPPORTED!)");
    }

    public String verToString() {
        return major + "." + minor + "." + revision + "_" + build;
    }

    @Override
    public int compareTo(JavaInfo o) {
        if (o.major > major)
            return -1;
        if (o.major < major)
            return 1;
        if (o.minor > minor)
            return -1;
        if (o.minor < minor)
            return 1;
        if (o.revision > revision)
            return -1;
        if (o.revision < revision)
            return 1;
        if (o.build > build)
            return -1;
        if (o.build < build)
            return 1;
        return 0;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

}
