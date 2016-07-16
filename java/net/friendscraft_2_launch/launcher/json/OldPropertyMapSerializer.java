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

package net.friendscraft_2_launch.launcher.json;

import com.google.common.collect.ForwardingMultimap;
import com.google.gson.*;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.lang.reflect.Type;

public class OldPropertyMapSerializer implements JsonSerializer<PropertyMap> {

    @Override
    public JsonElement serialize(PropertyMap src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject out = new JsonObject();
        for (String key : ((ForwardingMultimap<String, Property>) src).keySet()) {
            JsonArray jsa = new JsonArray();
            for (Property p : ((ForwardingMultimap<String, Property>) src).get(key)) {
                jsa.add(new JsonPrimitive(p.getValue()));
            }
            out.add(key, jsa);
        }
        return out;
    }
}
