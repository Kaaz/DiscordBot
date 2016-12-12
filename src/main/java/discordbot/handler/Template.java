package discordbot.handler;


import com.google.api.client.repackaged.com.google.common.base.Joiner;
import discordbot.db.WebDb;
import discordbot.db.controllers.CBotEvent;
import discordbot.db.controllers.CGuild;
import discordbot.main.Config;
import discordbot.util.DisUtil;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the text templates
 * templates are stored in the database,
 */
public class Template {

	static private final Map<String, List<String>> dictionary = new ConcurrentHashMap<>();
	static private final ConcurrentHashMap<Integer, Map<String, List<String>>> guildDictionary = new ConcurrentHashMap<>();
	private static Random rnd = new Random();

	private Template() {
		initialize();
	}

	public static void removeGuild(int guildId) {
		if (guildDictionary.containsKey(guildId)) {
			guildDictionary.remove(guildId);
		}
	}

	/**
	 * gets a "random" keyphrase
	 *
	 * @param keyPhrase keyphrase to return
	 * @return a random string out of the options for the keyphrase
	 */
	public static String get(String keyPhrase) {
		return get(0, keyPhrase);
	}

	public static String get(MessageChannel channel, String keyPhrase) {
		return get(CGuild.getCachedId(channel), keyPhrase);
	}

	public static String getWithTags(MessageChannel channel, String keyphrase, User user) {
		return DisUtil.replaceTags(get(channel, keyphrase), user, channel);
	}

	public static String get(int guildId, String keyPhrase) {
		if (!Config.SHOW_KEYPHRASE) {
			if (guildId > 0 && guildDictionary.containsKey(guildId) && guildDictionary.get(guildId).containsKey(keyPhrase)) {
				List<String> list = guildDictionary.get(guildId).get(keyPhrase);
				return list.get(rnd.nextInt(list.size()));
			} else if (dictionary.containsKey(keyPhrase)) {
				List<String> list = dictionary.get(keyPhrase);
				return list.get(rnd.nextInt(list.size()));
			}
		}
		CBotEvent.insert(":warning:", ":label:", String.format("the phrase `%s` is not set!", keyPhrase));
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
		return get(0, keyPhrase, parameters);
	}

	public static String get(MessageChannel channel, String keyPhrase, Object... parameters) {
		return get(CGuild.getCachedId(channel), keyPhrase, parameters);
	}

	public static String get(int guildId, String keyPhrase, Object... parameters) {
		if (!Config.SHOW_KEYPHRASE) {
			return String.format(get(guildId, keyPhrase), parameters);
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * see {@link Template#getAllKeyphrases(int, int)}
	 *
	 * @param contains    keyphrase contains this string
	 * @param maxListSize maximum amount to retrieve
	 * @param offset      how many to skip
	 * @return list of filtered keyphrases
	 */
	public static List<String> getAllKeyphrases(String contains, int maxListSize, int offset) {
		List<String> ret = new ArrayList<>();
		try (ResultSet rs = WebDb.get().select(
				"SELECT DISTINCT keyphrase " +
						"FROM template_texts " +
						"WHERE guild_id = 0 " +
						"AND keyphrase LIKE ? " +
						"ORDER BY keyphrase ASC LIMIT ?, ?", "%" + contains + "%", offset, maxListSize)) {
			while (rs.next()) {
				ret.add(rs.getString("keyphrase"));
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
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
		try (ResultSet rs = WebDb.get().select("SELECT count(DISTINCT keyphrase) AS sum FROM template_texts WHERE guild_id = 0 ORDER BY keyphrase ASC ")) {
			if (rs.next()) {
				amount = rs.getInt("sum");
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return amount;
	}

	/**
	 * refreshes the data from the database
	 */
	public static synchronized void initialize() {
		dictionary.clear();
		guildDictionary.clear();
		try (ResultSet rs = WebDb.get().select("SELECT id,guild_id, keyphrase, text FROM template_texts")) {
			while (rs.next()) {
				String keyphrase = rs.getString("keyphrase");
				int guildId = rs.getInt("guild_id");
				if (guildId == 0) {
					if (!dictionary.containsKey(keyphrase)) {
						dictionary.put(keyphrase, new ArrayList<>());
					}
					dictionary.get(keyphrase).add(rs.getString("text"));
				} else {
					if (!guildDictionary.containsKey(guildId)) {
						guildDictionary.put(guildId, new ConcurrentHashMap<>());
					}
					if (!guildDictionary.get(guildId).containsKey(keyphrase)) {
						guildDictionary.get(guildId).put(keyphrase, new ArrayList<>());
					}
					guildDictionary.get(guildId).get(keyphrase).add(rs.getString("text"));
				}
			}
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
			e.getStackTrace();
		}
	}

	public static synchronized void initialize(int guildId) {
		if (guildDictionary.containsKey(guildId)) {
			guildDictionary.remove(guildId);
		}
		try (ResultSet rs = WebDb.get().select("SELECT id,keyphrase, text FROM template_texts WHERE guild_id = ?", guildId)) {
			while (rs.next()) {
				String keyphrase = rs.getString("keyphrase");
				if (!guildDictionary.containsKey(guildId)) {
					guildDictionary.put(guildId, new ConcurrentHashMap<>());
				}
				if (!guildDictionary.get(guildId).containsKey(keyphrase)) {
					guildDictionary.get(guildId).put(keyphrase, new ArrayList<>());
				}
				guildDictionary.get(guildId).get(keyphrase).add(rs.getString("text"));

			}
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
			e.getStackTrace();
		}
	}

	/**
	 * returns a list of all texts for specified keyphrase
	 *
	 * @param keyphrase to return a list of
	 * @return list
	 */
	public static List<String> getAllFor(String keyphrase) {
		if (dictionary.containsKey(keyphrase)) {
			return dictionary.get(keyphrase);
		}
		return new ArrayList<>();
	}

	public static List<String> getAllFor(int guildId, String keyphrase) {
		if (guildId > 0 && guildDictionary.containsKey(guildId) && guildDictionary.get(guildId).containsKey(keyphrase)) {
			return guildDictionary.get(guildId).get(keyphrase);
		}
		return getAllFor(keyphrase);
	}

	/**
	 * deletes a specific entry
	 *
	 * @param keyPhrase keyphrase
	 * @param text      text
	 */
	public static synchronized void remove(String keyPhrase, String text) {
		if (dictionary.containsKey(keyPhrase)) {
			if (dictionary.get(keyPhrase).contains(text)) {
				try {
					WebDb.get().query("DELETE FROM template_texts WHERE keyphrase = ? AND text = ? ", keyPhrase, text);
					dictionary.get(keyPhrase).remove(text);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static synchronized void remove(int guildId, String keyPhrase, String text) {
		if (guildId == 0) {
			remove(keyPhrase, text);
			return;
		}
		if (guildDictionary.containsKey(guildId) && guildDictionary.get(guildId).containsKey(keyPhrase)) {
			if (guildDictionary.get(guildId).get(keyPhrase).contains(text)) {
				try {
					WebDb.get().query("DELETE FROM template_texts WHERE keyphrase = ? AND text = ? AND guild_id = ?", keyPhrase, text, guildId);
					guildDictionary.get(guildId).get(keyPhrase).remove(text);
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
	public static synchronized void add(String keyPhrase, String text) {
		try {
			WebDb.get().query("INSERT INTO template_texts(keyphrase,text,guild_id) VALUES(?, ?, 0)", keyPhrase, text);
			if (!dictionary.containsKey(keyPhrase)) {
				dictionary.put(keyPhrase, new ArrayList<>());
			}
			dictionary.get(keyPhrase).add(text);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * adds a template for a keyphrase for a guild
	 * Only adds the template if the template exists in the dictionary
	 *
	 * @param guildId   internal guild id
	 * @param keyPhrase keyphrase
	 * @param text      the text
	 */
	public static synchronized boolean add(int guildId, String keyPhrase, String text) {
		if (!dictionary.containsKey(keyPhrase) && guildId > 0) {
			return false;
		}
		if (guildId == 0) {
			add(keyPhrase, text);
			return true;
		}
		try {
			WebDb.get().query("INSERT INTO template_texts(guild_id,keyphrase,text) VALUES(?, ?, ?)", guildId, keyPhrase, text);
			if (!guildDictionary.containsKey(guildId)) {
				guildDictionary.put(guildId, new ConcurrentHashMap<>());
			}
			if (!guildDictionary.get(guildId).containsKey(keyPhrase)) {
				guildDictionary.get(guildId).put(keyPhrase, new ArrayList<>());
			}
			guildDictionary.get(guildId).get(keyPhrase).add(text);

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public String[] getPhrases() {
		return dictionary.keySet().toArray(new String[dictionary.keySet().size()]);
	}

	public int countTemplates() {
		int count = 0;
		for (List<String> list : dictionary.values()) {
			count += list.size();
		}
		return count;
	}

	/**
	 * Refresh only a keyphase
	 *
	 * @param keyPhrase phrase to refresh
	 */
	public synchronized void reload(String keyPhrase) {
		if (!dictionary.containsKey(keyPhrase)) {
			dictionary.put(keyPhrase, new ArrayList<>());
		}
		dictionary.get(keyPhrase).clear();
		try (ResultSet rs = WebDb.get().select("SELECT text FROM template_texts WHERE keyphrase = ? AND guild_id = 0", keyPhrase)) {
			dictionary.get(keyPhrase).add(rs.getString("text"));
			rs.getStatement().close();
		} catch (SQLException e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
}