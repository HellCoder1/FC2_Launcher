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

package net.fc2.gui.dialogs;

import net.fc2.gui.LaunchFrame;
import net.fc2.locale.I18N;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class LoadingDialog extends JDialog {
    public static JDialog instance;
    private static JProgressBar progress;
    public Color c = new Color(1f, 0f, 0f, .5f);
    private JLabel loadStatusLbl;
    private JLabel splashLbl;

    public LoadingDialog() {
        super(LaunchFrame.getInstance(), true);
        instance = this;

        setupGui();
    }

    public static int getProgress() {
//    	String c = String.valueOf(progress.getValue());
        return progress.getValue();
    }

    public static void setProgress(int new_progress) {
        if (progress != null && progress.getValue() < new_progress) {
            progress.setValue(new_progress);

            instance.repaint();
        }
    }

    public void releaseModal() {
        // Release modal from the loading screen, so the main thread can continue
        this.setVisible(false);
        this.setModal(false);
        this.setVisible(true);
        this.toFront();
        this.repaint();
    }

    private void setupGui() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_fc2.png")));
        setTitle(I18N.getLocaleString("FriendsCraft2 Launcher"));
        setSize(600, 150);
        setLayout(null);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container panel = getContentPane();

        splashLbl = new JLabel(new ImageIcon(this.getClass().getResource("/image/FC2_load.png")));
        splashLbl.setBounds(0, 0, 600, 100);

        loadStatusLbl = new JLabel("Loading...");
        loadStatusLbl.setHorizontalAlignment(SwingConstants.CENTER);
        loadStatusLbl.setBounds(150, 100, 300, 20);

        progress = new JProgressBar(0, 200);
        progress.setBounds(5, 130, 590, 20);

        panel.add(splashLbl);
        panel.add(loadStatusLbl);
        panel.add(progress);
    }

}
