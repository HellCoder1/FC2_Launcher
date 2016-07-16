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

import net.fc2.log.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

public class AppUtils {
    /**
     * Reads all of the data from the given stream and returns it as a string.
     *
     * @param stream the stream to read from.
     * @return the data read from the given stream as a string.
     */
    @SuppressWarnings("resource")
    public static String readString(InputStream stream) {
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    /**
     * Downloads data from the given URL and returns it as a Document
     *
     * @param url the URL to fetch
     * @return The document
     * @throws IOException, SAXException if an error occurs when reading from the stream
     */
    public static Document downloadXML(URL url) throws IOException, SAXException {
        return getXML(url.openStream());
    }

    /**
     * Reads XML from a file
     *
     * @param file the URL to fetch
     * @return The document
     * @throws IOException, SAXException if an error occurs when reading from the stream
     */
    public static Document readXML(File file) throws IOException, SAXException {
        return getXML(new FileInputStream(file));
    }

    /**
     * Reads XML from a stream
     *
     * @param stream the stream to read the document from
     * @return The document
     * @throws IOException, SAXException if an error occurs when reading from the stream
     */
    public static Document getXML(InputStream stream) throws IOException, SAXException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            return docFactory.newDocumentBuilder().parse(stream);
        } catch (ParserConfigurationException ignored) {
            Logger.logError(ignored.getMessage(), ignored);
        } catch (UnknownHostException e) {
            Logger.logError(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Simple sleep and check locking scheme
     *
     * @param b boolean to check, continue  when true
     */
    public static void waitForLock(boolean b) {
        waitForLock(b, null);
    }

    /**
     * Simple sleep and check locking scheme
     *
     * @param b boolean to check, continue when true
     * @param s String to use as locking reason for log messages
     */
    public static void waitForLock(boolean b, String s) {
        while (!b) {
            try {
                Thread.sleep(100);
                if (s != null) {
                    Logger.logInfo("Waiting for " + s);
                }
            } catch (InterruptedException e) {
            }
        }
    }
}


