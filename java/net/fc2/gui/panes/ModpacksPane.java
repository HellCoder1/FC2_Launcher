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

import net.fc2.data.LauncherStyle;
import net.fc2.data.ModPack;
import net.fc2.data.Settings;
import net.fc2.data.events.ModPackListener;
import net.fc2.gui.LaunchFrame;
import net.fc2.locale.I18N;
import net.fc2.log.Logger;
import net.fc2.mclauncher.LoginAndStart;
import net.fc2.tools.CabManager;
import net.fc2.util.ErrorUtils;
import net.fc2.util.OSUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("serial")
public class ModpacksPane extends JPanel implements ILauncherPane, ModPackListener {
    public static ArrayList<JPanel> packPanels;
    //	private JLabel loadingImage;
    public static boolean loaded = false;
    private static JPanel packs;
    private static JScrollPane packsScroll;
    private static JButton maps, textures;
    private static int selectedPack = 0;
    private static boolean modPacksAdded = false;
    public static HashMap<Integer, ModPack> currentPacks = new HashMap<Integer, ModPack>();
    public final ModpacksPane instance = this;
    private static JEditorPane packInfo;
    private static JScrollPane infoScroll;

    public ModpacksPane() {
        super();
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(null);

        packPanels = new ArrayList<JPanel>();

        packs = new JPanel();
        packs.setLayout(null);
        packs.setOpaque(false);

        if (!LaunchFrame.bstart) {
            // stub for a real wait message
            final JPanel p = new JPanel();
            p.setBounds(0, 0, 420, 55);
            p.setLayout(null);

            JTextArea filler = new JTextArea(I18N.getLocaleString("MODS_WAIT_WHILE_LOADING"));
            filler.setBorder(null);
            filler.setEditable(false);
            filler.setForeground(LauncherStyle.getCurrentStyle().tabPaneForeground);
            filler.setBounds(58, 6, 378, 42);
            filler.setBackground(LauncherStyle.getCurrentStyle().tabPaneBackground);
            p.add(filler);
            packs.add(p);
        }
        packsScroll = new JScrollPane();
        packsScroll.setBounds(495, 0, 337, 315);
        packsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        packsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        packsScroll.setWheelScrollingEnabled(true);
        packsScroll.setOpaque(false);
        packsScroll.setViewportView(packs);
        packsScroll.getVerticalScrollBar().setUnitIncrement(19);
        add(packsScroll);

        packInfo = new JEditorPane();
        packInfo.setEditable(false);
        packInfo.setContentType("text/html");
        packInfo.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent event) {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    OSUtils.browse(event.getURL().toString());
                }
            }
        });
        packInfo.setBackground(UIManager.getColor("control").darker().darker());
        add(packInfo);

        infoScroll = new JScrollPane();
        infoScroll.setBounds(0, 25, 495, 290);
        infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScroll.setWheelScrollingEnabled(true);
        infoScroll.setViewportView(packInfo);
        infoScroll.setOpaque(false);
        add(infoScroll);

        maps = new JButton(I18N.getLocaleString("MAPS_BTN"));
        maps.setBounds(10, 2, 135, 25);
        maps.setToolTipText("BETA!");
        maps.addActionListener(new ActionListener() {
            @SuppressWarnings("unused")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (CabManager.getGroupnum().equals("3") || CabManager.getGroupnum().equals("5")
                        || CabManager.getGroupnum().equals("6") || CabManager.getGroupnum().equals("7")) {
                    BigMapsDialog aod = new BigMapsDialog();
                } else {
                    ErrorUtils.showClickableMessage(I18N.getLocaleString("ERROR_GROUP"), "http://friendscraft2.ru/go/shop/", I18N.getLocaleString("ERROR_GROUP_LINK") + "!");
                }
            }
        });
        add(maps);

        textures = new JButton(I18N.getLocaleString("TEXTURES_BTN"));
        textures.setBounds(155, 2, 135, 25);
        textures.setToolTipText("Soon...");
        add(textures);
    }

    /*
     * GUI Code to add a modpack to the selection
     */
    public static void addPack(ModPack pack) {
        if (!modPacksAdded) {
            modPacksAdded = true;
            packs.removeAll();
            packs.repaint();
        }
        final int packIndex = packPanels.size();
        final JPanel p = new JPanel();
        p.setBounds(0, (packIndex * 55), 420, 55);
        p.setLayout(null);
        JLabel logo = new JLabel(new ImageIcon(pack.getLogo()));
        logo.setBounds(6, 6, 42, 42);
        logo.setVisible(true);

        JTextArea filler = new JTextArea(pack.getName() + "\n" + pack.getMessage());
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(Color.white);
        filler.setBounds(58, 6, 362, 42);
        filler.setBackground(new Color(255, 255, 255, 0));
        MouseAdapter lin = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    LoginAndStart.doLaunch();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectedPack = packIndex;
                updatePacks();
            }
        };
        p.addMouseListener(lin);
        filler.addMouseListener(lin);
        logo.addMouseListener(lin);
        p.add(filler);
        p.add(logo);
        packPanels.add(p);
        packs.add(p);
        if (currentPacks.isEmpty()) {
            packs.setMinimumSize(new Dimension(420, (ModPack.getPackArray().size() * 55)));
            packs.setPreferredSize(new Dimension(420, (ModPack.getPackArray().size() * 55)));
        } else {
            packs.setMinimumSize(new Dimension(420, (currentPacks.size() * 55)));
            packs.setPreferredSize(new Dimension(420, (currentPacks.size() * 55)));
        }
        packsScroll.revalidate();
        if (pack.getDir().equalsIgnoreCase(Settings.getSettings().getLastPack())) {
            selectedPack = packIndex;
        }
    }

    public static void sortPacks() {
        packPanels.clear();
        packs.removeAll();
        currentPacks.clear();
        selectedPack = 0;
        packInfo.setText("");
        packs.repaint();
        modPacksAdded = false;
        updatePacks();
    }

    private static void updatePacks() {
        for (int i = 0; i < packPanels.size(); i++) {
            if (selectedPack == i) {
                ModPack pack = ModPack.getPack(getIndex());
                if (pack != null) {
                    packPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
                    packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    File tempDir = new File(OSUtils.getCacheStorageLocation(), "ModPacks" + File.separator + ModPack.getPack(getIndex()).getDir());
                    packInfo.setText("<html><img src='file:///" + tempDir.getPath() + File.separator + ModPack.getPack(getIndex()).getImageName() + "' width=472 height=224></img> <br>"
                            + ModPack.getPack(getIndex()).getInfo());
                    packInfo.setCaretPosition(0);
                }
            } else {
                packPanels.get(i).setBackground(UIManager.getColor("control"));
                packPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
    }

    public static int getSelectedModIndex() {

        return modPacksAdded ? getIndex() : -1;
    }

    private static int getIndex() {
        return (!currentPacks.isEmpty()) ? currentPacks.get(selectedPack).getIndex() : selectedPack;
    }

    @Override
    public void onVisible() {
    }

    @Override
    public void onModPackAdded(ModPack pack) {
        final ModPack pack_ = pack;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                addPack(pack_);
                Logger.logInfo("Adding pack " + packPanels.size() + " (" + pack_.getName() + ")");
                if (!currentPacks.isEmpty()) {
                    sortPacks();
                } else {
                    updatePacks();

                }
            }
        });
    }
}
