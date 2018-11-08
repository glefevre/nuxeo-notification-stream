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

package org.nuxeo.ecm.platform.notification;

import java.util.Collection;
import java.util.Map;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.platform.notification.dispatcher.Dispatcher;
import org.nuxeo.ecm.platform.notification.message.EventRecord;
import org.nuxeo.ecm.platform.notification.resolver.Resolver;
import org.nuxeo.lib.stream.computation.Topology;
import org.nuxeo.lib.stream.log.LogManager;

/**
 * @since XXX
 */
public interface NotificationService {
    /**
     * Get Dispacher instance by id.
     *
     * @param id of the Dispatcher.
     * @return a Dispatcher instance, or null if id is not registered.
     */
    Dispatcher getDispatcher(String id);

    /**
     * List all contributed dispatchers.
     * 
     * @return list of dispatcher, or an empty list otherwise.
     */
    Collection<Dispatcher> getDispatchers();

    /**
     * Get resolver instance by id
     *
     * @param id of the Resolver.
     * @return a Resolver instance, or null if id is not registered.
     */
    Resolver getResolver(String id);

    /**
     * List all contributed resolvers.
     * 
     * @return list of resolved, or an empty list otherwise.
     */
    Collection<Resolver> getResolvers();

    /**
     * List resolver that accept the event
     * 
     * @param eventRecord that has to be tested
     * @return list of resolver that handle the event, or an empty list otherwise.
     */
    Collection<Resolver> getResolvers(EventRecord eventRecord);

    /**
     * Returns Notification's Stream computation topology.
     */
    Topology buildTopology(Map<String, String> options);

    /**
     * Returns stream name which is going to be consumed to create Notification object
     */
    String getEventInputStream();

    /**
     * @return The name of the output stream of the notification processor.
     */
    String getNotificationOutputStream();

    /**
     * Returns the Log manager configured on the notification service.
     */
    LogManager getLogManager();
}