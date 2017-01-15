package discordbot.handler;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ChatBotHandler {
	private ChatterBot chatbot;
	private final Map<String, ChatBotInstance> sessions;

	public ChatBotHandler() {
		ChatterBotFactory factory = new ChatterBotFactory();
		sessions = new ConcurrentHashMap<>();
		try {
			chatbot = factory.create(ChatterBotType.CLEVERBOT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ChatterBotSession getSession() {
		return chatbot.createSession(Locale.ENGLISH);
	}

	public void cleanCache() {
		long deleteBefore = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30);
		Iterator<Map.Entry<String, ChatBotInstance>> iterator = sessions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, ChatBotInstance> entry = iterator.next();
			if (entry.getValue().getLastInteraction() < deleteBefore) {
				sessions.remove(entry.getKey());
			}
		}
	}

	public String chat(String guildId, String input) {
		if (!sessions.containsKey(guildId)) {
			sessions.put(guildId, new ChatBotInstance(getSession()));
		}
		return sessions.get(guildId).chat(input);
	}

	private class ChatBotInstance {
		private long lastInteraction;
		private int failedAttempts = 0;
		private ChatterBotSession botsession = null;

		ChatBotInstance(ChatterBotSession session) {
			botsession = session;
		}

		public long getLastInteraction() {
			return lastInteraction;
		}

		public String chat(String input) {
			if (failedAttempts > 25) {
				return "";
			}
			try {
				failedAttempts = 0;
				lastInteraction = System.currentTimeMillis();
				return new String(botsession.think(input).getBytes("UTF-8"), "UTF-8");
			} catch (Exception ignored) {
				failedAttempts++;
			}
			return "";
		}

	}
}
