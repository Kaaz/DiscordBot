package novaz.handler;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import java.util.Locale;

public class ChatBotHandler {
	private int failedAttempts = 0;
	private ChatterBotSession botsession = null;

	public ChatterBotSession getSession() throws Exception {
		ChatterBotFactory factory = new ChatterBotFactory();
		ChatterBot bot1 = factory.create(ChatterBotType.CLEVERBOT);
		return bot1.createSession(Locale.ENGLISH);
	}

	public boolean isEnabled() {
		return failedAttempts <= 50;
	}

	public String chat(String input) {
		if (isEnabled()) {
			try {
				if (botsession == null) {
					botsession = getSession();
				}
				return botsession.think(input);
			} catch (Exception e) {
				failedAttempts++;
			}
		}
		return "";
	}
}
