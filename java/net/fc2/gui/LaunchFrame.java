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

package net.fc2.gui;

import net.fc2.data.LauncherStyle;
import net.fc2.data.LoginResponse;
import net.fc2.data.ModPack;
import net.fc2.data.Settings;
import net.fc2.download.Locations;
import net.fc2.gui.dialogs.LauncherUpdateDialog;
import net.fc2.gui.dialogs.LoadingDialog;
import net.fc2.gui.graphics.BPanel;
import net.fc2.gui.panes.CabPane;
import net.fc2.gui.panes.ILauncherPane;
import net.fc2.gui.panes.ModpacksPane;
import net.fc2.gui.panes.OptionsPane;
import net.fc2.locale.I18N;
import net.fc2.log.*;
import net.fc2.tools.CabManager;
import net.fc2.tools.ProcessMonitor;
import net.fc2.tracking.AnalyticsConfigData;
import net.fc2.tracking.JGoogleAnalyticsTracker;
import net.fc2.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;
import net.fc2.updater.UpdateChecker;
import net.fc2.util.*;
import net.fc2.util.OSUtils.OS;
import net.fc2.workers.CabWorker;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("serial")
public class LaunchFrame extends JFrame {
    public static JPanel panel;
    public static JTextField username;
    public static JPasswordField password;
    public static JCheckBox savePassword;
    public static boolean bstart = false;
    public static String macSt = "test";
    public static int buildNumber = 130;
    public static boolean noConfig = false;
    public static boolean allowVersionChange = false;
    public static boolean doVersionBackup = false;
    public static boolean i18nLoaded = false;
    public static LauncherConsole con;
    public static Panes currentPane = Panes.MODPACK;
    public static AnalyticsConfigData AnalyticsConfigData = new AnalyticsConfigData("UA-50173332-2");
    public static JGoogleAnalyticsTracker tracker;
    public static LoadingDialog loader;

    private static LaunchFrame instance = null;
    public static String version = "1.3.0";
    static TrayMenu trayMenu;
    public final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    public final JButton launch = new JButton();
    private final JButton START = new JButton();
    public final JButton Exit = new JButton();
    public ModpacksPane modPacksPane;
    public OptionsPane optionsPane;
    public static boolean MCRunning = false;
    public LoginResponse RESPONSE;
    private JPanel footer = new JPanel();
    private JLabel footerFriends = new JLabel(new ImageIcon(this.getClass().getResource("/image/logo_friendscraft.png")));
    private JLabel desc = new JLabel(new ImageIcon(this.getClass().getResource("/image/desc.png")));
    private BPanel bg = new BPanel("/image/bg/1bg.jpg", 2);
    private JLabel hitext;
    private JLabel usernameLbl;
    private JLabel passwordLbl;
    private CabPane cabPane;
    public static ProcessMonitor procMonitor;

    public LaunchFrame() {
        setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
        setResizable(false);
        setTitle("FriendsCraft 2 Launcher v" + version);
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_fc2.png")));
        panel = new JPanel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        if (OSUtils.getCurrentOS() == OS.WINDOWS) {
            setBounds(100, 100, 850, 482);
        } else {
            setBounds(100, 100, 850, 480);
        }
        panel.setBounds(0, 0, 850, 480);
        panel.setLayout(null);

        bg.setBounds(0, 0, 842, 480);
        bg.setLayout(null);
        panel.add(bg);

        usernameLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_USERNAME"));
        usernameLbl.setBounds(120, 25, 30, 30);

        username = new JTextField(16);
        username.setBounds(150, 25, 130, 30);

        passwordLbl = new JLabel(I18N.getLocaleString("PROFILEADDER_PASSWORD"));
        passwordLbl.setBounds(100, 55, 60, 30);

        password = new JPasswordField(16);
        password.setBounds(150, 55, 130, 30);

        savePassword = new JCheckBox(I18N.getLocaleString("PROFILEADDER_SAVEPASSWORD"));
        savePassword.setContentAreaFilled(false);
        savePassword.setBounds(150, 85, 150, 30);
        savePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                CabWorker.writeUsername();
            }
        });

        CabWorker.readUsername();

        START.setText(I18N.getLocaleString("START_BUTTON"));
        START.setEnabled(true);
        START.setBounds(175, 150, 100, 30);
        START.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                checkLogIn();
            }
        });

        bg.add(usernameLbl);
        bg.add(username);
        bg.add(passwordLbl);
        bg.add(password);
        bg.add(savePassword);
        bg.add(START);
        setContentPane(panel);
    }

    public static void main(String[] args) {
        OSUtils.createStorageLocations();

        System.setProperty("java.net.preferIPv4Stack", "true");

        if (Settings.getSettings().getUseSystemProxy()) {
            System.setProperty("java.net.useSystemProxies", "true");
        }

        if (new File(Settings.getSettings().getInstallPath(), "FC2LauncherLog.txt").exists()) {
            new File(Settings.getSettings().getInstallPath(), "FC2LauncherLog.txt").delete();
        }

        if (new File(Settings.getSettings().getInstallPath(), "MinecraftLog.txt").exists()) {
            new File(Settings.getSettings().getInstallPath(), "MinecraftLog.txt").delete();
        }

        Logger.addListener(new StdOutLogger());
        System.setOut(new OutputOverride(System.out, LogLevel.INFO));
        System.setErr(new OutputOverride(System.err, LogLevel.ERROR));

        try {
            Logger.addListener(new LogWriter(new File(Settings.getSettings().getInstallPath(), Locations.launcherLogFile), LogSource.LAUNCHER));
            Logger.addListener(new LogWriter(new File(Settings.getSettings().getInstallPath(), Locations.minecraftLogFile), LogSource.EXTERNAL));
        } catch (IOException e1) {
            Logger.logError(e1.getMessage(), e1);
        }

        AnalyticsConfigData.setUserAgent("Java/" + System.getProperty("java.version") + " (" + System.getProperty("os.name") + "; " + System.getProperty("os.arch") + ")");
        tracker = new JGoogleAnalyticsTracker(AnalyticsConfigData, GoogleAnalyticsVersion.V_4_7_2);
        tracker.setEnabled(true);
        TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher Start v" + version);
        if (!new File(OSUtils.getDynamicStorageLocation(), "FC2_OS_Sent" + version + ".txt").exists()) {
            TrackerUtils.sendPageView("net/ftb/gui/LaunchFrame.java", "Launcher " + version + " OS " + OSUtils.getOSString());
            try {
                new File(OSUtils.getDynamicStorageLocation(), "FC2_OS_Sent" + version + ".txt").createNewFile();
            } catch (IOException e) {
                Logger.logError("Error creating os cache text file");
            }
        }

        LaunchFrameHelpers.printInfo();

        DownloadUtils thread = new DownloadUtils();
        thread.start();

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                StyleUtil.loadUiStyles();
                try {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                    } catch (Exception e1) {
                    }
                }
                loader = new LoadingDialog();
                loader.setBackground(new Color(0, 0, 0, 0));
                loader.setModal(false);
                loader.setVisible(true);

                I18N.setupLocale();
                i18nLoaded = true;

                if (noConfig) {
                    Settings.getSettings().setInstallPath(OSUtils.getDynamicStorageLocation() + "FriendsCraft2");
                    Settings.getSettings().save();
                }

                LoadingDialog.setProgress(120);

                File installDir = new File(Settings.getSettings().getInstallPath());
                if (!installDir.exists()) {
                    installDir.mkdirs();
                }

                LoadingDialog.setProgress(130);

                LoadingDialog.setProgress(140);

                if (Settings.getSettings().getConsoleActive()) {
                    con = new LauncherConsole();
                    con.setVisible(true);
                    Logger.addListener(con);
                    con.scrollToBottom();
                }

                LaunchFrameHelpers.googleAnalytics();

                LoadingDialog.setProgress(160);

                AppUtils.waitForLock(i18nLoaded);

                LaunchFrame frame = new LaunchFrame();
                instance = frame;

                // Set up System Tray
                if (SystemTray.isSupported()) {
                    setUpSystemTray();
                } else {
                    Logger.logWarn("System Tray not supported");
                }

                LoadingDialog.setProgress(170);

                Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        Logger.logError("Unhandled exception in " + t.toString(), e);
                    }
                });

                instance.setVisible(true);

                UpdateChecker updateChecker = new UpdateChecker(buildNumber) {
                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                LauncherUpdateDialog p = new LauncherUpdateDialog(this);
                                p.setVisible(true);
                            }
                        } catch (InterruptedException e) {
                            Logger.logError(String.valueOf(e));
                        } catch (ExecutionException e) {
                            Logger.logError(String.valueOf(e));
                        }
                    }
                };
                updateChecker.execute();
                LoadingDialog.setProgress(180);

                if (savePassword.isSelected()) {
                    instance.checkLogIn();
                }

                instance.toFront();
            }
        });
    }

    public static void checkDoneLoading() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (ModpacksPane.loaded) {
                    instance.toFront();
                    LoadingDialog.setProgress(190);
                    LoadingDialog.setProgress(200);
                    loader.setVisible(false);
                    instance.setVisible(true);
                    LaunchFrame.getInstance().getLaunch().setEnabled(true);
                }
            }
        });
    }

    private static void setUpSystemTray() {
        trayMenu = new TrayMenu();

        SystemTray tray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage(instance.getClass().getResource("/image/logo_fc2.png")));

        trayIcon.setPopupMenu(trayMenu);
        trayIcon.setToolTip("FriendsCraft2 Launcher");
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static String getUserName() {

        return username.getText();
    }

    public static LaunchFrame getInstance() {
        return LaunchFrame.instance;
    }

    public static String getVersion() {
        return LaunchFrame.version;
    }

    public static ProcessMonitor getProcMonitor() {
        return LaunchFrame.procMonitor;
    }

    public void StartFrame(final int tab) {

        desc.setBounds(635, 6, 195, 50);

        tabbedPane.setBounds(0, 0, 850, 380);
        panel.add(tabbedPane);
        panel.add(desc);
        setContentPane(panel);

        footerFriends.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footerFriends.setBounds(-5, 0, 330, 74);
        footerFriends.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                OSUtils.browse("http://www.friendscraft2.ru");
            }
        });


        cabPane = new CabPane();
        modPacksPane = new ModpacksPane();
        optionsPane = new OptionsPane(Settings.getSettings());

        getRootPane().setDefaultButton(launch);
        updateLocale();
        try {
            tabbedPane.add(cabPane, 0);
            tabbedPane.add(modPacksPane, 1);
            tabbedPane.add(optionsPane, 2);
            tabbedPane.setIconAt(0, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/news.png")));
            tabbedPane.setIconAt(1, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/modpacks.png")));
            tabbedPane.setIconAt(2, LauncherStyle.getCurrentStyle().filterHeaderIcon(this.getClass().getResource("/image/tabs/options.png")));
            tabbedPane.setSelectedIndex(tab);

        } catch (Exception e1) {
            Logger.logError("error changing colors", e1);
        }
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
                if (tabbedPane.getSelectedComponent() == cabPane && !CabManager.getGroupnum().equals("3")) {
                    tabbedPane.setSelectedIndex(1);
                    ErrorUtils.tossError("No access!!!");
                }

                if (tabbedPane.getSelectedComponent() instanceof ILauncherPane) {
                    ((ILauncherPane) tabbedPane.getSelectedComponent()).onVisible();
                    currentPane = Panes.values()[tabbedPane.getSelectedIndex()];
                    updateFooter();
                }
            }
        });

        footer.setBounds(0, 380, 850, 100);
        footer.setLayout(null);
        footer.setBackground(LauncherStyle.getCurrentStyle().footerColor);
        panel.add(footer);

        if (!LaunchFrame.bstart) {
            launch.setText(I18N.getLocaleString("LAUNCH_BUTTON"));
            //launch.setEnabled(true);
            launch.setBounds(711, 20, 100, 30);
            launch.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    if (CabManager.getGroupnum().equals("3") || CabManager.getGroupnum().equals("5")
                            || CabManager.getGroupnum().equals("6") || CabManager.getGroupnum().equals("7")) {
                        CabManager.getGameNum();
//                        int g  = CabManager.getGames() + 1;
//                        CabManager.setGameNum(g);
//                        LoginAndStart.doLaunch();
                    } else {
                        ErrorUtils.showClickableMessage(I18N.getLocaleString("ERROR_GROUP"), "http://friendscraft2.ru/go/shop/", I18N.getLocaleString("ERROR_GROUP_LINK") + "!");
                    }
                }
            });

            Exit.setText(I18N.getLocaleString("MAIN_EXIT"));
            Exit.setEnabled(true);
            Exit.setBounds(611, 20, 100, 30);
            Exit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    bstart = true;
                    tabbedPane.removeAll();
                    footer.removeAll();
                    bg.setVisible(true);
                }
            });
        }

        hitext = new JLabel();
        hitext.setText("<html><body><font size='4'>" + I18N.getLocaleString("HI_TEXT") + " " + "<strong><a style='color:#ff6c36'>" + username.getText() + "</a></strong>" + "</font></body><html>");
        hitext.setBounds(380, 10, 220, 50);

        footer.add(footerFriends);
        footer.add(Exit);
        footer.add(launch);
        footer.add(hitext);

        ModPack.addListener(instance.modPacksPane);
        ModPack.loadXml("servers.xml");
    }

    public void checkLogIn() {

        String str = "";

        CabWorker.writeUsername();
        try {
            str = CabWorker.checkNamePass(username.getText(), new String(password.getPassword()));
        } catch (Exception e) {
            Logger.logError(e.getMessage(), e);
        }

        String str2 = "true";

        if (str2.equalsIgnoreCase("true")) {
            StartFrame(1);
            bg.setVisible(false);
            saveSettings();
        } else {
            ErrorUtils.tossError(I18N.getLocaleString("BAD4"));
        }
    }

    public void saveSettings() {
        instance.optionsPane.saveSettingsInto(Settings.getSettings());
    }

    public void enableObjects() {
        tabbedPane.setEnabledAt(0, true);
        tabbedPane.setEnabledAt(1, true);
        tabbedPane.setEnabledAt(2, true);
        tabbedPane.getSelectedComponent().setEnabled(true);
        updateFooter();
        launch.setEnabled(true);
        Exit.setEnabled(true);
    }

    public void updateFooter() {
        switch (currentPane) {
            default:
                launch.setVisible(true);
                Exit.setVisible(true);
                break;
        }
    }

    public void updateLocale() {
        launch.setText(I18N.getLocaleString("LAUNCH_BUTTON"));
        Exit.setText(I18N.getLocaleString("MAIN_EXIT"));
        optionsPane.updateLocale();
    }

    public JTabbedPane getTabbedPane() {
        return this.tabbedPane;
    }

    public JButton getLaunch() {
        return this.launch;
    }

    public JButton getSTART() {
        return this.START;
    }

    public JButton getExit() {
        return this.Exit;
    }

    public ModpacksPane getModPacksPane() {
        return this.modPacksPane;
    }

    public OptionsPane getOptionsPane() {
        return this.optionsPane;
    }

    public LoginResponse getRESPONSE() {
        return this.RESPONSE;
    }

    public JPanel getFooter() {
        return this.footer;
    }

    public JLabel getFooterFriends() {
        return this.footerFriends;
    }

    public JLabel getDesc() {
        return this.desc;
    }

    public BPanel getBg() {
        return this.bg;
    }

    public JLabel getHitext() {
        return this.hitext;
    }

    public JLabel getUsernameLbl() {
        return this.usernameLbl;
    }

    public JLabel getPasswordLbl() {
        return this.passwordLbl;
    }

    public CabPane getCabPane() {
        return this.cabPane;
    }

    protected enum Panes {
        NEWS, OPTIONS, MODPACK
    }
}
