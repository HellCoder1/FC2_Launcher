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

import net.fc2.locale.I18N;
import net.fc2.log.*;
import net.fc2.util.GameUtils;
import net.fc2.util.OSUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class LauncherConsole extends JFrame implements ILogListener {
    private JEditorPane displayArea;
    @SuppressWarnings("rawtypes")
    private final JComboBox logTypeComboBox;
    private LogType logType = LogType.MINIMAL;
    private HTMLDocument doc;
    private final HTMLEditorKit kit;
    @SuppressWarnings("rawtypes")
    private final JComboBox logSourceComboBox;
    private LogSource logSource = LogSource.ALL;
    @SuppressWarnings("unused")
    private LogLevel logLevel = LogLevel.INFO;
    private JButton killMCButton;

    private SimpleAttributeSet RED = new SimpleAttributeSet();
    private SimpleAttributeSet YELLOW = new SimpleAttributeSet();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public LauncherConsole() {
        setTitle("FriendsCraft 2 Launcher " + "console");
        setMinimumSize(new Dimension(800, 400));
        setPreferredSize(new Dimension(800, 400));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_fc2.png")));
        getContentPane().setLayout(new BorderLayout(0, 0));

        StyleConstants.setForeground(RED, Color.RED);
        StyleConstants.setForeground(YELLOW, Color.YELLOW);

        // setup buttons
        JPanel panel = new JPanel();

        getContentPane().add(panel, BorderLayout.SOUTH);
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton clipboard = new JButton(I18N.getLocaleString("CONSOLE_COPYCLIP"));
        clipboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane pane = new JOptionPane(I18N.getLocaleString("CONSOLE_CLIP_CONFIRM"));
                Object[] options = new String[]{I18N.getLocaleString("MAIN_YES"), I18N.getLocaleString("MAIN_CANCEL")};
                pane.setOptions(options);
                JDialog dialog = pane.createDialog(new JFrame(), I18N.getLocaleString("CONSOLE_COPYCLIP"));
                dialog.setVisible(true);
                Object obj = pane.getValue();
                int result = -1;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(obj)) {
                        result = i;
                    }
                }
                if (result == 0) {
                    StringSelection stringSelection = new StringSelection("FC2 Launcher logs:\n" + Logger.getLogs()
                            + "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]" + " Logs copied to clipboard");
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);
                }
            }
        });
        panel.add(clipboard);

        logTypeComboBox = new JComboBox(LogType.values());
        logTypeComboBox.setSelectedItem(logType);
        logTypeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                logType = (LogType) logTypeComboBox.getSelectedItem();

                // setup loglevel. If DEBUG selected show also DEBUG messages
                switch (logType) {
                    case MINIMAL:
                        logLevel = LogLevel.INFO;
                        break;
                    case EXTENDED:
                        logLevel = LogLevel.INFO;
                        break;
                    case DEBUG:
                        logLevel = LogLevel.DEBUG;
                        break;
                }

                refreshLogs();
            }
        });
        panel.add(logTypeComboBox);

        logSourceComboBox = new JComboBox(LogSource.values());
        logSourceComboBox.setSelectedItem(logSource);
        logSourceComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                logSource = (LogSource) logSourceComboBox.getSelectedItem();
                refreshLogs();
            }
        });
        panel.add(logSourceComboBox);

        JButton ircButton = new JButton(I18N.getLocaleString("CONSOLE_SUPPORT"));
        ircButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                OSUtils.browse("http://vk.com/hellcoder");
            }
        });
        panel.add(ircButton);

        killMCButton = new JButton(I18N.getLocaleString("KILL_MC"));
        killMCButton.setEnabled(false);
        killMCButton.setVisible(true);
        killMCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                GameUtils.killMC();
            }
        });
        panel.add(killMCButton);

        // setup log area
        displayArea = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };

        displayArea = new JEditorPane("text/html", "");
        displayArea.setEditable(false);
        kit = new HTMLEditorKit();
        displayArea.setEditorKit(kit);

        DefaultCaret caret = (DefaultCaret) displayArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        getContentPane().add(scrollPane);
        pack();

        refreshLogs();

        addWindowListener(new WindowAdapter() {

            @SuppressWarnings("static-access")
            @Override
            public void windowClosing(WindowEvent e) {
                Logger.removeListener(LaunchFrame.getInstance().con);
            }
        });
    }

    synchronized void refreshLogs() {
        doc = new HTMLDocument();
        displayArea.setDocument(doc);
        List<LogEntry> entries = Logger.getLogEntries();
        StringBuilder logHTML = new StringBuilder();
        for (LogEntry entry : entries) {
            if (logSource == LogSource.ALL || entry.source == logSource) {
                logHTML.append(getMessage(entry));
            }
        }
        addHTML(logHTML.toString());
    }

    private void addHTML(String html) {
        synchronized (kit) {
            try {
                kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
            } catch (BadLocationException ignored) {
                Logger.logError(ignored.getMessage(), ignored);
            } catch (IOException ignored) {
                Logger.logError(ignored.getMessage(), ignored);
            }
            displayArea.setCaretPosition(displayArea.getDocument().getLength());
        }
    }

    public void scrollToBottom() {
        displayArea.setCaretPosition(displayArea.getDocument().getLength());
    }

    private String getMessage(LogEntry entry) {
        String color = "#d8a903";
        switch (entry.level) {
            case ERROR:
                color = "#FF7070";
                break;
            case WARN:
                color = "yellow";
            case INFO:
                break;
            case UNKNOWN:
                break;
            default:
                break;
        }
        return "<font color=\"" + color + "\">" + (entry.toString(logType).replace("<", "&lt;").replace(">", "&gt;").trim().replace("\r\n", "\n").replace("\n", "<br/>")) + "</font><br/>";
    }


    public void minecraftStarted() {
        killMCButton.setEnabled(true);
        killMCButton.setVisible(true);
    }

    @Override
    public void onLogEvent(LogEntry entry) {
        if (logSource == LogSource.ALL || entry.source == logSource) {
            final LogEntry entry_ = entry;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    addHTML(getMessage(entry_));
                }
            });
        }
    }
}