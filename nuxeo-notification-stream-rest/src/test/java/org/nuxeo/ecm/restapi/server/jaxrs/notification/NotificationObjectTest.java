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

package org.nuxeo.ecm.restapi.server.jaxrs.notification;

import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NOT_MODIFIED;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.notification.NotificationFeature;
import org.nuxeo.ecm.restapi.server.jaxrs.notification.marschaller.NotifierJsonWriter;
import org.nuxeo.ecm.restapi.server.jaxrs.notification.marschaller.NotifierListJsonWriter;
import org.nuxeo.ecm.restapi.server.jaxrs.notification.marschaller.ResolverJsonWriter;
import org.nuxeo.ecm.restapi.server.jaxrs.notification.marschaller.ResolverListJsonWriter;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.stream.StreamHelper;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.ServletContainer;

import com.fasterxml.jackson.databind.JsonNode;

@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class, NotificationFeature.class })
@ServletContainer(port = 18090)
@Deploy("org.nuxeo.ecm.platform.notification.stream.rest")
@Deploy("org.nuxeo.ecm.platform.notification.stream.rest.test")
public class NotificationObjectTest extends BaseTest {

    @Test
    public void testUnknonResolver() {
        try (CloseableClientResponse res = getResponse(RequestType.GET, "/notification/resolver/missing")) {
            assertThat(res.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void testResolverWriter() throws IOException {
        JsonNode json = getResponseAsJson(RequestType.GET, "/notification/resolver/fileCreated");
        assertThat(json.get("entity-type").asText()).isEqualTo(ResolverJsonWriter.ENTITY_TYPE);
        assertThat(json.get("id").asText()).isEqualTo("fileCreated");
    }

    @Test
    public void testResolverListWriter() throws IOException {
        JsonNode json = getResponseAsJson(RequestType.GET, "/notification/resolver");
        assertThat(json.get("entity-type").asText()).isEqualTo(ResolverListJsonWriter.ENTITY_TYPE);
        assertThat(json.get("entries").isArray()).isTrue();
    }

    @Test
    public void testUnknonNotifier() {
        try (CloseableClientResponse res = getResponse(RequestType.GET, "/notification/notifier/missing")) {
            assertThat(res.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void testNotifierWriter() throws IOException {
        JsonNode json = getResponseAsJson(RequestType.GET, "/notification/notifier/log");
        assertThat(json.get("entity-type").asText()).isEqualTo(NotifierJsonWriter.ENTITY_TYPE);
        assertThat(json.get("name").asText()).isEqualTo("log");
    }

    @Test
    public void testNotifierListWriter() throws IOException {
        JsonNode json = getResponseAsJson(RequestType.GET, "/notification/notifier");
        assertThat(json.get("entity-type").asText()).isEqualTo(NotifierListJsonWriter.ENTITY_TYPE);
        assertThat(json.get("entries").isArray()).isTrue();
    }

    @Test
    public void testSubscribeAndUnsubscribe() {
        try (CloseableClientResponse res = getResponse(RequestType.POST, "/notification/resolver/fileCreated")) {
            assertThat(res.getStatus()).isEqualTo(CREATED.getStatusCode());
        }

        // Wait until computations are processed
        assertThat(StreamHelper.drainAndStop()).isTrue();

        // Second call should not modify existing registration
        try (CloseableClientResponse res = getResponse(RequestType.POST, "/notification/resolver/fileCreated")) {
            assertThat(res.getStatus()).isEqualTo(NOT_MODIFIED.getStatusCode());
        }

        try (CloseableClientResponse res = getResponse(RequestType.DELETE, "/notification/resolver/fileCreated")) {
            assertThat(res.getStatus()).isEqualTo(ACCEPTED.getStatusCode());
        }

        // Wait until computations are processed
        assertThat(StreamHelper.drainAndStop()).isTrue();

        // Second call should response not found
        try (CloseableClientResponse res = getResponse(RequestType.DELETE, "/notification/resolver/fileCreated")) {
            assertThat(res.getStatus()).isEqualTo(NOT_FOUND.getStatusCode());
        }
    }
}