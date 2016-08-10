package novaz.handler;


import novaz.db.WebDb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TextHandler {

	private static TextHandler instance = new TextHandler();
	private static Random rnd;
	private HashMap<String, ArrayList<String>> dictionary;

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
				try {
					WebDb.get().query("DELETE FROM template_texts WHERE keyphrase = ? AND text = ? ", keyPhrase, text);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void add(String keyPhrase, String text) {
		if (!instance.dictionary.containsKey(keyPhrase)) {
			instance.dictionary.put(keyPhrase, new ArrayList<String>());
		}
		instance.dictionary.get(keyPhrase).add(text);
		try {
			WebDb.get().query("INSERT INTO template_texts(keyphrase,text) VALUES(?, ?)", keyPhrase, text);
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		try (ResultSet rs = WebDb.get().select("SELECT text FROM template_texts WHERE keyphrase = ?", keyPhrase)) {
			instance.dictionary.get(keyPhrase).add(rs.getString("text"));
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	private void load() {
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
