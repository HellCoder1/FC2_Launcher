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

package net.fc2.gui.panes;

import net.fc2.data.Settings;
import net.fc2.download.Locations;
import net.fc2.gui.ChooseDir;
import net.fc2.gui.LaunchFrame;
import net.fc2.gui.dialogs.DeleteDialog;
import net.fc2.locale.I18N;
import net.fc2.log.Logger;
import net.fc2.util.FileUtils;
import net.fc2.util.OSUtils;
import net.fc2.util.OSUtils.OS;
import net.fc2.util.winreg.JavaFinder;
import net.fc2.util.winreg.JavaInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("serial")
public class OptionsPane extends JPanel implements ILauncherPane {
    private final Settings settings;
    private FocusListener settingsChangeListener = new FocusListener() {
        @Override
        public void focusLost(FocusEvent e) {
            LaunchFrame.con.setVisible(settings.getConsoleActive());
            saveSettingsInto(settings);
        }

        @Override
        public void focusGained(FocusEvent e) {
        }
    };
    private JButton installBrowseBtn /*, advancedOptionsBtn*/, btnInstallJava, btnSave, btndel = new JButton();
    private JLabel lblJavaVersion, lblvmType, lblArch, cprlbl, fclbl, lblInstallFolder, lblRamMaximum, /*lblLocale,*/
            currentRam, lbl32BitWarning = new JLabel();
    private JSlider ramMaximum;
    //	private JComboBox locale;
    private JTextField installFolderTextField;
    private JCheckBox chckbxShowConsole, keepLauncherOpen, optJavaArgs, useSystemProxy;
    private DeleteDialog deleteDialog;

    public OptionsPane(Settings settings) {
        this.settings = settings;
        setBorder(new EmptyBorder(5, 5, 5, 5));

        btnSave = new JButton(I18N.getLocaleString("OPTIONS_SAVE"));
        btnSave.setBounds(650, 250, 135, 25);
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                saveSettingsInto(OptionsPane.this.settings);
                Logger.logInfo(I18N.getLocaleString("OPTIONS_SAVE") + "!");
            }
        });
        add(btnSave);

        btndel = new JButton(I18N.getLocaleString("OPTIONS_DEL"));
        btndel.setBounds(650, 220, 135, 25);
        btndel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                deleteDialog = new DeleteDialog("OPTIONS_DEL_MESSAGE", "OPTIONS_DEL_CONFIRM", "OPTIONS_DEL_TITLE");
                deleteDialog.setVisible(true);
                deleteDialog.toFront();
                if (deleteDialog.ready && deleteDialog.ret) {
                    String installPath = Settings.getSettings().getInstallPath();
                    try {
                        FileUtils.delete(new File(installPath));
                    } catch (IOException e) {
                        Logger.logInfo("CAN'T DELETE!", e);
                    }
                    saveSettingsInto(OptionsPane.this.settings);
                    Logger.logInfo("ALL DONE!");
                }
                deleteDialog.setVisible(false);
            }
        });
        add(btndel);

        installBrowseBtn = new JButton("...");
        installBrowseBtn.setBounds(786, 11, 49, 28);
        installBrowseBtn.addActionListener(new ChooseDir(this));
        setLayout(null);
        add(installBrowseBtn);

        lblInstallFolder = new JLabel(I18N.getLocaleString("INSTALL_FOLDER"));
        lblInstallFolder.setBounds(10, 11, 127, 28);
        add(lblInstallFolder);

        installFolderTextField = new JTextField();
        installFolderTextField.setBounds(147, 11, 629, 28);
        installFolderTextField.addFocusListener(settingsChangeListener);
        installFolderTextField.setColumns(10);
        installFolderTextField.setText(settings.getInstallPath());
        add(installFolderTextField);

        currentRam = new JLabel();
        currentRam.setBounds(357, 55, 85, 25);
        long ram = OSUtils.getOSTotalMemory();
        long freeram = OSUtils.getOSFreeMemory();

        ramMaximum = new JSlider();
        ramMaximum.setBounds(150, 55, 180, 25);
        ramMaximum.setSnapToTicks(true);
        ramMaximum.setMajorTickSpacing(256);
        ramMaximum.setMinorTickSpacing(256);
        ramMaximum.setMinimum(256);
        String vmType = new String();
        if (OSUtils.getCurrentOS().equals(OS.WINDOWS) && JavaFinder.parseJavaVersion() != null) {
            vmType = JavaFinder.parseJavaVersion().is64bits ? "64" : "32";
        } else {
            vmType = System.getProperty("sun.arch.data.model");
        }
        if (vmType != null) {
            if (vmType.equals("64")) {
                ramMaximum.setMaximum((int) ram);
            } else if (vmType.equals("32")) {
                if (ram < 1024) {
                    ramMaximum.setMaximum((int) ram);
                } else {
                    if (freeram > 2046) {
                        ramMaximum.setMaximum(1536);
                    } else {
                        ramMaximum.setMaximum(1024);
                    }
                }
            }
        }
        int ramMax = (Integer.parseInt(settings.getRamMax()) > ramMaximum.getMaximum()) ? ramMaximum.getMaximum() : Integer.parseInt(settings.getRamMax());
        ramMaximum.setValue(ramMax);
        currentRam.setText(getAmount());
        ramMaximum.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                currentRam.setText(getAmount());
            }
        });
        ramMaximum.addFocusListener(settingsChangeListener);

        lblRamMaximum = new JLabel(I18N.getLocaleString("RAM_MAX"));
        lblRamMaximum.setBounds(10, 55, 195, 25);
        add(lblRamMaximum);
        add(ramMaximum);
        add(currentRam);

//		String[] locales;
//		synchronized (I18N.localeIndices) {
//			locales = new String[I18N.localeIndices.size()];
//			for(Map.Entry<Integer, String> entry : I18N.localeIndices.entrySet()) {
//				Logger.logInfo("[i18n] Added " + entry.getKey().toString() + " " + entry.getValue() + " to options pane");
//				locales[entry.getKey()] = I18N.localeFiles.get(entry.getValue());
//			}
//		}
//		locale = new JComboBox(locales);
//		locale.setBounds(100, 130, 222, 25);
//		locale.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				I18N.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
//				if(LaunchFrame.getInstance() != null) {
//					LaunchFrame.getInstance().updateLocale();
//				}
//			}
//		});
//		locale.addFocusListener(settingsChangeListener);
//		locale.setSelectedItem(I18N.localeFiles.get(settings.getLocale()));
//
//		lblLocale = new JLabel(I18N.getLocaleString("LANGUAGE"));
//		lblLocale.setBounds(10, 130, 195, 25);
//		add(lblLocale);
//		add(locale);

        updateJavaLabels();

        chckbxShowConsole = new JCheckBox(I18N.getLocaleString("SHOW_CONSOLE"));
        chckbxShowConsole.addFocusListener(settingsChangeListener);
        chckbxShowConsole.setSelected(settings.getConsoleActive());
        chckbxShowConsole.setBounds(490, 55, 183, 25);
        add(chckbxShowConsole);

        keepLauncherOpen = new JCheckBox(I18N.getLocaleString("REOPEN_LAUNCHER"));
        keepLauncherOpen.setBounds(490, 130, 300, 25);
        keepLauncherOpen.setSelected(settings.getKeepLauncherOpen());
        keepLauncherOpen.addFocusListener(settingsChangeListener);
        add(keepLauncherOpen);

        optJavaArgs = new JCheckBox(I18N.getLocaleString("OPT_JAVA_ARGS"));
        optJavaArgs.setBounds(490, 75, 300, 25);
        optJavaArgs.setSelected(settings.getOptJavaArgs());
        optJavaArgs.addFocusListener(settingsChangeListener);
        add(optJavaArgs);


        useSystemProxy = new JCheckBox(I18N.getLocaleString("USE_SYSTEM_PROXY"));
        useSystemProxy.setBounds(490, 95, 300, 25);
        useSystemProxy.setSelected(settings.getUseSystemProxy());
        useSystemProxy.addFocusListener(settingsChangeListener);
        add(useSystemProxy);

//		advancedOptionsBtn = new JButton(I18N.getLocaleString("ADVANCED_OPTIONS"));
//		advancedOptionsBtn.setBounds(147, 275, 429, 29);
//		advancedOptionsBtn.addActionListener(new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				AdvancedOptionsDialog aod = new AdvancedOptionsDialog();
//				aod.setVisible(true);
//			}
//		});
//		advancedOptionsBtn.getModel().setPressed(settings.getForceUpdate());
//		add(advancedOptionsBtn);

        if (OSUtils.getCurrentOS().equals(OS.WINDOWS) && JavaFinder.parseJavaVersion() != null && JavaFinder.parseJavaVersion().path != null) {
            lblJavaVersion = new JLabel("Java version: " + JavaFinder.parseJavaVersion().origVersion);
            lblJavaVersion.setBounds(15, 270, 250, 25);

            lblvmType = new JLabel("VM Type: " + vmType);
            lblvmType.setBounds(15, 283, 250, 25);

            add(lblJavaVersion);
            add(lblvmType);
        }
        lblArch = new JLabel("OS Arch: " + System.getProperty("os.arch"));
        lblArch.setBounds(15, 296, 250, 25);
        add(lblArch);

        fclbl = new JLabel("Copyright � 2013-2014, FriendsCraft2, HellCoder");
        fclbl.setBounds(540, 283, 350, 25);
        add(fclbl);

        cprlbl = new JLabel("Copyright � 2012-2014, FTB Launcher Contributors");
        cprlbl.setBounds(540, 296, 350, 25);
        add(cprlbl);
    }

    public void setInstallFolderText(String text) {
        installFolderTextField.setText(text);
        saveSettingsInto(settings);
    }

    public void saveSettingsInto(Settings settings) {
        settings.setInstallPath(installFolderTextField.getText());
        settings.setRamMax(String.valueOf(ramMaximum.getValue()));
//		settings.setLocale(I18N.localeIndices.get(locale.getSelectedIndex()));
        settings.setConsoleActive(chckbxShowConsole.isSelected());
        settings.setOptJavaArgs(optJavaArgs.isSelected());
        settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
        settings.setUseSystemProxy(useSystemProxy.isSelected());
//		settings.setMusicActive(music.isSelected());
        settings.save();
    }

    public void updateLocale() {
        lblInstallFolder.setText(I18N.getLocaleString("INSTALL_FOLDER"));
        lblRamMaximum.setText(I18N.getLocaleString("RAM_MAX"));
//		lblLocale.setText(I18N.getLocaleString("RUSSIAN"));
    }

    private String getAmount() {
        int ramMax = ramMaximum.getValue();
        return (ramMax >= 1024) ? Math.round((ramMax / 256) / 4) + "." + (((ramMax / 256) % 4) * 25) + " GB" : ramMax + " MB";
    }

    @Override
    public void onVisible() {
    }

    public void addUpdateJREButton(final String webLink, String unlocMessage) {
        btnInstallJava = new JButton(I18N.getLocaleString(unlocMessage));
        btnInstallJava.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                OSUtils.browse(webLink);
            }
        });
        btnInstallJava.setBounds(345, 210, 150, 28);
        add(btnInstallJava);
    }

    public void addUpdateLabel(final String unlocMessage) {
        lbl32BitWarning.setText(I18N.getLocaleString(unlocMessage));
        lbl32BitWarning.setBounds(60, 170, 700, 25);
        lbl32BitWarning.setForeground(Color.red);
        add(lbl32BitWarning);

    }

    public void updateJavaLabels() {
        remove(lbl32BitWarning);
//		         remove(btnInstallJava);
        // Dependant on vmType from earlier RAM calculations to detect 64 bit JVM
        JavaInfo java = Settings.getSettings().getCurrentJava();
        if (java.getMajor() < 1 || (java.getMajor() == 1 && java.getMinor() < 7)) {
            if (OSUtils.getCurrentOS().equals(OS.MACOSX)) {
                if (JavaFinder.java8Found) {//they need the jdk link
                    addUpdateJREButton(Locations.jdkMac, "DOWNLOAD_JAVAGOOD");
                    addUpdateLabel("JAVA_NEW_Warning");
                } else if (OSUtils.canRun7OnMac()) {
                    addUpdateJREButton(Locations.jreMac, "DOWNLOAD_JAVAGOOD");
                    addUpdateLabel("JAVA_OLD_Warning");
                } else {
                }
            } else if (OSUtils.is64BitOS()) {
                if (OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
                    addUpdateJREButton(Locations.java64Win, "DOWNLOAD_JAVA64");
                    addUpdateLabel("JAVA_OLD_Warning");
                } else if (OSUtils.getCurrentOS().equals(OS.UNIX)) {
                    addUpdateJREButton(Locations.java64Lin, "DOWNLOAD_JAVA64");
                    addUpdateLabel("JAVA_OLD_Warning");
                }
            } else {
                if (OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
                    addUpdateJREButton(Locations.java32Win, "DOWNLOAD_JAVA32");
                    addUpdateLabel("JAVA_OLD_Warning");
                } else if (OSUtils.getCurrentOS().equals(OS.UNIX)) {
                    addUpdateJREButton(Locations.java32Lin, "DOWNLOAD_JAVA32");
                    addUpdateLabel("JAVA_OLD_Warning");
                }
            }
        } else if (OSUtils.getCurrentOS().equals(OS.MACOSX) && (java.getMajor() > 1 || (java.getMajor() == 1 || java.getMinor() > 7))) {
            addUpdateJREButton(Locations.jdkMac, "DOWNLOAD_JAVAGOOD");//they need the jdk link
            addUpdateLabel("JAVA_NEW_Warning");
        } else if (!Settings.getSettings().getCurrentJava().is64bits) {//needs to use proper bit's
            addUpdateLabel("JAVA_32BIT_WARNING");
            if (OSUtils.getCurrentOS().equals(OS.WINDOWS)) {
                if (OSUtils.is64BitWindows()) {
                    addUpdateJREButton(Locations.java64Win, "DOWNLOAD_JAVA64");
                }
            }
        }
        repaint();
    }

    public void updateShowConsole() {
        chckbxShowConsole.setSelected(settings.getConsoleActive());
    }
}
