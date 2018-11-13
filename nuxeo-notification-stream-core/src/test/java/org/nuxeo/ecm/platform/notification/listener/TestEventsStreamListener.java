/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/).
 * This is unpublished proprietary source code of Nuxeo SA. All rights reserved.
 * Notice of copyright on this source code does not indicate publication.
 *
 * Contributors:
 *     Gildas Lefevre
 */
package org.nuxeo.ecm.platform.notification.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CREATED;
import static org.nuxeo.runtime.stream.StreamServiceImpl.DEFAULT_CODEC;

import java.nio.file.Path;
import java.time.Duration;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.EventContextImpl;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.notification.NotificationFeature;
import org.nuxeo.ecm.platform.notification.NotificationStreamConfig;
import org.nuxeo.ecm.platform.notification.message.EventRecord;
import org.nuxeo.lib.stream.codec.Codec;
import org.nuxeo.lib.stream.computation.Record;
import org.nuxeo.lib.stream.log.LogManager;
import org.nuxeo.lib.stream.log.LogRecord;
import org.nuxeo.lib.stream.log.LogTailer;
import org.nuxeo.lib.stream.log.chronicle.ChronicleLogManager;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.codec.CodecService;
import org.nuxeo.runtime.stream.StreamService;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.TransactionalFeature;

/**
 * Test class for the listener {@link EventsStreamListener}.
 *
 * @since XXX
 */
@RunWith(FeaturesRunner.class)
@Features(NotificationFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy("org.nuxeo.ecm.platform.notification.stream.core:OSGI-INF/test-event-listener-contrib.xml")
public class TestEventsStreamListener {

    @Inject
    protected CoreSession session;

    @Inject
    protected NotificationStreamConfig streamConfig;

    @Inject
    protected EventService eventService;

    @Inject
    protected TransactionalFeature txFeature;

    @Test
    public void listenerPushesEventToStream() throws InterruptedException {
        // Create a new document
        DocumentModel doc = session.createDocumentModel("/", "testDoc", "File");
        doc = session.createDocument(doc);
        session.save();

        // Wait for the end of the async listeners
        txFeature.nextTransaction();
        eventService.waitForAsyncCompletion();

        // Check the Record in the stream
        Record record = readRecord();
        assertThat(record.getKey()).isEqualTo("documentCreated:" + doc.getId());

        // Check the EventRecord in the Record
        byte[] decodedEvent = record.getData();
        Codec codecMessage = Framework.getService(CodecService.class).getCodec(DEFAULT_CODEC, EventRecord.class);
        EventRecord eventRecord = (EventRecord) codecMessage.decode(decodedEvent);
        assertThat(eventRecord.getEventName()).isEqualTo(DOCUMENT_CREATED);
        assertThat(eventRecord.getDocumentSourceId()).isEqualTo(doc.getId());
        assertThat(eventRecord.getUsername()).isEqualTo("Administrator");

        // Trigger a non document event
        EventContext ctx = new EventContextImpl(session, session.getPrincipal());
        eventService.fireEvent("randomEvent", ctx);
        // Wait for the end of the async listeners
        txFeature.nextTransaction();
        eventService.waitForAsyncCompletion();

        // Check the Record in the stream
        record = readRecord();
        assertThat(record.getKey()).isEqualTo("randomEvent:Administrator");
    }

    protected Record readRecord() throws InterruptedException {
        // Check the record in the stream
        Codec<Record> codec = Framework.getService(CodecService.class).getCodec(DEFAULT_CODEC, Record.class);
        LogManager logManager = streamConfig.getLogManager(streamConfig.getLogConfigNotification());
        try (LogTailer<Record> tailer = logManager.createTailer(streamConfig.getEventInputStream(),
                streamConfig.getEventInputStream(), codec)) {
            LogRecord<Record> logRecord = tailer.read(Duration.ofSeconds(5));
            tailer.commit();
            return logRecord.message();
        }
        // never close the manager this is done by the service
    }
}
