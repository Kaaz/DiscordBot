package novaz.handler;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TextHandler {

	private static TextHandler instance = new TextHandler();
	private static Random rnd;
	private HashMap<String, ArrayList<String>> dictionary;
	private String channel;

	private TextHandler() {
		rnd = new Random();
		load();
	}

	public static TextHandler getInstance() {
		return instance;
	}

	public static int countTemplates() {
		int count = 0;
		for (ArrayList<String> list : instance.dictionary.values()) {
			count += list.size();
		}
		return count;
	}

	public static void remove(String keyPhrase, String text) {
		if (instance.dictionary.containsKey(keyPhrase)) {
			if (instance.dictionary.get(keyPhrase).contains(text)) {
				instance.dictionary.get(keyPhrase).remove(text);
//				Db.query("DELETE FROM text_template WHERE keyphrase = ? AND text = ? AND channel = ?", keyPhrase, text, instance.channel);
			}

		}
	}

	public static void add(String keyPhrase, String text) {
		if (!instance.dictionary.containsKey(keyPhrase)) {
			instance.dictionary.put(keyPhrase, new ArrayList<String>());
		}
		instance.dictionary.get(keyPhrase).add(text);
//		Db.query("INSERT INTO text_template(keyphrase,channel, text) VALUES(?, ?, ?)", keyPhrase, instance.channel, text);
	}

	public static String get(String keyPhrase) {
		if (instance.dictionary.containsKey(keyPhrase)) {
			ArrayList<String> list = instance.dictionary.get(keyPhrase);
			return list.get(rnd.nextInt(list.size()));
		}
		return "keyPhrase: '" + keyPhrase + "'";
	}

	public static void reload(String keyPhrase) {
		if (!instance.dictionary.containsKey(keyPhrase)) {
			instance.dictionary.put(keyPhrase, new ArrayList<String>());
		}
		instance.dictionary.get(keyPhrase).clear();
//		try (ResultSet rs = Db.select("SELECT text FROM text_template WHERE channel = ? AND keyphrase = ?", instance.channel, keyPhrase)) {
//			instance.dictionary.get(keyPhrase).add(rs.getString("text"));
//		} catch (SQLException e) {
//			System.out.println(e);
//		}
	}

	private void load() {
		dictionary = new HashMap<>();
//		try (ResultSet rs = Db.select("SELECT id, keyphrase, text FROM text_template WHERE channel = ?", this.channel)) {
//			while (rs.next()) {
//				if (!dictionary.containsKey(rs.getString("keyphrase"))) {
//					dictionary.put(rs.getString("keyphrase"), new ArrayList<String>());
//				}
//				dictionary.get(rs.getString("keyphrase")).add(rs.getString("text"));
//			}
//		} catch (SQLException e) {
//			System.out.println(e);
//		}
	}
}
