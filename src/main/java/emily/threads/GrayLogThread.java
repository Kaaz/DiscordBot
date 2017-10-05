/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.threads;

import emily.main.BotConfig;
import emily.main.Launcher;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sending messages to graylog
 */
public class GrayLogThread extends Thread {
    private LinkedBlockingQueue<GelfMessage> itemsToLog = new LinkedBlockingQueue<>();
    private volatile boolean loggerTerminated = false;
    private GelfTransport transport;
    private GelfMessageBuilder builder;

    public GrayLogThread() throws InterruptedException {
        super("graylog-writer");
        connect();
    }

    private void connect() throws InterruptedException {
        GelfConfiguration config = new GelfConfiguration(new InetSocketAddress(BotConfig.BOT_GRAYLOG_HOST, BotConfig.BOT_GRAYLOG_PORT))
                .transport(GelfTransports.UDP)
                .queueSize(512)
                .connectTimeout(5000)
                .reconnectDelay(1000)
                .tcpNoDelay(true)
                .sendBufferSize(32768);
        transport = GelfTransports.create(config);
        builder = new GelfMessageBuilder("??", BotConfig.BOT_WEBSITE)
                .level(GelfMessageLevel.INFO)
                .additionalField("env", BotConfig.BOT_ENV);
    }

    public void run() {
        try {
            GelfMessage logMessage;
            while (!loggerTerminated && !Launcher.isBeingKilled) {
                logMessage = itemsToLog.take();
                transport.trySend(logMessage);
            }
        } catch (InterruptedException iex) {
        } finally {
            loggerTerminated = true;
        }
    }

    /**
     * @param message the log message
     * @param type    the category of the log message
     * @param subtype the subcategory of a logmessage
     * @param args    optional extra arguments
     */
    public void log(String message, String type, String subtype, Object... args) {
        if (loggerTerminated) return;
        try {
            GelfMessage msg = builder.message(message).build();
            msg.setFullMessage(message);
            for (int i = 0; i < args.length; i += 2) {
                if (args[i] == null || args[i + 1] == null) {
                    break;
                }
                msg.addAdditionalField(String.valueOf(args[i]), args[i + 1]);
            }
            msg.addAdditionalField("event", type);
            msg.addAdditionalField("sub-event", subtype);

            msg.setTimestamp(System.currentTimeMillis() / 1000L);
            itemsToLog.put(msg);
        } catch (InterruptedException iex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Unexpected interruption");
        }
    }
}
