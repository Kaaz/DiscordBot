package discordbot.threads;

import discordbot.main.Config;
import org.graylog2.gelfclient.*;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

public class GrayLogThread extends Thread {
	private LinkedBlockingQueue<GelfMessage> itemsToLog =
			new LinkedBlockingQueue<>();
	private volatile boolean loggerTerminated = false;
	private GelfConfiguration config;
	private GelfTransport transport;
	private GelfMessageBuilder builder;

	public GrayLogThread() throws InterruptedException {
		super("graylog-writer");
		connect();
	}

	private void connect() throws InterruptedException {
		config = new GelfConfiguration(new InetSocketAddress(Config.BOT_GRAYLOG_HOST, Config.BOT_GRAYLOG_PORT))
				.transport(GelfTransports.UDP)
				.queueSize(512)
				.connectTimeout(5000)
				.reconnectDelay(1000)
				.tcpNoDelay(true)
				.sendBufferSize(32768);
		transport = GelfTransports.create(config);
		builder = new GelfMessageBuilder("??", Config.BOT_WEBSITE)
				.level(GelfMessageLevel.INFO)
				.additionalField("env", Config.BOT_ENV);
	}

	public void run() {
		try {
			GelfMessage logMessage;
			while (!loggerTerminated) {
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
				msg.addAdditionalField(String.valueOf(args[i]), String.valueOf(args[i + 1]));
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
