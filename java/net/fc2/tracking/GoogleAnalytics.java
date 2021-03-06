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
 * Created at Jul 20, 2010, 4:39:49 AM
 */
package net.fc2.tracking;

import java.util.Random;

/**
 * http://code.google.com/apis/analytics/docs/tracking/gaTrackingTroubleshooting.html#gifParameters
 *
 * @author Daniel Murphy
 */
public class GoogleAnalytics {
    public static final String URL_PREFIX = "http://www.google-analytics.com/__utm.gif";

    private AnalyticsConfigData config;
    private Random random = new Random((long) (Math.random() * Long.MAX_VALUE));

    public GoogleAnalytics(AnalyticsConfigData argConfig) {
        config = argConfig;
    }

    /**
     * @see com.dmurph.tracking.IGoogleAnalyticsURLBuilder#getGoogleAnalyticsVersion()
     */
    public String getGoogleAnalyticsVersion() {
        return "4.7.2";
    }

    /**
     * @see com.dmurph.tracking.IGoogleAnalyticsURLBuilder#buildURL(com.dmurph.tracking.AnalyticsRequestData)
     */
    public String buildURL(AnalyticsRequestData argData) {
        StringBuilder sb = new StringBuilder();
        sb.append(URL_PREFIX);

        sb.append("?utmwv=").append(getGoogleAnalyticsVersion()); // version
        sb.append("&utmn=").append(random.nextInt()); // random int so no caching

        if (argData.getHostName() != null) {
            sb.append("&utmhn=").append(getURIString(argData.getHostName())); // hostname
        }
        if (argData.getEventAction() != null && argData.getEventCategory() != null) {
            sb.append("&utmt=event");
            String category = getURIString(argData.getEventCategory());
            String action = getURIString(argData.getEventAction());

            sb.append("&utme=5(").append(category).append("*").append(action);

            if (argData.getEventLabel() != null) {
                sb.append("*").append(getURIString(argData.getEventLabel()));
            }
            sb.append(")");

            if (argData.getEventValue() != null) {
                sb.append("(").append(argData.getEventValue()).append(")");
            }
        } else if (argData.getEventAction() != null || argData.getEventCategory() != null) {
            throw new IllegalArgumentException("Event tracking must have both a category and an action");
        }
        if (config.getEncoding() != null) {
            sb.append("&utmcs=").append(getURIString(config.getEncoding())); // encoding
        } else {
            sb.append("&utmcs=-");
        }
        if (config.getScreenResolution() != null) {
            sb.append("&utmsr=").append(getURIString(config.getScreenResolution())); // screen resolution
        }
        if (config.getColorDepth() != null) {
            sb.append("&utmsc=").append(getURIString(config.getColorDepth())); // color depth
        }
        if (config.getUserLanguage() != null) {
            sb.append("&utmul=").append(getURIString(config.getUserLanguage())); // language
        }
        sb.append("&utmje=1"); // java enabled (probably)
        if (config.getFlashVersion() != null) {
            sb.append("&utmfl=").append(getURIString(config.getFlashVersion())); // flash version
        }
        if (argData.getPageTitle() != null) {
            sb.append("&utmdt=").append(getURIString(argData.getPageTitle())); // page title
        }
        sb.append("&utmhid=").append(random.nextInt());
        if (argData.getPageURL() != null) {
            sb.append("&utmp=").append(getURIString(argData.getPageURL())); // page url
        }
        sb.append("&utmac=").append(config.getTrackingCode()); // tracking code

        String utmcsr = getURIString(argData.getUtmcsr());
        String utmccn = getURIString(argData.getUtmccn());
        String utmctr = getURIString(argData.getUtmctr());
        String utmcmd = getURIString(argData.getUtmcmd());
        String utmcct = getURIString(argData.getUtmcct());

        int hostnameHash = hostnameHash(argData.getHostName());
        int visitorId = config.getVisitorData().getVisitorId();
        long timestampFirst = config.getVisitorData().getTimestampFirst();
        long timestampPrevious = config.getVisitorData().getTimestampPrevious();
        long timestampCurrent = config.getVisitorData().getTimestampCurrent();
        int visits = config.getVisitorData().getVisits();

        sb.append("&utmcc=__utma%3D").append(hostnameHash).append(".").append(visitorId).append(".").append(timestampFirst).append(".").append(timestampPrevious).append(".").append(timestampCurrent)
                .append(".").append(visits).append("%3B%2B__utmz%3D").append(hostnameHash).append(".").append(timestampCurrent).append(".1.1.utmcsr%3D").append(utmcsr).append("%7Cutmccn%3D")
                .append(utmccn).append("%7Cutmcmd%3D").append(utmcmd).append((utmctr != null ? "%7Cutmctr%3D" + utmctr : "")).append((utmcct != null ? "%7Cutmcct%3D" + utmcct : ""))
                .append("%3B&gaq=1");
        return sb.toString();
    }

    private String getURIString(String argString) {
        return (argString == null ? null : URIEncoder.encodeURI(argString));
    }

    private int hostnameHash(String hostname) {
        return 999;
    }

    /**
     * @see com.dmurph.tracking.IGoogleAnalyticsURLBuilder#resetSession()
     */
    public void resetSession() {
        config.getVisitorData().resetSession();
    }
}
