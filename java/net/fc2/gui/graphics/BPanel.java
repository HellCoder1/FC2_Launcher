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

package net.fc2.gui.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;


@SuppressWarnings("serial")
public class BPanel extends JPanel {

    private BufferedImage image = null;
    private BufferedImage imageA;
    private int radius = 1;
    boolean fasterBlur = false;

    /**
     * Java image(background) blur
     *
     * @param image  picture name
     * @param radius blur radius
     */
    public BPanel(String image, int radius) {
        try {
            this.radius = radius;
            imageA = GraphicsUtilities.loadCompatibleImage(getClass().getResource(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage changeImageWidth(BufferedImage image, int width) {
        float ratio = (float) image.getWidth() / (float) image.getHeight();
        int height = (int) (width / ratio);

        BufferedImage temp = new BufferedImage(width, height,
                image.getType());
        Graphics2D g2 = temp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, 0, 0, temp.getWidth(), temp.getHeight(), null);
        g2.dispose();

        return temp;
    }

    public static ConvolveOp getGaussianBlurFilter(int radius,
                                                   boolean horizontal) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }

        int size = radius * 2 + 1;
        float[] data = new float[size];

        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }

        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        Kernel kernel;
        if (horizontal) {
            kernel = new Kernel(size, 1, data);
        } else {
            kernel = new Kernel(1, size, data);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(imageA.getWidth(), imageA.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image == null) {
            image = new BufferedImage(imageA.getWidth() + 2 * radius,
                    imageA.getHeight() + 2 * radius,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.drawImage(imageA, radius, radius, null);
            g2.dispose();

            long start = System.nanoTime();

            if (fasterBlur) {
                image = changeImageWidth(image, image.getWidth() / 2);
                image = getGaussianBlurFilter(radius / 2, true).filter(image, null);
                image = getGaussianBlurFilter(radius / 2, false).filter(image, null);
                image = changeImageWidth(image, image.getWidth() * 2);
            } else {
                image = getGaussianBlurFilter(radius, true).filter(image, null);
                image = getGaussianBlurFilter(radius, false).filter(image, null);
            }

            long delay = System.nanoTime() - start;
            System.out.println("time = " + (delay / 1000.0f / 1000.0f) + "ms for radius = " + radius);
        }

        int x = (getWidth() - image.getWidth()) / 2;
        int y = (getHeight() - image.getHeight()) / 2;
        g.drawImage(image, x, y, null);
    }
}
