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

package org.nuxeo.ecm.platform.notification.dispatcher;

import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.runtime.model.Descriptor;

/**
 * @since XXX
 */
@XObject("dispatcher")
public class DispatcherDescriptor implements Descriptor {
    @XNode("@id")
    protected String id;

    @XNode("@class")
    protected Class<? extends Dispatcher> dispatcherClass;

    @XNodeMap(value = "property", key = "@name", type = HashMap.class, componentType = String.class)
    protected Map<String, String> properties = new HashMap<String, String>();

    @Override
    public String getId() {
        return id;
    }

    public Dispatcher newInstance() {
        try {
            return dispatcherClass.getConstructor(DispatcherDescriptor.class, int.class, int.class) //
                                  .newInstance(this, 1, 0);
        } catch (ReflectiveOperationException e) {
            throw new NuxeoException(e);
        }
    }

    public Map<String, String> getProperties() {
        return new HashMap<>(properties);
    }
}