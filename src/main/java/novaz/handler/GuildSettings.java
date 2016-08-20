package novaz.handler;

import novaz.db.WebDb;
import novaz.db.table.TServers;
import novaz.handler.guildsettings.AbstractGuildSetting;
import novaz.handler.guildsettings.DefaultGuildSettings;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guild specific configurations, such as which channel is for music
 */
public class GuildSettings {
	private final static Map<IGuild, GuildSettings> settingInstance = new ConcurrentHashMap<>();
	private final IGuild guild;
	private final Map<String, String> settings;
	private int id = 0;
	private boolean initialized = false;

	private GuildSettings(IGuild guild) {
		this.guild = guild;
		this.settings = new ConcurrentHashMap<>();
		settingInstance.put(guild, this);
		this.id = TServers.findBy(guild.getID()).id;
		loadSettings();
	}

	public static GuildSettings get(IGuild guild) {
		if (settingInstance.containsKey(guild)) {
			return settingInstance.get(guild);
		} else {
			return new GuildSettings(guild);
		}
	}

	/**
	 * @param clazz class to search
	 * @return the setting or default value
	 */
	public String getOrDefault(Class<? extends AbstractGuildSetting> clazz) {
		return getOrDefault(DefaultGuildSettings.getKey(clazz));
	}

	public String getOrDefault(String key) {
		return settings.get(key);
	}

	/**
	 * (re-)loads settings for guild
	 */
	public void loadSettings() {
		if (initialized || id <= 0) {
			return;
		}
		try (ResultSet rs = WebDb.get().select(
				"SELECT name, config " +
						"FROM guild_settings s " +
						"WHERE guild = ? ", id)) {
			Map<String, AbstractGuildSetting> defaults = DefaultGuildSettings.getDefaults();
			while (rs.next()) {
				String key = rs.getString("name");
				String value = rs.getString("config");
				if (defaults.containsKey(key)) {
					if (null != value && !value.isEmpty()) {
						settings.put(key, value);
					} else {
						settings.put(key, defaults.get(key).getDefault());
					}
				}
			}
			for (String key : defaults.keySet()) {
				if (!settings.containsKey(key)) {
					settings.put(key, defaults.get(key).getDefault());
				}
			}
			initialized = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean set(String key, String value) {
		if (DefaultGuildSettings.isValidKey(key)) {
			try {
				WebDb.get().insert("INSERT INTO guild_settings (guild, name, config) VALUES(?, ?, ?) " +
						"ON DUPLICATE KEY UPDATE config=?", id, key, value, value);
				settings.put(key, value);
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public Map<String, String> getSettings() {
		return settings;
	}
}