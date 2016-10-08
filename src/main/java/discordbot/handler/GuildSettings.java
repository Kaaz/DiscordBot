package discordbot.handler;

import discordbot.db.WebDb;
import discordbot.db.table.TGuild;
import discordbot.guildsettings.AbstractGuildSetting;
import discordbot.guildsettings.DefaultGuildSettings;
import sx.blah.discord.handle.obj.IChannel;
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
	private final Map<String, String> settings;
	private int id = 0;
	private boolean initialized = false;

	private GuildSettings(IGuild guild) {
		this.settings = new ConcurrentHashMap<>();
		settingInstance.put(guild, this);
		this.id = TGuild.findBy(guild.getID()).id;
		loadSettings();
	}

	/**
	 * Simplified method to get the setting for a channel instead of guild
	 *
	 * @param channel the channel to check
	 * @param settingClass   the Setting
	 * @return the setting
	 */
	public static String getFor(IChannel channel, Class<? extends AbstractGuildSetting> settingClass) {
		if (channel == null || channel.isPrivate()) {
			return DefaultGuildSettings.getDefault(settingClass);
		}
		return GuildSettings.get(channel.getGuild()).getOrDefault(settingClass);
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
		Map<String, AbstractGuildSetting> defaults = DefaultGuildSettings.getDefaults();
		for (String key : defaults.keySet()) {
			settings.put(key, defaults.get(key).getDefault());
		}
		try (ResultSet rs = WebDb.get().select(
				"SELECT name, config " +
						"FROM guild_settings s " +
						"WHERE guild = ? ", id)) {
			while (rs.next()) {
				String key = rs.getString("name");
				String value = rs.getString("config");
				if (defaults.containsKey(key)) {
					if (null != value && !value.isEmpty()) {
						settings.put(key, value);
					}
				}
			}
			rs.getStatement().close();
			initialized = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String[] getDescription(String key) {
		if (DefaultGuildSettings.isValidKey(key)) {
			return DefaultGuildSettings.get(key).getDescription();
		}
		return new String[]{};
	}

	public boolean set(String key, String value) {
		if (DefaultGuildSettings.isValidKey(key) &&
				DefaultGuildSettings.get(key).isValidValue(value)) {
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

	public String getDefaultValue(String key) {
		if (DefaultGuildSettings.isValidKey(key)) {
			return DefaultGuildSettings.get(key).getDefault();
		}
		return "";
	}
}