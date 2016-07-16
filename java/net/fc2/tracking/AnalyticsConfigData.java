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
/**
 * Created at Jul 22, 2010, 11:37:36 PM
 */
package net.fc2.tracking;

/**
 * Data that is client-specific, and should be common for all tracking requests.
 * For convenience most of this data is populated automatically by
 * {@link #populateFromSystem()}.
 *
 * @author Daniel Murphy
 */
public class AnalyticsConfigData {
    private final String trackingCode;
    private String encoding = "UTF-8";
    private String screenResolution = null;
    private String colorDepth = null;
    private String userLanguage = null;
    private String flashVersion = null;
    private String userAgent = null;
    private VisitorData visitorData;

    /**
     * constructs with the tracking code and a new visitor data.
     *
     * @param argTrackingCode
     */
    public AnalyticsConfigData(String argTrackingCode) {
        this(argTrackingCode, VisitorData.newVisitor());
    }

    /**
     * constructs with the tracking code using the provided visitor data.
     *
     * @param argTrackingCode
     */
    public AnalyticsConfigData(String argTrackingCode, VisitorData visitorData) {
        if (argTrackingCode == null) {
            throw new RuntimeException("Tracking code cannot be null");
        }
        trackingCode = argTrackingCode;
        this.visitorData = visitorData;
    }

    /**
     * @return the colorDepth
     */
    public String getColorDepth() {
        return colorDepth;
    }

    /**
     * Sets the color depth of the user. like 32 bit.
     *
     * @param argColorDepth
     */
    public void setColorDepth(String argColorDepth) {
        colorDepth = argColorDepth;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the character encoding of the client. like UTF-8
     *
     * @param argEncoding the encoding to set
     */
    public void setEncoding(String argEncoding) {
        encoding = argEncoding;
    }

    /**
     * @return the flashVersion
     */
    public String getFlashVersion() {
        return flashVersion;
    }

    /**
     * Sets the flash version of the client, like "9.0 r24"
     *
     * @param argFlashVersion the flashVersion to set
     */
    public void setFlashVersion(String argFlashVersion) {
        flashVersion = argFlashVersion;
    }

    /**
     * @return the screenResolution
     */
    public String getScreenResolution() {
        return screenResolution;
    }

    /**
     * Sets the screen resolution, like "1280x800".
     *
     * @param argScreenResolution the screenResolution to set
     */
    public void setScreenResolution(String argScreenResolution) {
        screenResolution = argScreenResolution;
    }

    /**
     * @return the trackingCode
     */
    public String getTrackingCode() {
        return trackingCode;
    }

    /**
     * @return the user agent used for the network requests
     */
    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * @return the userLanguage
     */
    public String getUserLanguage() {
        return userLanguage;
    }

    /**
     * Sets the user language, like "EN-us"
     *
     * @param argUserLanguage the userLanguage to set
     */
    public void setUserLanguage(String argUserLanguage) {
        userLanguage = argUserLanguage;
    }

    /**
     * @return the visitor data, used to track unique visitors
     */
    public VisitorData getVisitorData() {
        return visitorData;
    }
}
