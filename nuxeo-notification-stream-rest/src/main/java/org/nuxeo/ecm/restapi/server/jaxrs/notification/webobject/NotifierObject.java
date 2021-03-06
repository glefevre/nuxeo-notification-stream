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

package org.nuxeo.ecm.restapi.server.jaxrs.notification.webobject;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.nuxeo.ecm.notification.notifier.Notifier;
import org.nuxeo.ecm.restapi.server.jaxrs.notification.AbstractNotificationObject;
import org.nuxeo.ecm.webengine.model.WebObject;

/**
 * @since XXX
 */
@WebObject(type = NotifierObject.TYPE)
@Produces(APPLICATION_JSON)
public class NotifierObject extends AbstractNotificationObject {

    public static final String TYPE = "notification-notifier";

    protected Notifier notifier;

    @Override
    protected void initialize(Object... args) {
        notifier = (Notifier) args[0];
    }

    @GET
    @Path("/")
    public Object getNotifier() {
        return notifier;
    }
}
