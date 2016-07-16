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
package net.fc2.data;

import net.fc2.log.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Properties;

public class LauncherStyle extends Properties {

    private static final long serialVersionUID = 6370446700503387209L;

    private static LauncherStyle currentStyle;

    static {
        currentStyle = new LauncherStyle();
    }

    public Color control = new Color(40, 40, 40);
    public Color text = new Color(40, 40, 40).brighter().brighter().brighter().brighter().brighter();
    public Color nimbusBase = new Color(0, 0, 0);
    public Color nimbusFocus = new Color(40, 40, 40);
    public Color nimbusBorder = new Color(40, 40, 40);
    public Color nimbusLightBackground = new Color(40, 40, 40);
    public Color info = new Color(40, 40, 40).brighter().brighter();
    public Color nimbusSelectionBackground = new Color(40, 40, 40).brighter().brighter();
    public Color footerColor = new Color(25, 25, 25);
    public Color tabPaneBackground = new Color(255, 255, 255, 0);
    public Color tabPaneForeground = new Color(255, 255, 255);
    public Color headerImageColor = new Color(255, 255, 255);
    public Color headerColor = new Color(243, 119, 31);
    public Color headerImageHighlightColor = new Color(236, 26, 61);
    public Color headerHighlightColor = new Color(236, 26, 61);

    public static LauncherStyle getCurrentStyle() {
        return currentStyle;
    }

    //convienience method for handling header icons
    public ImageIcon filterHeaderIcon(URL u) {
        try {
            return changeColor(ImageIO.read(u), headerImageColor, headerColor, headerImageHighlightColor, headerHighlightColor);
        } catch (Exception e) {
            Logger.logWarn("error changing colors, using default instead");
        }
        return new ImageIcon(u);
    }

    public ImageIcon changeColor(BufferedImage image, Color toReplace, Color newColor, Color toReplace2, Color newColor2) {
        BufferedImage destImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = destImage.createGraphics();
        g.drawImage(image, null, 0, 0);
        g.dispose();
        for (int i = 0; i < destImage.getWidth(); i++) {
            for (int j = 0; j < destImage.getHeight(); j++) {
                Color c = new Color(image.getRGB(i, j), true);
                if (toReplace != null && compareColors(toReplace, c)) {
                    destImage.setRGB(i, j, getRGB(newColor, c));
                }
                if (toReplace2 != null && compareColors(toReplace2, c)) {
                    destImage.setRGB(i, j, getRGB(newColor2, c));
                }

            }
        }
        return new ImageIcon(destImage);
    }

    public boolean compareColors(Color b, Color c) {
        //ignores transparency, we add this back in in the getRGB method
        return c.getBlue() == b.getBlue() && c.getRed() == b.getRed() && c.getGreen() == b.getGreen();
    }

    public int getRGB(Color nw, Color oldColorWithTrans) {
        return new Color(nw.getRed(), nw.getGreen(), nw.getBlue(), oldColorWithTrans.getAlpha()).getRGB();
    }
}
