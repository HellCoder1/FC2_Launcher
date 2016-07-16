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

import net.fc2.locale.I18N;
import net.fc2.log.Logger;
import net.fc2.tools.CabManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;


@SuppressWarnings("serial")
public class CabPane extends JPanel implements ILauncherPane {

    private static JEditorPane skin;
    private JLabel userbalanse, usergroup;
   // Color backColor = new Color(0, 0, 0);

    public CabPane() {
        super();

        Logger.logInfo("CabManager initialization");
        CabManager.init();

        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(null);

        skin = new JEditorPane();
        skin.setEditable(false);
        skin.setContentType("text/html");
        skin.setText("<html><img src='http://friendscraft2.ru/skin.php?user_name=" + CabManager.getName() + "' height='224' width='224'></img></html>");
        skin.setBounds(10, 10, 224, 224);
        skin.setFocusable(true);
        add(skin);

        userbalanse = new JLabel(I18N.getLocaleString("BALANCE") + ": " + CabManager.getBalanse());
        userbalanse.setBounds(300, 5, 100, 50);
        add(userbalanse);

        usergroup = new JLabel(I18N.getLocaleString("GROUP") + ": " + CabManager.getGroup());
        usergroup.setBounds(300, 25, 200, 50);
        add(usergroup);
    }

    @Override
    public void onVisible() {

    }
}
