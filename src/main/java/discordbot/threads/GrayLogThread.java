package discordbot.threads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.graylog2.gelfclient.*;
import org.graylog2.gelfclient.transport.GelfTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class GrayLogThread extends Thread {
	public static final Logger LOGGER = LoggerFactory.getLogger(GrayLogThread.class);
	private final Gson gson;
	private LinkedBlockingQueue<Map> itemsToLog =
			new LinkedBlockingQueue<>();
	private volatile boolean loggerTerminated = false;
	private GelfConfiguration config;
	private GelfTransport transport;
	private GelfMessageBuilder builder;

	public GrayLogThread() throws InterruptedException {
		super("graylog-writer");
		gson = new GsonBuilder().create();
		connect();
	}

	private void connect() throws InterruptedException {
		config = new GelfConfiguration(new InetSocketAddress("10.120.34.139", 12202))
				.transport(GelfTransports.UDP)
				.queueSize(512)
				.connectTimeout(5000)
				.reconnectDelay(1000)
				.tcpNoDelay(true)
				.sendBufferSize(32768);
		transport = GelfTransports.create(config);
		builder = new GelfMessageBuilder("??", "emily-bot.pw")
				.level(GelfMessageLevel.INFO)
				.additionalField("_source", "emily-bot-test")
				.additionalField("from_gelf", "true");
//		for (int i = 0; i < 5; i++) {
		int i = 1;
		GelfMessage message = builder.message("This is message #" + i).build();
		message.addAdditionalField("anderveld", "anderewaarde");
		message.setFullMessage("the full message is blablabla");
		message.setTimestamp(System.currentTimeMillis() / 1000L);
		transport.trySend(message);
//		}
		Thread.sleep(10000);
	}

	public void run() {
		try {
			Map logMessage;
			while (!loggerTerminated) {
				logMessage = itemsToLog.take();
				gson.toJson(logMessage);

			}
		} catch (InterruptedException iex) {
		} finally {
			loggerTerminated = true;
		}
	}

	public void log(Map lm) {
		if (loggerTerminated) return;
		try {
			itemsToLog.put(lm);
		} catch (InterruptedException iex) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("Unexpected interruption");
		}
	}
}
