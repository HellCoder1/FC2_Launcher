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

import net.fc2.data.Settings;
import net.fc2.gui.panes.OptionsPane;
import net.fc2.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

@SuppressWarnings("serial")
public class ChooseDir extends JFrame implements ActionListener {
    private OptionsPane optionsPane;
//	private EditModPackDialog editMPD;

    private String choosertitle = "Please select an install location";

    public ChooseDir(OptionsPane optionsPane) {
        super();
        this.optionsPane = optionsPane;
//		editMPD = null;
    }

//	public ChooseDir(EditModPackDialog editMPD) {
//		super();
//		optionsPane = null;
//		this.editMPD = editMPD;
//	}


    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        if (optionsPane != null) {
            chooser.setCurrentDirectory(new File(Settings.getSettings().getInstallPath()));
            chooser.setDialogTitle(choosertitle);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Logger.logInfo("getCurrentDirectory(): " + chooser.getCurrentDirectory());
                Logger.logInfo("getSelectedFile() : " + chooser.getSelectedFile());
                optionsPane.setInstallFolderText(chooser.getSelectedFile().getPath());
            } else {
                Logger.logWarn("No Selection.");
            }
        }
//		} else if(editMPD != null) {
//			if(!Settings.getSettings().getLastAddPath().isEmpty()) {
//				chooser.setCurrentDirectory(new File(Settings.getSettings().getLastAddPath()));
//			}
//			chooser.setDialogTitle("Please select the mod to install");
//			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//			chooser.setAcceptAllFileFilterUsed(true);
//			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//				File destination = new File(editMPD.folder, chooser.getSelectedFile().getName());
//				if(!destination.exists()) {
//					try {
//						FileUtils.copyFile(chooser.getSelectedFile(), destination);
//						Settings.getSettings().setLastAddPath(chooser.getSelectedFile().getPath());
//						LaunchFrame.getInstance().saveSettings();
//					} catch (IOException e1) {
//						Logger.logError(e1.getMessage());
//					}
//					editMPD.updateLists();
//				} else {
//					ErrorUtils.tossError("File already exists, cannot add mod!");
//				}
//			} else {
//				Logger.logWarn("No Selection.");
//			}
//		} 
        else {
            chooser.setCurrentDirectory(new File(Settings.getSettings().getInstallPath()));
            chooser.setDialogTitle(choosertitle);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                Logger.logInfo("getCurrentDirectory(): " + chooser.getCurrentDirectory());
                Logger.logInfo("getSelectedFile() : " + chooser.getSelectedFile());
            } else {
                Logger.logWarn("No Selection.");
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }
}