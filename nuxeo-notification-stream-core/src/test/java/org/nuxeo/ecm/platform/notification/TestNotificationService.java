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

import static org.assertj.core.api.Assertions.assertThat;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CHECKEDIN;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_UPDATED;

import javax.inject.Inject;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.notification.dispatcher.Dispatcher;
import org.nuxeo.ecm.platform.notification.message.EventRecord;
import org.nuxeo.ecm.platform.notification.resolver.FileCreatedResolver;
import org.nuxeo.ecm.platform.notification.resolver.FileUpdatedResolver;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@Deploy("org.nuxeo.ecm.platform.notification.stream.core")
@Deploy("org.nuxeo.ecm.platform.notification.stream.core:OSGI-INF/dummy-contrib.xml")
public class TestNotificationService {
    @Inject
    NotificationService notif;

    @Inject
    CoreSession session;

    @Test
    public void testRegistration() {
        assertThat(notif.getDispatcher("dummy")).isNull();
        assertThat(notif.getDispatcher("inApp")).isNotNull();
        assertThat(notif.getDispatchers()).hasSize(2);

        assertThat(notif.getResolver("dummy")).isNull();
        assertThat(notif.getResolver("fileCreated")) //
                .isNotNull();
        assertThat(notif.getResolver("fileUpdated")) //
                .isNotNull();
        assertThat(notif.getResolvers()).hasSize(2);
    }

    @Test
    public void testDispatcherInit() {
        Dispatcher log = notif.getDispatcher("log");
        assertThat(log.getProperty("dummy", "john")).isEqualTo("john");
        assertThat(log.getProperty("dummy")).isNull();
        assertThat(log.getProperty("my-secret-key")).isEqualTo("my-secret-value");
    }

    @Test
    public void testResolverResolution() {
        EventRecord eventRecord = new EventRecord(DOCUMENT_UPDATED, "0000-0000-0000", "File", session.getPrincipal().getName());
        assertThat(notif.getResolvers(eventRecord)).hasSize(1)
                .first()
                .isInstanceOf(FileUpdatedResolver.class);

        eventRecord.setEventName(DOCUMENT_CREATED);
        assertThat(notif.getResolvers(eventRecord)).hasSize(1)
                .first()
                .isInstanceOf(FileCreatedResolver.class);

        eventRecord.setEventName(DOCUMENT_CHECKEDIN);
        assertThat(notif.getResolvers(eventRecord)).hasSize(0);
    }

    @Test
    public void testNotificationSettings() {
        NotificationComponent cmp = (NotificationComponent) notif;
        SettingsDescriptor.DispatcherSetting setting = cmp.getSetting("fileCreated").getSetting("inApp");
        assertThat(setting.isDefault()).isTrue();
        assertThat(setting.isEnabled()).isTrue();

        setting = cmp.getSetting("fileCreated").getSetting("unknown");
        assertThat(setting.isDefault()).isFalse();
        assertThat(setting.isEnabled()).isTrue();

        setting = cmp.getSetting("fileUpdated").getSetting("log");
        assertThat(setting.isEnabled()).isFalse();
    }
}