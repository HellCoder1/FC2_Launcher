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
import net.fc2.data.Map;
import net.fc2.data.ModPack;
import net.fc2.data.events.MapListener;
import net.fc2.gui.LaunchFrame;
import net.fc2.locale.I18N;
import net.fc2.log.Logger;
import net.fc2.tools.MapManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class BigMapsDialog extends JDialog implements MapListener {
    public static ArrayList<JPanel> mapPanels;
    public static String type = "Client", origin = "All";
    public static boolean loaded = false;
    static JPanel maps;
    static HashMap<Integer, Map> currentMaps = new HashMap<Integer, Map>();
    private static JScrollPane mapsScroll;
    private static JComboBox<String> mapInstallLocation;
    private static int selectedMap = 0;
    private static boolean mapsAdded = false;
    private final BigMapsDialog instance = this;
    private final JButton mapInstall = new JButton();
    private JPanel footer = new JPanel();

    public BigMapsDialog() {
        super(LaunchFrame.getInstance(), true);
        setupGui();
        sortMaps();
        Map.loadAll();
        Map.addListener(instance);
        setVisible(true);
    }

    /*
     * GUI Code to add a map to the selection
     */
    public static void addMap(Map map) {
        if (!mapsAdded) {
            mapsAdded = true;
            maps.removeAll();
        }

        final int mapIndex = mapPanels.size();
        final JPanel p = new JPanel();
        p.setBounds(0, (mapIndex * 55), 380, 55);
        p.setLayout(null);
        JLabel logo = new JLabel(new ImageIcon(map.getLogo()));
        logo.setBounds(6, 6, 42, 42);
        logo.setVisible(true);

        JTextArea filler = new JTextArea(map.getName() + " (v." + map.getVersion() + ")\n" + "By " + map.getAuthor());
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(Color.white);
        filler.setBounds(58, 6, 380, 42);
        filler.setBackground(new Color(255, 255, 255, 0));

        JTextArea desc = new JTextArea(I18N.getLocaleString("DESCRIPTION") + ":" + "\n" + map.getInfo());
        desc.setBorder(null);
        desc.setEditable(false);
        desc.setForeground(Color.white);
        desc.setBounds(0, 55, 380, 42);
        desc.setBackground(new Color(255, 255, 255, 0));

        MouseAdapter lin = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedMap = mapIndex;
//                runThread.start();
                updateMaps();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectedMap = mapIndex;
//                runThread.start();
                updateMaps();
            }
        };
        p.addMouseListener(lin);
        filler.addMouseListener(lin);
        logo.addMouseListener(lin);
        p.add(filler);
        p.add(logo);
        p.add(desc);
        mapPanels.add(p);
        maps.add(p);
        if (origin.equalsIgnoreCase("all")) {
            maps.setMinimumSize(new Dimension(380, (Map.getMapArray().size() * 55)));
            maps.setPreferredSize(new Dimension(380, (Map.getMapArray().size() * 55)));
        } else {
            maps.setMinimumSize(new Dimension(380, (currentMaps.size() * 55)));
            maps.setPreferredSize(new Dimension(380, (currentMaps.size() * 55)));
        }
        mapsScroll.revalidate();

    }

    public static void sortMaps() {
        mapPanels.clear();
        maps.removeAll();
        currentMaps.clear();
        int counter = 0;
        selectedMap = 0;
        maps.repaint();
        updateMapInstallLocs(new String[]{""});
//        mapInfo.setText("");
        HashMap<Integer, List<Map>> sorted = new HashMap<Integer, List<Map>>();
        sorted.put(0, new ArrayList<Map>());
        sorted.put(1, new ArrayList<Map>());
        for (Map map : sorted.get(1)) {
            addMap(map);
            currentMaps.put(counter, map);
            counter++;
        }
        for (Map map : sorted.get(0)) {
            addMap(map);
            currentMaps.put(counter, map);
            counter++;
        }
        updateMaps();
    }

    private static void updateMaps() {

        for (int i = 0; i < mapPanels.size(); i++) {
            mapPanels.get(i).setBounds(0, i * 55, 380, 55);
            if (selectedMap == i) {
                mapPanels.get(i).setBackground(UIManager.getColor("control").darker().darker());
                mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                updateMapInstallLocs(Map.getMap(getIndex()).getCompatible());
                mapPanels.get(i).setBounds(0, i * 55, 380, 100);
            } else {
                mapPanels.get(i).setBackground(UIManager.getColor("control"));
                mapPanels.get(i).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                if (!(i < selectedMap)) {
                    for (int s = i; s < mapPanels.size(); s++) {
                        mapPanels.get(s).setBounds(0, (s + 1) * 55, 380, 55);
                    }
                }
            }
        }
    }

    public static int getSelectedMapIndex() {
        return mapsAdded ? getIndex() : -1;
    }

    private static int getIndex() {
        return (currentMaps.size() > 0) ? currentMaps.get(selectedMap).getIndex() : selectedMap;
    }

    public static void updateMapInstallLocs(String[] locations) {
        mapInstallLocation.removeAllItems();
        for (String location : locations) {
            if (!location.isEmpty()) {
                mapInstallLocation.addItem(ModPack.getPack(location.trim()).getName());
            }
        }
    }

    public static int getSelectedMapInstallIndex() {
        return mapInstallLocation.getSelectedIndex();
    }

    private static int getMapNum() {
        if (currentMaps.size() > 0) {
            if (!origin.equalsIgnoreCase(I18N.getLocaleString("MAIN_ALL"))) {
                return currentMaps.get((mapPanels.size() - 1)).getIndex();
            }
        }
        return mapPanels.size();
    }

    private void setupGui() {
        setFont(new Font("a_FuturaOrto", Font.PLAIN, 12));
        setResizable(false);
        setTitle(I18N.getLocaleString("MAPS_BTN") + " beta");
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_fc2.png")));
        setBounds(100, 100, 400, 450);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        mapPanels = new ArrayList<JPanel>();
        maps = new JPanel();
        maps.setLayout(null);
        maps.setOpaque(false);
        maps.setBounds(0, 0, 400, 330);

        final JPanel p = new JPanel();
        p.setBounds(0, 0, 400, 55);
        p.setLayout(null);

        JTextArea filler = new JTextArea(I18N.getLocaleString("MAPS_WAIT_WHILE_LOADING"));
        filler.setBorder(null);
        filler.setEditable(false);
        filler.setForeground(Color.white);
        filler.setBounds(58, 6, 378, 42);
        filler.setBackground(LauncherStyle.getCurrentStyle().footerColor);
        p.add(filler);


        footer.setBounds(0, 380, 400, 40);
        footer.setLayout(null);
        footer.setBackground(LauncherStyle.getCurrentStyle().footerColor);
        add(footer);

        mapsScroll = new JScrollPane();
        mapsScroll.setBounds(0, 0, 400, 360);
        mapsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mapsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mapsScroll.setWheelScrollingEnabled(true);
        mapsScroll.setOpaque(false);
        mapsScroll.setViewportView(maps);

        mapInstallLocation = new JComboBox<String>();
        mapInstallLocation.setBounds(5, 5, 220, 30);
        mapInstallLocation.setToolTipText("�������� ������");

        mapInstall.setBounds(235, 5, 160, 30);
        mapInstall.setText(I18N.getLocaleString("INSTALL_MAP"));
        mapInstall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (mapPanels.size() > 0 && getSelectedMapIndex() >= 0) {
                    MapManager man = new MapManager(new JFrame(), true);
                    man.setVisible(true);
                    MapManager.cleanUp();
                }
            }
        });

        add(mapsScroll);
        maps.add(p);
        footer.add(mapInstall);
        footer.add(mapInstallLocation);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.out.println("Clear maps");
            setVisible(false);
        }
    }

    @Override
    public void onMapAdded(Map map) {
        final Map map_ = map;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                addMap(map_);
                Logger.logInfo("Adding map " + getMapNum() + " (" + map_.getName() + ")");
                updateMaps();
            }
        });
    }

    public BigMapsDialog getInstance() {
        return this.instance;
    }
}

