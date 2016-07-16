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
package net.fc2.util;

import net.fc2.gui.LaunchFrame;
import net.fc2.log.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class ErrorUtils {

    public static void tossError(String output) {
        JOptionPane.showMessageDialog(LaunchFrame.getInstance(), output, "ERROR!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Writes error into log and shows error in message dialog
     * <p/>
     * Parameter for logging system and for message dialog are given separately and
     * this method is optimal to show localized message in message dialog but
     * to log error message in english.
     *
     * @param output String to show in message dialog
     * @param log    String to log
     */
    public static void tossError(String output, String log) {
        Logger.logError(log);
        JOptionPane.showMessageDialog(LaunchFrame.getInstance(), output, "ERROR!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Writes error and exception stacktrace into log and shows error in message dialog
     *
     * @param output Strong to log and show in message dialog
     * @param t      Exception to to log
     */
    public static void tossError(String output, Throwable t) {
        Logger.logError(output, t);
        JOptionPane.showMessageDialog(LaunchFrame.getInstance(), output, "ERROR!", JOptionPane.ERROR_MESSAGE);
    }

    public static void showClickableMessage(String message, String url, String urlname) {
        JLabel l = new JLabel();
        Font font = l.getFont();
        StringBuffer html = new StringBuffer("");
        html.append("<html><body style=\"" +
                "font-family:" + font.getFamily() + ";" +
                "font-weight:" + (font.isBold() ? "bold" : "normal") + ";" +
                "font-size:" + font.getSize() + "pt;" +
                "\">");

        html.append(message + " ");
        if (url != null) {
            html.append("<a href=\"" + url + "\">" + urlname + "</a>");
        }

        JEditorPane ep = new JEditorPane("text/html", html.toString());
        ep.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                    OSUtils.browse(e.getURL().toString()); // roll your own link launcher or use Desktop if J6+
            }
        });
        ep.setEditable(false);
        JOptionPane.showMessageDialog(LaunchFrame.getInstance(), ep);
    }
}
