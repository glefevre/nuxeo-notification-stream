package org.nuxeo.ecm.platform.notification.dispatcher;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.lib.stream.computation.Record;

public class LogDispatcher extends Dispatcher {
    private static final Log log = LogFactory.getLog(LogDispatcher.class);

    public static int processed = 0;

    public LogDispatcher(DispatcherDescriptor desc, int nbInputStreams, int nbOutputStreams) {
        super(desc, nbInputStreams, nbOutputStreams);
    }

    public static void reset() {
        processed = 0;
    }

    @Override
    public void process(Record record) {
        processed++;
        log.error(getName() + ":" + record.toString());
    }
}
