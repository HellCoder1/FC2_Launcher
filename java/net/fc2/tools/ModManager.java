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

import net.fc2.data.ModPack;
import net.fc2.data.Settings;
import net.fc2.gui.LaunchFrame;
import net.fc2.gui.dialogs.ModpackUpdateDialog;
import net.fc2.log.Logger;
import net.fc2.util.DownloadUtils;
import net.fc2.util.FileUtils;
import net.fc2.util.OSUtils;
import net.fc2.util.TrackerUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("serial")
public class ModManager extends JDialog {
    public static boolean update = false, backup = false, erroneous = false, upToDate = false;
    public static ModManagerWorker worker;
    private static String curVersion = "";
    private static String sep = File.separator;
    private static File baseDynamic;
    private final JProgressBar progressBar;
    private final JLabel label;
    private JPanel contentPane;
    private double downloadedPerc;

    /**
     * Create the frame.
     */
    public ModManager(JFrame owner, Boolean model) {
        super(owner, model);
        setResizable(false);
        setTitle("Downloading...");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(100, 100, 313, 138);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        progressBar = new JProgressBar();
        progressBar.setBounds(10, 63, 278, 22);
        contentPane.add(progressBar);

        JLabel lblDownloadingModPack = new JLabel("<html><body><center>Downloading mod pack...<br/>Please Wait</center></body></html>");
        lblDownloadingModPack.setHorizontalAlignment(SwingConstants.CENTER);
        lblDownloadingModPack.setBounds(0, 5, 313, 30);
        contentPane.add(lblDownloadingModPack);
        label = new JLabel("");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBounds(0, 42, 313, 14);
        contentPane.add(label);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent arg0) {
                worker = new ModManagerWorker() {
                    @Override
                    protected void done() {
                        setVisible(false);
                        super.done();
                    }
                };
                worker.execute();
            }
        });
    }

    public static void cleanUp() {
        ModPack pack = ModPack.getSelectedPack();
        File tempFolder = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + sep + pack.getDir() + sep);
        for (String file : tempFolder.list()) {
            if (!file.equals(pack.getLogoName()) && !file.equals(pack.getImageName()) && !file.equals("version")) {
                try {
                    if (file.endsWith(".zip")) {
                        Logger.logInfo("retaining modpack file: " + tempFolder + File.separator + file);
                    } else {
                        FileUtils.delete(new File(tempFolder, file));
                    }
                } catch (IOException e) {
                    Logger.logError(e.getMessage(), e);
                }
            }
        }
    }

    public static void clearModsFolder(ModPack pack) {
        File modsFolder = new File(Settings.getSettings().getInstallPath(), pack.getDir() + File.separator + "minecraft" + File.separator + "mods");
        clearFolder(modsFolder);
        Logger.logInfo("Mods Folder: " + modsFolder.toString());
        File dyn = new File(baseDynamic.getPath(), "minecraft" + File.separator + "mods");
        Logger.logInfo("Dynamic Folder: " + dyn);
        clearFolder(dyn);
    }

    public static void clearFolder(File folder) {
        if (folder.exists()) {
            for (String file : folder.list()) {
                if (new File(folder, file).isDirectory()) {
                    //Logger.logInfo(new File(folder, file).toString());
                    clearFolder(new File(folder, file));
                }
                if (file.toLowerCase().endsWith(".zip") || file.toLowerCase().endsWith(".jar") || file.toLowerCase().endsWith(".disabled") || file.toLowerCase().endsWith(".litemod")) {
                    try {
                        boolean b = FileUtils.delete(new File(folder, file));
                        if (!b) Logger.logInfo("Error deleting " + file);
                    } catch (IOException e) {
                        e.printStackTrace();


                    }
                }
            }
        }
    }

    private boolean upToDate() throws IOException {
        ModPack pack = ModPack.getSelectedPack();
        File version = new File(Settings.getSettings().getInstallPath(), pack.getDir() + sep + "version");
        if (!version.exists()) {
            version.getParentFile().mkdirs();
            version.createNewFile();
            curVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
            return false;
        }
        BufferedReader in = new BufferedReader(new FileReader(version));
        String line = in.readLine();
        in.close();
        int currentVersion, requestedVersion;
        currentVersion = (line != null) ? Integer.parseInt(line.replace(".", "")) : 0;
        if (!Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") && !Settings.getSettings().getPackVer().equalsIgnoreCase("newest version")) {
            requestedVersion = Integer.parseInt(Settings.getSettings().getPackVer().trim().replace(".", ""));
            if (requestedVersion != currentVersion) {
                Logger.logInfo("Modpack is out of date.");
                curVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? pack.getVersion() : Settings.getSettings().getPackVer()).replace(".", "_");
                return false;
            } else {
                Logger.logInfo("Modpack is up to date.");
                return true;
            }
        } else if (Integer.parseInt(pack.getVersion().replace(".", "")) > currentVersion) {
            Logger.logInfo("Modpack is out of date.");
            if (LaunchFrame.allowVersionChange) {
                curVersion = (Settings.getSettings().getPackVer().equalsIgnoreCase("recommended version") ? pack.getVersion().replace(".", "_") : Settings.getSettings().getPackVer()).replace(".", "_");
                return false;
            }
            ModpackUpdateDialog p = new ModpackUpdateDialog(LaunchFrame.getInstance(), true);
            p.setVisible(true);
            if (!update) {
                return true;
            }
            if (backup) {
                File destination = new File(OSUtils.getDynamicStorageLocation(), "backups" + sep + pack.getDir() + sep + "config_backup");
                if (destination.exists()) {
                    FileUtils.delete(destination);
                }
                FileUtils.copyFolder(new File(Settings.getSettings().getInstallPath(), pack.getDir() + sep + "minecraft" + sep + "config"), destination);
            }
            curVersion = pack.getVersion().replace(".", "_");
            return false;
        } else {
            Logger.logInfo("Modpack is up to date.");
            return true;
        }
    }

    public class ModManagerWorker extends SwingWorker<Boolean, Void> {
        @Override
        protected Boolean doInBackground() {
            try {
                if (!upToDate()) {
                    String installPath = OSUtils.getDynamicStorageLocation();
                    ModPack pack = ModPack.getSelectedPack();
                    pack.setUpdated(true);
                    File modPackZip = new File(installPath, "ModPacks" + sep + pack.getDir() + sep + pack.getUrl());
                    if (modPackZip.exists()) {
                        FileUtils.delete(modPackZip);
                    }
                    File animationGif = new File(OSUtils.getDynamicStorageLocation(), "ModPacks" + sep + pack.getDir() + sep);
                    if (animationGif.exists()) {
                        FileUtils.delete(animationGif);
                    }
                    String dynamicLoc = OSUtils.getDynamicStorageLocation();
                    baseDynamic = new File(dynamicLoc, "ModPacks" + sep + pack.getDir() + sep);
                    //clearModsFolder(pack);
                    erroneous = !downloadModPack(pack.getUrl(), pack.getDir());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        public String downloadUrl(String filename, String urlString) {
            BufferedInputStream in = null;
            FileOutputStream fout = null;
            HttpURLConnection connection = null;
            String md5 = "";
            int amount = 0, startAmount = -1, modPackSize = 0, count = 0, steps = 0;
            int retryCount = 5;

            try {
                fout = new FileOutputStream(filename);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logError("Failed opening output file: " + filename);
                return null;
            }

            do {
                try {
                    startAmount = amount;

                    if (amount > 0) {
                        Logger.logInfo("Resuming download from offset " + Integer.toString(amount));
                    }

                    URL url_ = new URL(urlString);
                    byte data[] = new byte[1024];
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            progressBar.setMaximum(10000);
                        }
                    });

                    connection = (HttpURLConnection) url_.openConnection();
                    connection.setAllowUserInteraction(true);
                    connection.setConnectTimeout(14000);
                    connection.setReadTimeout(20000);
                    if (amount > 0) {
                        connection.setRequestProperty("Range", "bytes=" + amount + "-");
                    }
                    connection.connect();
                    md5 = connection.getHeaderField("Content-MD5");
                    in = new BufferedInputStream(connection.getInputStream());
                    if (modPackSize == 0) {
                        modPackSize = connection.getContentLength();
                    } else {
                        if (amount + connection.getContentLength() != modPackSize) {
                            throw new IOException("Resume failed");
                        } else {
                            Logger.logInfo("Resume started sucessfully");
                        }
                    }

                    while ((count = in.read(data, 0, 1024)) != -1) {
                        fout.write(data, 0, count);

                        if (count > 0)
                            retryCount = 5;

                        downloadedPerc += (count * 1.0 / modPackSize) * 100;
                        amount += count;
                        steps++;
                        if (steps > 100) {
                            steps = 0;
                            final String txt = (amount / 1024) + "Kb / " + (modPackSize / 1024) + "Kb";
                            final int perc = (int) downloadedPerc * 100;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    progressBar.setValue(perc);
                                    label.setText(txt);
                                }
                            });
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (in != null) {
                        in.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (amount < modPackSize && (amount > startAmount || retryCount-- > 0));

            try {
                if (fout != null) {
                    fout.flush();
                    fout.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return md5;
        }

        protected boolean downloadModPack(String modPackName, String dir) {
            String debugTag = "downloadModPack: ";

            Logger.logInfo("Downloading Mod Pack");
            TrackerUtils.sendPageView("net/fc2/tools/ModManager.java", "Downloaded: " + modPackName + " v." + curVersion.replace('_', '.'));
            String dynamicLoc = OSUtils.getDynamicStorageLocation();
            String installPath = Settings.getSettings().getInstallPath();
            ModPack pack = ModPack.getSelectedPack();
            //clearModsFolder(pack);
            String baseLink = ("modpacks/" + dir + "/" + curVersion + "/");
            baseDynamic = new File(dynamicLoc, "ModPacks" + sep + dir + sep);
            Logger.logInfo(debugTag + "pack dir: " + dir);
            Logger.logInfo(debugTag + "dynamicLoc: " + dynamicLoc);
            Logger.logInfo(debugTag + "installPath: " + installPath);
            baseDynamic.mkdirs();

            @SuppressWarnings("unused")
            String md5 = "";
            try {
                File packFile = new File(baseDynamic, modPackName);
                if (!packFile.exists() /*|| !DownloadUtils.backupIsValid(packFile, baseLink + modPackName)*/) {
                    try {
                        new File(baseDynamic, modPackName).createNewFile();
                        md5 = downloadUrl(baseDynamic.getPath() + sep + modPackName, DownloadUtils.getFriendsCraftLink(baseLink + modPackName));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Logger.logError("Error validating pack archive");
            }

            try {
                Logger.logInfo(debugTag + "Extracting pack.");

                Logger.logInfo(debugTag + "Purging mods, coremods, instMods");

                clearModsFolder(pack);
                FileUtils.delete(new File(installPath, dir + "/minecraft/coremods"));
                FileUtils.delete(new File(installPath, dir + "/instMods/"));
                Logger.logInfo(debugTag + "Extracting pack.");
                FileUtils.extractZipTo(baseDynamic.getPath() + sep + modPackName, baseDynamic.getPath());
                File version = new File(installPath, dir + sep + "version");
                BufferedWriter out = new BufferedWriter(new FileWriter(version));
                out.write(curVersion.replace("_", "."));
                out.flush();
                out.close();
                Logger.logInfo(debugTag + "Pack extracted, version tagged.");
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
