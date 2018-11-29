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

package org.nuxeo.ecm.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nuxeo.ecm.notification.resolver.CollectionResolver.COLLECTION_DOC_ID;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.collections.api.CollectionManager;
import org.nuxeo.ecm.collections.core.adapter.CollectionMember;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.notification.message.Notification;
import org.nuxeo.ecm.notification.notifier.CounterNotifier;
import org.nuxeo.runtime.stream.StreamHelper;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

/**
 * @since XXX
 */
@RunWith(FeaturesRunner.class)
@Features(NotificationFeature.class)
@Deploy("org.nuxeo.ecm.platform.notification.stream.default")
@Deploy("org.nuxeo.ecm.platform.collections.core")
@Deploy("org.nuxeo.ecm.platform.notification.stream.default:OSGI-INF/default-contrib.xml")
public class TestCollectionNotifications {
    @Inject
    protected CoreSession session;

    @Inject
    protected CollectionManager collectionManager;

    @Inject
    protected NotificationStreamCallback nsc;

    @Inject
    protected NotificationService ns;

    protected DocumentModel doc;

    protected String collectionId;

    protected static final String RESOLVER_ID = "collection";

    @Before
    public void before() {
        doc = session.createDocumentModel("/", "my-file", "File");
        doc = session.createDocument(doc);

        DocumentModel collection = collectionManager.createCollection(session, "My Collection", "Rly?!", "/");
        collectionManager.addToCollection(collection, doc, session);

        session.save();

        doc = session.getDocument(doc.getRef());
        collectionId = collection.getId();
        CollectionMember cm = doc.getAdapter(CollectionMember.class);

        assertThat(cm).isNotNull();
        assertThat(cm.getCollectionIds()).contains(collectionId);
    }

    @Test
    public void testSubscription() {
        assertThat(ns.getSubscriptions(RESOLVER_ID, getCtx()).getUsernames()).isEmpty();

        nsc.doSubscribe("dummyUser", RESOLVER_ID, getCtx());
        assertThat(ns.getSubscriptions(RESOLVER_ID, getCtx()).getUsernames()).hasSize(1);

        assertThat(CounterNotifier.processed).isEqualTo(0);

        updateDocAndWait();

        assertThat(CounterNotifier.processed).isEqualTo(1);
        Notification last = CounterNotifier.getLast();

        assertThat(last).isNotNull();
        assertThat(last.getUsername()).isEqualTo("dummyUser");
        assertThat(last.getResolverId()).isEqualTo("collection");
    }

    @Test
    public void testDocInMultipleCollection() {
        DocumentModel collection = collectionManager.createCollection(session, "My Other Collection", "Rly -nd- ?!",
                "/");
        collectionManager.addToCollection(collection, doc, session);

        // Subscribe different user to two collections, and dummyUser to both
        nsc.doSubscribe("dummyUser", RESOLVER_ID, getCtx());
        nsc.doSubscribe("dummyOtherUser", RESOLVER_ID, Collections.singletonMap(COLLECTION_DOC_ID, collection.getId()));
        nsc.doSubscribe("dummyUser", RESOLVER_ID, Collections.singletonMap(COLLECTION_DOC_ID, collection.getId()));
        assertThat(CounterNotifier.processed).isEqualTo(0);

        updateDocAndWait();

        // Ensure dummyUser is only notified 1 time
        assertThat(CounterNotifier.processed).isEqualTo(2);
        assertThat(CounterNotifier.fullCtx.stream().map(Notification::getUsername)).containsExactlyInAnyOrder(
                "dummyOtherUser", "dummyUser");
    }

    protected void updateDocAndWait() {
        doc.setPropertyValue("dc:title", RandomStringUtils.randomAlphabetic(10));
        session.save();

        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        assertThat(StreamHelper.drainAndStop()).isTrue();
    }

    protected Map<String, String> getCtx() {
        return Collections.singletonMap(COLLECTION_DOC_ID, collectionId);
    }
}