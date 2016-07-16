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
 * Created on Jul 20, 2010, 4:53:44 AM
 */
package net.fc2.tracking;

/**
 * Tracking data that is pertinent to each individual tracking
 * request.
 *
 * @author Daniel Murphy
 */
public class AnalyticsRequestData {

    private String pageTitle = null;
    private String hostName = null;
    private String pageURL = null;
    private String eventCategory = null;
    private String eventAction = null;
    private String eventLabel = null;
    private Integer eventValue = null;
    //	utmcsr
    //	Identifies a search engine, newsletter name, or other source specified in the
    //	utm_source query parameter See the Marketing Campaign Tracking
    //	section for more information about query parameters.
    //
    //	utmccn
    //	Stores the campaign name or value in the utm_campaign query parameter.
    //
    //	utmctr
    //	Identifies the keywords used in an organic search or the value in the utm_term query parameter.
    //
    //	utmcmd
    //	A campaign medium or value of utm_medium query parameter.
    //
    //	utmcct
    //	Campaign content or the content of a particular ad (used for A/B testing)
    //	The value from utm_content query parameter.
    // referal:
    //utmcsr=forums.jinx.com|utmcct=/topic.asp|utmcmd=referral
    //utmcsr=rolwheels.com|utmccn=(referral)|utmcmd=referral|utmcct=/rol_dhuez_wheels.php
    // search:
    // utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=rol%20wheels

    // utmcsr%3D(direct)%7Cutmccn%D(direct)%7utmcmd%3D(none)
    private String utmcsr = "(direct)";
    private String utmccn = "(direct)";
    private String utmctr = null;
    private String utmcmd = "(none)";
    private String utmcct = null;

    public void setReferrer(String argSite, String argPage) {
        utmcmd = "referral";
        utmcct = argPage;
        utmccn = "(referral)";
        utmcsr = argSite;
        utmctr = null;
    }

    public void setSearchReferrer(String argSearchSource, String argSearchKeywords) {
        utmcsr = argSearchSource;
        utmctr = argSearchKeywords;
        utmcmd = "organic";
        utmccn = "(organic)";
        utmcct = null;
    }

    /**
     * @return the utmcsr
     */
    public String getUtmcsr() {
        return utmcsr;
    }

    /**
     * @return the utmccn
     */
    public String getUtmccn() {
        return utmccn;
    }

    /**
     * @return the utmctr
     */
    public String getUtmctr() {
        return utmctr;
    }

    /**
     * @return the utmcmd
     */
    public String getUtmcmd() {
        return utmcmd;
    }

    /**
     * @return the utmcct
     */
    public String getUtmcct() {
        return utmcct;
    }

    /**
     * @return the eventAction
     */
    public String getEventAction() {
        return eventAction;
    }

    /**
     * Sets the event action, which is required for
     * tracking events.
     *
     * @param argEventAction the eventAction to set
     */
    public void setEventAction(String argEventAction) {
        eventAction = argEventAction;
    }

    /**
     * @return the eventCategory
     */
    public String getEventCategory() {
        return eventCategory;
    }

    /**
     * Sets the event category, which is required for
     * tracking events.
     *
     * @param argEventCategory the eventCategory to set
     */
    public void setEventCategory(String argEventCategory) {
        eventCategory = argEventCategory;
    }

    /**
     * @return the eventLabel
     */
    public String getEventLabel() {
        return eventLabel;
    }

    /**
     * Sets the event label, which is optional for
     * tracking events.
     *
     * @param argEventLabel the eventLabel to set
     */
    public void setEventLabel(String argEventLabel) {
        eventLabel = argEventLabel;
    }

    /**
     * @return the eventValue
     */
    public Integer getEventValue() {
        return eventValue;
    }

    /**
     * Sets the event value, which is optional for tracking
     * events.
     *
     * @param argEventValue the eventValue to set
     */
    public void setEventValue(Integer argEventValue) {
        eventValue = argEventValue;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * The host name of the page
     *
     * @param argHostName the hostName to set
     */
    public void setHostName(String argHostName) {
        hostName = argHostName;
    }

    /**
     * @return the contentTitle
     */
    public String getPageTitle() {
        return pageTitle;
    }

    /**
     * Sets the page title, which will be the Content Title
     * in Google Analytics
     *
     * @param argContentTitle the contentTitle to set
     */
    public void setPageTitle(String argContentTitle) {
        pageTitle = argContentTitle;
    }

    /**
     * @return the pageURL
     */
    public String getPageURL() {
        return pageURL;
    }

    /**
     * The page url, which is required.  Traditionally
     * this is of the form "/content/page.html", but you can
     * put anything here (like "/com/dmurph/test.java").
     *
     * @param argPageURL the pageURL to set
     */
    public void setPageURL(String argPageURL) {
        pageURL = argPageURL;
    }
}
