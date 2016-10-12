package discordbot.handler;


import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.db.WebDb;
import discordbot.exceptions.TemplateNotSetException;
import discordbot.main.Config;
import discordbot.main.DiscordBot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Handles the text templates
 * templates are stored in the database,
 */
public class Template {

	private static final Template instance = new Template();
	private DiscordBot bot = null;
	private Random rnd;
	private HashMap<String, List<String>> dictionary;

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
			List<String> list = instance.dictionary.get(keyPhrase);
			return list.get(instance.rnd.nextInt(list.size()));
		}
		if (instance.bot != null && !Config.SHOW_KEYPHRASE) {
			instance.bot.out.sendErrorToMe(new TemplateNotSetException(keyPhrase), "key", keyPhrase, "copy this", "**!template add " + keyPhrase + "** ", instance.bot);
		}
		return "**`" + keyPhrase + "`**";
	}

	/**
	 * Formatted version of Template#get(String), but surrounded by String.format
	 *
	 * @param keyPhrase  keyphrase
	 * @param parameters the parameters to put in the keyphrase
	 * @return formatted keyphrase
	 */
	public static String get(String keyPhrase, Object... parameters) {
		if (!Config.SHOW_KEYPHRASE) {
			return String.format(get(keyPhrase), parameters);
		}
		return "`" + keyPhrase + "` params: `" + Joiner.on("`, `").join(parameters) + "`";
	}

	/**
	 * Retrieves all unique keyphrases from the database
	 *
	 * @return list of keyphrases
	 */
	public static List<String> getAllKeyphrases() {
		List<String> ret = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT DISTINCT keyphrase FROM template_texts ORDER BY keyphrase ASC ")) {
			while (rs.next()) {
				ret.add(rs.getString("keyphrase"));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
		}
		return ret;
	}

	/**
	 * Retrieves a list of all unique keyphrases (used for pagination)
	 *
	 * @param maxListSize maximum amount to retrieve
	 * @param offset      how many keyphrases to skip
	 * @return list of keyphrases
	 */
	public static List<String> getAllKeyphrases(int maxListSize, int offset) {
		List<String> ret = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select("SELECT DISTINCT keyphrase FROM template_texts ORDER BY keyphrase ASC LIMIT ?, ?", offset, maxListSize)) {
			while (rs.next()) {
				ret.add(rs.getString("keyphrase"));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
		}
		return ret;
	}

	/**
	 * Retrieves the number of unique phrases
	 *
	 * @return number
	 */
	public static int uniquePhraseCount() {
		int amount = 0;
		try (ResultSet rs = WebDb.get().select("SELECT count(DISTINCT keyphrase) AS sum FROM template_texts ORDER BY keyphrase ASC ")) {
			if (rs.next()) {
				amount = rs.getInt("sum");
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
		}
		return amount;
	}

	public String[] getPhrases() {
		return dictionary.keySet().toArray(new String[dictionary.keySet().size()]);
	}

	public int countTemplates() {
		int count = 0;
		for (List<String> list : instance.dictionary.values()) {
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
		try {
			WebDb.get().query("INSERT INTO template_texts(keyphrase,text) VALUES(?, ?)", keyPhrase, text);
			if (!instance.dictionary.containsKey(keyPhrase)) {
				instance.dictionary.put(keyPhrase, new ArrayList<>());
			}
			instance.dictionary.get(keyPhrase).add(text);
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
			rs.getStatement().close();
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
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
}