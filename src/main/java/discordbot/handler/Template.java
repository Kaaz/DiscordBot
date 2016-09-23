package discordbot.handler;


import discordbot.db.WebDb;
import discordbot.exceptions.TemplateNotSetException;
import discordbot.main.Config;
import discordbot.main.DiscordBot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Handles the text templates
 * templates are stored in the database,
 */
public class Template {

	private static final Template instance = new Template();
	private DiscordBot bot = null;
	private Random rnd;
	private HashMap<String, ArrayList<String>> dictionary;

	private Template() {
		rnd = new Random();
		load();
	}

	public static void setBot(DiscordBot bot) {
		instance.bot = bot;
	}

	public static Template getInstance() {
		return instance;
	}

	/**
	 * gets a "random" keyphrase
	 *
	 * @param keyPhrase keyphrase to return
	 * @return a random string out of the options for the keyphrase
	 */
	public static String get(String keyPhrase) {
		if (!Config.SHOW_KEYPHRASE && instance.dictionary.containsKey(keyPhrase)) {
			ArrayList<String> list = instance.dictionary.get(keyPhrase);
			return list.get(instance.rnd.nextInt(list.size()));
		}
		if (instance.bot != null) {
			instance.bot.out.sendErrorToMe(new TemplateNotSetException(keyPhrase), "key", keyPhrase, "copy this", "**!template add " + keyPhrase + "** ", instance.bot);
		}
		return "**'" + keyPhrase + "'**";
	}

	public String[] getPhrases() {
		return dictionary.keySet().toArray(new String[dictionary.keySet().size()]);
	}

	public int countTemplates() {
		int count = 0;
		for (ArrayList<String> list : instance.dictionary.values()) {
			count += list.size();
		}
		return count;
	}

	/**
	 * deletes a specific entry
	 *
	 * @param keyPhrase keyphrase
	 * @param text      text
	 */
	public void remove(String keyPhrase, String text) {
		if (instance.dictionary.containsKey(keyPhrase)) {
			if (instance.dictionary.get(keyPhrase).contains(text)) {
				instance.dictionary.get(keyPhrase).remove(text);
				try {
					WebDb.get().query("DELETE FROM template_texts WHERE keyphrase = ? AND text = ? ", keyPhrase, text);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * adds a template for a keyphrase
	 *
	 * @param keyPhrase keyphrase
	 * @param text      the text
	 */
	public void add(String keyPhrase, String text) {
		if (!instance.dictionary.containsKey(keyPhrase)) {
			instance.dictionary.put(keyPhrase, new ArrayList<>());
		}
		instance.dictionary.get(keyPhrase).add(text);
		try {
			WebDb.get().query("INSERT INTO template_texts(keyphrase,text) VALUES(?, ?)", keyPhrase, text);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Refresh only a keyphase
	 *
	 * @param keyPhrase phrase to refresh
	 */
	public void reload(String keyPhrase) {
		if (!instance.dictionary.containsKey(keyPhrase)) {
			instance.dictionary.put(keyPhrase, new ArrayList<>());
		}
		instance.dictionary.get(keyPhrase).clear();
		try (ResultSet rs = WebDb.get().select("SELECT text FROM template_texts WHERE keyphrase = ?", keyPhrase)) {
			instance.dictionary.get(keyPhrase).add(rs.getString("text"));
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	/**
	 * refreshes the data from the database
	 */
	public void load() {
		dictionary = new HashMap<>();
		try (ResultSet rs = WebDb.get().select("SELECT id, keyphrase, text FROM template_texts")) {
			while (rs.next()) {
				if (!dictionary.containsKey(rs.getString("keyphrase"))) {
					dictionary.put(rs.getString("keyphrase"), new ArrayList<>());
				}
				dictionary.get(rs.getString("keyphrase")).add(rs.getString("text"));
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
}