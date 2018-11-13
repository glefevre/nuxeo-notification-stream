/*
 * (C) Copyright 2018 Nuxeo (http://nuxeo.com/) and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Contributors:
 *      Nuxeo
 */

package org.nuxeo.ecm.platform.notification.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nuxeo.ecm.platform.notification.SettingsDescriptor;
import org.nuxeo.ecm.platform.notification.dispatcher.Dispatcher;

/**
 * @since XXX
 */
public class UserDispatcherSettings implements Serializable {

    private static final long serialVersionUID = 0L;

    protected Map<String, Boolean> settings;

    public UserDispatcherSettings() {
        // Empty constructor for Avro
    }

    public Map<String, Boolean> getSettings() {
        if (settings == null) {
            settings = new HashMap<>();
        }
        return settings;
    }

    public void setSettings(Map<String, Boolean> settings) {
        this.settings = settings;
    }

    public List<String> getSelectedDispatchers() {
        return settings.entrySet().stream().filter(Entry::getValue).map(Entry::getKey).collect(Collectors.toList());
    }

    public void enable(String dispatcherId) {
        getSettings().put(dispatcherId, true);
    }

    public void disable(String dispatcherId) {
        getSettings().put(dispatcherId, false);
    }

    public boolean isEnabled(String dispatcherId) {
        return getSettings().getOrDefault(dispatcherId, false);
    }

    public static UserDispatcherSettings defaultFromDescriptor(SettingsDescriptor desc) {
        UserDispatcherSettings urs = new UserDispatcherSettings();
        urs.settings = new HashMap<>();
        desc.getSettings()
            .entrySet()
            .stream()
            .filter(es -> es.getValue().isEnabled())
            .forEach(s -> urs.settings.put(s.getKey(), s.getValue().isDefault()));
        return urs;
    }

    public static UserDispatcherSettings defaultFromDispatchers(Collection<Dispatcher> dispatchers) {
        UserDispatcherSettings urs = new UserDispatcherSettings();
        urs.settings = new HashMap<>();
        dispatchers.forEach(d -> urs.settings.put(d.getName(), false));
        return urs;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
