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

import com.mojang.authlib.UserAuthentication;
import lombok.Getter;

@Getter
public class LoginResponse {
    private String latestVersion, downloadTicket, username, sessionID;
    private UserAuthentication auth;

    /**
     * Constructor for LoginResponse class
     *
     * @param responseString - the response from the minecraft server
     */
    public LoginResponse(String responseString) {
        String[] responseValues = responseString.split(":");
        if (responseValues.length < 4) {
            throw new IllegalArgumentException("Invalid response string.");
        } else {
            this.latestVersion = responseValues[0];
            this.downloadTicket = responseValues[1];
            this.username = responseValues[2];
            this.sessionID = responseValues[3];
        }
    }

}
