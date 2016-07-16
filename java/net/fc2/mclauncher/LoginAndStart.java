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

package net.fc2.mclauncher;

import net.fc2.data.LoginResponse;
import net.fc2.data.ModPack;
import net.fc2.data.Settings;
import net.fc2.download.Locations;
import net.fc2.download.info.DownloadInfo;
import net.fc2.download.workers.AssetDownloader;
import net.fc2.gui.LaunchFrame;
import net.fc2.gui.dialogs.ModPackVersionChangeDialog;
import net.fc2.gui.panes.OptionsPane;
import net.fc2.locale.I18N;
import net.fc2.log.LogEntry;
import net.fc2.log.LogLevel;
import net.fc2.log.Logger;
import net.fc2.log.StreamLogger;
import net.fc2.tools.ModManager;
import net.fc2.tools.ProcessMonitor;
import net.fc2.util.*;
import net.fc2.util.winreg.JavaInfo;
import net.fc2.workers.LoginWorker;
import net.friendscraft_2_launch.launcher.json.JsonFactory;
import net.friendscraft_2_launch.launcher.json.assets.AssetIndex;
import net.friendscraft_2_launch.launcher.json.versions.Library;
import net.friendscraft_2_launch.launcher.json.versions.Version;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LoginAndStart {

    public static void doLaunch() {
        JavaInfo java = Settings.getSettings().getCurrentJava();
        int[] minSup = ModPack.getSelectedPack().getMinJRE();

        String user = LaunchFrame.getInstance().username.getText();

        if (ModPack.getSelectedPack() != null) {
            if (minSup.length >= 2 && minSup[0] <= java.getMajor() && minSup[1] <= java.getMinor()) {
                Settings.getSettings().setLastPack(ModPack.getSelectedPack().getDir());
                LaunchFrame.getInstance().saveSettings();
                LoginAndStart.doLogin(LaunchFrame.getInstance().username.getText(), new String(LaunchFrame.getInstance().password.getPassword()));
            } else {//user can't run pack-- JRE not high enough
                ErrorUtils.tossError("You must use at least java " + minSup[0] + "." + minSup[1] + " to play this pack! Please go to Options to get a link or Advanced Options enter a path.");
            }
        } else if (user.isEmpty()) {
            ErrorUtils.tossError(I18N.getLocaleString("PROFILE_ERROR"));
        }
    }

    public static void doLogin(final String username, String password) {

        if (ModPack.getSelectedPack().getBeta() != null && !ModPack.getSelectedPack().getBeta().isEmpty()) {
            ErrorUtils.tossError(ModPack.getSelectedPack().getBeta());
        }

        Logger.logInfo(I18N.getLocaleString("GOOD0"));

        LaunchFrame.getInstance().tabbedPane.setEnabledAt(0, false);
        LaunchFrame.getInstance().tabbedPane.setEnabledAt(1, false);
        LaunchFrame.getInstance().tabbedPane.setEnabledAt(2, false);
        LaunchFrame.getInstance().tabbedPane.getSelectedComponent().setEnabled(false);

        LaunchFrame.getInstance().launch.setEnabled(false);
        LaunchFrame.getInstance().Exit.setEnabled(false);

        LoginWorker loginWorker = new LoginWorker(username, password,LaunchFrame.getInstance(). macSt) {
            @Override
            public void done() {


                String responseStr;
                try {
                    responseStr = get();
                } catch (InterruptedException err) {
                    Logger.logError(err.getMessage(), err);
                    LaunchFrame.getInstance().enableObjects();
                    return;
                } catch (ExecutionException err) {
                    if (err.getCause() instanceof IOException || err.getCause() instanceof MalformedURLException) {
                        Logger.logError(err.getMessage(), err);
                    }
                    LaunchFrame.getInstance().enableObjects();
                    return;
                }
                try {
                    LaunchFrame.getInstance().RESPONSE = new LoginResponse(responseStr);
                } catch (IllegalArgumentException e) {
                    if (responseStr.contains(":")) {
                        Logger.logError("Received invalid response from server.");
                    } else {
                        if (responseStr.equalsIgnoreCase("bad login1")) {
                            ErrorUtils.tossError(I18N.getLocaleString("BAD4"));
                        } else if (responseStr.equalsIgnoreCase("old version")) {
                            ErrorUtils.tossError("пїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅпїЅпїЅпїЅ.");
                        } else {
                            ErrorUtils.tossError(I18N.getLocaleString("BAD4"));
                        }
                    }
                    LaunchFrame.getInstance().enableObjects();
                    return;
                }
                Logger.logInfo(I18N.getLocaleString("GOOD2"));
                runGameUpdater();
            }
        };
        loginWorker.execute();
    }

    private static void runGameUpdater() {

        final String installPath = Settings.getSettings().getInstallPath();
        final ModPack pack = ModPack.getSelectedPack();
        final String debugTag = "runGameUpdater: ";

        Logger.logInfo(debugTag + "ForceUpdate: " + Settings.getSettings().isForceUpdateEnabled());
        Logger.logInfo(debugTag + "installPath: " + installPath);
        Logger.logInfo(debugTag + "pack dir: " + pack.getDir());
        Logger.logInfo(debugTag + "pack check path: " + pack.getDir() + File.separator + "version");

        File verFile = new File(installPath, pack.getDir() + File.separator + "version");

        if (Settings.getSettings().isForceUpdateEnabled() && verFile.exists()) {
            verFile.delete();
            Logger.logInfo(debugTag + "Pack found and delete attempted");
        }

        if (Settings.getSettings().isForceUpdateEnabled() || !verFile.exists() || checkVersion(verFile, pack)) {
            if (LaunchFrame.getInstance().doVersionBackup) {
                try {
                    File destination = new File(OSUtils.getCacheStorageLocation(), "backups" + File.separator + pack.getDir() + File.separator + "config_backup");
                    if (destination.exists()) {
                        FileUtils.delete(destination);
                    }
                    FileUtils.copyFolder(new File(Settings.getSettings().getInstallPath(), pack.getDir() + File.separator + "minecraft" + File.separator + "config"), destination);
                } catch (IOException e) {
                    Logger.logError(e.getMessage(), e);
                }
            }

            if (!initializeMods()) {
                Logger.logInfo(debugTag + "initializeMods: Failed to Init mods! Aborting to menu.");
                LaunchFrame.getInstance().enableObjects();
                return;
            }
        }

        if (pack.getMcVersion().startsWith("1.6") || pack.getMcVersion().startsWith("1.7") || pack.getMcVersion().startsWith("1.8") || pack.getMcVersion().startsWith("14w")) {
            setupNewStyle(installPath, pack);
            return;
        }

    }

    private static Boolean checkVersion(File verFile, ModPack pack) {
        String storedVersion = pack.getStoredVersion(verFile);
        String onlineVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") || Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) ? pack
                .getVersion() : Settings.getSettings().getPackVer();

        if (storedVersion.isEmpty()) {
            // Always allow updates from a version that isn't installed at all
            LaunchFrame.getInstance().allowVersionChange = true;
            return true;
        } else if (Integer.parseInt(storedVersion.replace(".", "")) != Integer.parseInt(onlineVersion.replace(".", ""))) {
            ModPackVersionChangeDialog verDialog = new ModPackVersionChangeDialog(LaunchFrame.getInstance(), true, storedVersion, onlineVersion);
            verDialog.setVisible(true);
        }
        return LaunchFrame.getInstance().allowVersionChange & (!storedVersion.equals(onlineVersion));
    }

    private static void setupNewStyle(final String installPath, final ModPack pack) {
        List<DownloadInfo> assets = gatherAssets(new File(installPath), pack.getMcVersion());
        if (assets == null) {
            ErrorUtils.tossError("Error occurred during downloading the assets");
            LaunchFrame.getInstance(). enableObjects();
        }

        if (assets.size() > 0) {
            Logger.logInfo("Gathering " + assets.size() + " assets, this may take a while...");

            final ProgressMonitor prog = new ProgressMonitor(LaunchFrame.getInstance(), "Downloading assets...", "", 0, 100);
            prog.setMaximum(assets.size() * 100);

            final AssetDownloader downloader = new AssetDownloader(prog, assets) {
                @Override
                public void done() {
                    try {
                        prog.close();
                        if (get()) {
                            Logger.logInfo("Asset downloading complete");
                            launchMinecraftNew(installPath, pack, LaunchFrame.getInstance().RESPONSE);
                        } else {
                            ErrorUtils.tossError("Error occurred during downloading the assets");
                        }
                    } catch (CancellationException e) {
                        Logger.logInfo("Asset download interrupted by user");
                    } catch (Exception e) {
                        ErrorUtils.tossError("Failed to download files.", e);
                    } finally {
                        LaunchFrame.getInstance().enableObjects();
                    }
                }
            };

            downloader.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (prog.isCanceled()) {
                        downloader.cancel(false);
                        prog.close();
                    } else if (!downloader.isCancelled()) {
                        if ("ready".equals(evt.getPropertyName()))
                            prog.setProgress(downloader.getReady());
                        if ("status".equals(evt.getPropertyName()))
                            prog.setNote(downloader.getStatus());
                    }
                }
            });

            downloader.execute();
        } else {
            launchMinecraftNew(installPath, pack, LaunchFrame.getInstance().RESPONSE);
        }
    }

    private static List<DownloadInfo> gatherAssets(File root, String mcVersion) {
        try {
            List<DownloadInfo> list = new ArrayList<DownloadInfo>();
            Boolean forceUpdate = Settings.getSettings().isForceUpdateEnabled();

            /*
             * vanilla minecraft.jar
             */

            File local = new File(root, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", mcVersion));
            if (!local.exists() || forceUpdate) {
                list.add(new DownloadInfo(new URL(Locations.mc_dl + "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", mcVersion)), local, local.getName()));
            }

            /*
             * <ftb installation location>/libraries/*
             */
            //check if our copy exists of the version json if not backup to mojang's copy
            URL url = new URL(DownloadUtils.getStaticFriendsCraftOrBackup("mcjsons/versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion), Locations.mc_dl
                    + "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion)));
            File json = new File(root, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", mcVersion));
            int attempt = 0, attempts = 3;
            boolean success = false;
            while ((attempt < attempts) && !success) {
                try {
                    success = true;
                    DownloadUtils.downloadToFile(url, json);
                } catch (Exception e) {
                    Logger.logError("JSON download failed. Trying download again.", e);
                    success = false;
                    attempt++;
                }
                if (attempt == attempts) {
                    ErrorUtils.tossError("JSON download failed");
                    return null;
                }
            }
            Version version = JsonFactory.loadVersion(json);
            for (Library lib : version.getLibraries()) {
                if (lib.natives == null) {
                    local = new File(root, "libraries/" + lib.getPath());
                    if (!local.exists() || forceUpdate) {
                        if (!lib.getUrl().toLowerCase().contains(Locations.maven)) {
                            list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPath()), local, lib.getPath()));
                        } else {
                            list.add(new DownloadInfo(new URL(DownloadUtils.getStaticFriendsCraftLink(lib.getUrl() + lib.getPath())), local, lib.getPath(), true));
                        }
                    }
                } else {
                    local = new File(root, "libraries/" + lib.getPathNatives());
                    if (!local.exists() || forceUpdate) {
                        list.add(new DownloadInfo(new URL(lib.getUrl() + lib.getPathNatives()), local, lib.getPathNatives()));
                    }

                }
            }

            // Move the old format to the new:
            File test = new File(root, "assets/READ_ME_I_AM_VERY_IMPORTANT.txt");
            if (test.exists()) {
                File assets = new File(root, "assets");
                Set<File> old = FileUtils.listFiles(assets);
                File objects = new File(assets, "objects");
                String[] skip = new String[]{objects.getAbsolutePath(), new File(assets, "indexes").getAbsolutePath(), new File(assets, "virtual").getAbsolutePath()};

                for (File f : old) {
                    String path = f.getAbsolutePath();
                    boolean move = true;
                    for (String prefix : skip) {
                        if (path.startsWith(prefix))
                            move = false;
                    }
                    if (move) {
                        String hash = DownloadUtils.fileSHA(f);
                        File cache = new File(objects, hash.substring(0, 2) + "/" + hash);
                        Logger.logInfo("Caching Asset: " + hash + " - " + f.getAbsolutePath().replace(assets.getAbsolutePath(), ""));
                        if (!cache.exists()) {
                            cache.getParentFile().mkdirs();
                            f.renameTo(cache);
                        }
                        f.delete();
                    }
                }

                List<File> dirs = FileUtils.listDirs(assets);
                for (File dir : dirs) {
                    if (dir.listFiles().length == 0) {
                        dir.delete();
                    }
                }
            }

            /*
             * assets/*
             */
            url = new URL(Locations.mc_dl + "indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));
            json = new File(root, "assets/indexes/{INDEX}.json".replace("{INDEX}", version.getAssets()));
            attempt = 0;
            attempts = 3;
            success = false;
            while ((attempt < attempts) && !success) {
                try {
                    success = true;
                    DownloadUtils.downloadToFile(url, json);
                } catch (Exception e) {
                    Logger.logError("JSON download failed. Trying download again.", e);
                    success = false;
                    attempt++;
                }
                if (attempt == attempts) {
                    ErrorUtils.tossError("JSON download failed");
                    return null;
                }
            }

            AssetIndex index = JsonFactory.loadAssetIndex(json);

            for (Map.Entry<String, AssetIndex.Asset> e : index.objects.entrySet()) {
                String name = e.getKey();
                AssetIndex.Asset asset = e.getValue();
                String path = asset.hash.substring(0, 2) + "/" + asset.hash;
                local = new File(root, "assets/objects/" + path);

                if (local.exists() && !asset.hash.equals(DownloadUtils.fileSHA(local))) {
                    local.delete();
                }

                if (!local.exists()) {
                    list.add(new DownloadInfo(new URL(Locations.mc_res + path), local, name, asset.hash, "sha1"));
                }
            }
            return list;
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
        return null;
    }

    public static void launchMinecraftNew(String installDir, ModPack pack, LoginResponse resp) {
        try {
            File packDir = new File(installDir, pack.getDir());
            File gameDir = new File(packDir, "minecraft");
            File assetDir = new File(installDir, "assets");
            File libDir = new File(installDir, "libraries");
            File natDir = new File(packDir, "natives");

            if (natDir.exists()) {
                natDir.delete();
            }
            natDir.mkdirs();

            Version base = JsonFactory.loadVersion(new File(installDir, "versions/{MC_VER}/{MC_VER}.json".replace("{MC_VER}", pack.getMcVersion())));
            byte[] buf = new byte[1024];
            for (Library lib : base.getLibraries()) {
                if (lib.natives != null) {
                    File local = new File(libDir, lib.getPathNatives());
                    ZipInputStream input = null;
                    try {
                        input = new ZipInputStream(new FileInputStream(local));
                        ZipEntry entry = input.getNextEntry();
                        while (entry != null) {
                            String name = entry.getName();
                            int n;
                            if (lib.extract == null || !lib.extract.exclude(name)) {
                                File output = new File(natDir, name);
                                output.getParentFile().mkdirs();
                                FileOutputStream out = new FileOutputStream(output);
                                while ((n = input.read(buf, 0, 1024)) > -1) {
                                    out.write(buf, 0, n);
                                }
                                out.close();
                            }
                            input.closeEntry();
                            entry = input.getNextEntry();
                        }
                    } catch (Exception e) {
                        Logger.logError(e.getMessage(), e);
                        ErrorUtils.tossError("Error extracting natives: " + e.getMessage());
                    } finally {
                        try {
                            input.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
            List<File> classpath = new ArrayList<File>();
            Version packjson = new Version();
            if (new File(gameDir, "pack.json").exists()) {
                packjson = JsonFactory.loadVersion(new File(gameDir, "pack.json"));
                for (Library lib : packjson.getLibraries()) {
                    classpath.add(new File(libDir, lib.getPath()));
                }
            }
            classpath.add(new File(installDir, "versions/{MC_VER}/{MC_VER}.jar".replace("{MC_VER}", pack.getMcVersion())));
            for (Library lib : base.getLibraries()) {
                classpath.add(new File(libDir, lib.getPath()));
            }

            Process minecraftProcess = LaunchMinecraft.launchMinecraft(Settings.getSettings().getJavaPath(), gameDir, assetDir, natDir, classpath, resp.getUsername(), resp.getSessionID(),
                    packjson.mainClass != null ? packjson.mainClass : base.mainClass, packjson.minecraftArguments != null ? packjson.minecraftArguments : base.minecraftArguments,
                    packjson.assets != null ? packjson.assets : base.getAssets(), Settings.getSettings().getRamMax(), pack.getMaxPermSize(), pack.getMcVersion());
            if (LaunchFrame.con != null)
                LaunchFrame.con.minecraftStarted();
            LaunchFrame.getInstance().MCRunning = true;
            StreamLogger.prepare(minecraftProcess.getInputStream(), new LogEntry().level(LogLevel.UNKNOWN));
            String[] ignore = {"Session ID is token"};
            StreamLogger.setIgnore(ignore);
            StreamLogger.doStart();
            String curVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
            TrackerUtils.sendPageView(ModPack.getSelectedPack().getName(), "Launched / " + ModPack.getSelectedPack().getName() + " / " + curVersion.replace('_', '.'));
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
            }
            try {
                minecraftProcess.exitValue();
            } catch (IllegalThreadStateException e) {
                LaunchFrame.getInstance().setVisible(false);
                LaunchFrame.getInstance().procMonitor = ProcessMonitor.create(minecraftProcess, new Runnable() {
                    @Override
                    public void run() {
                        if (!Settings.getSettings().getKeepLauncherOpen()) {
                            System.exit(0);
                        } else {
                            LaunchFrame launchFrame = LaunchFrame.getInstance();
                            launchFrame.setVisible(true);
                            launchFrame.enableObjects();
                            try {
                                Settings.getSettings().load(new FileInputStream(Settings.getSettings().getConfigFile()));
                                LaunchFrame.getInstance().tabbedPane.remove(2);
                                LaunchFrame.getInstance().optionsPane = new OptionsPane(Settings.getSettings());
                                LaunchFrame.getInstance().tabbedPane.add(LaunchFrame.getInstance().optionsPane, 2);
                                LaunchFrame.getInstance().tabbedPane.setIconAt(2, new ImageIcon(this.getClass().getResource("/image/tabs/options.png")));
                            } catch (Exception e1) {
                                Logger.logError("Failed to reload settings after launcher closed", e1);
                            }
                        }
                        LaunchFrame.getInstance().MCRunning = false;
                    }
                });
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }
    }

    protected static void installMods() throws IOException {
        String installpath = Settings.getSettings().getInstallPath();
        String temppath = OSUtils.getDynamicStorageLocation();

        @SuppressWarnings("static-access")
        ModPack pack = ModPack.getPack(LaunchFrame.getInstance().modPacksPane.getSelectedModIndex());

        String packDir = pack.getDir();

        Logger.logInfo("dirs mk'd");

        File source = new File(temppath, "ModPacks/" + packDir + "/.minecraft");
        if (!source.exists()) {
            source = new File(temppath, "ModPacks/" + packDir + "/minecraft");
        }

        final String debugTag = "installMods: ";
        Logger.logInfo(debugTag + "install path: " + installpath);
        Logger.logInfo(debugTag + "temp path: " + temppath);
        Logger.logInfo(debugTag + "source: " + source);
        Logger.logInfo(debugTag + "packDir: " + packDir);

        FileUtils.copyFolder(source, new File(installpath, packDir + "/minecraft/"));
        FileUtils.copyFolder(new File(temppath, "ModPacks/" + packDir + "/instMods/"), new File(installpath, packDir + "/instMods/"));
        FileUtils.copyFolder(new File(temppath, "ModPacks/" + packDir + "/libraries/"), new File(installpath, "/libraries/"), false);
    }

    private static boolean initializeMods() {
        final String debugTag = "initializeMods: ";
        Logger.logInfo(debugTag + "pack dir...");
        Logger.logInfo(ModPack.getSelectedPack().getDir());
        ModManager man = new ModManager(new JFrame(), true);
        man.setVisible(true);
        while (man == null) {
        }
        while (!man.worker.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        if (man.erroneous) {
            return false;
        }
        try {
            installMods();
            man.cleanUp();
        } catch (IOException e) {
            Logger.logInfo(debugTag + "Exception: " + e);
        }
        return true;
    }

}
