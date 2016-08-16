package novaz.handler;

import novaz.db.WebDb;
import novaz.db.table.TServers;
import novaz.handler.guildsettings.AbstractGuildSetting;
import novaz.handler.guildsettings.DefaultGuildSettings;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guildspecific configurations, such as which channel is for music
 */
public class GuildSettingsHandler {
	private final static Map<IGuild, GuildSettingsHandler> settingInstance = new ConcurrentHashMap<>();
	private final IGuild guild;
	private final NovaBot bot;
	private int id = 0;
	private boolean initialized = false;
	private final Map<String, String> settings;

	public static GuildSettingsHandler getSettingsFor(IGuild guild, NovaBot bot) {
		if (settingInstance.containsKey(guild)) {
			return settingInstance.get(guild);
		} else {
			return new GuildSettingsHandler(guild, bot);
		}
	}

	private GuildSettingsHandler(IGuild guild, NovaBot bot) {
		this.guild = guild;
		this.bot = bot;
		this.settings = new ConcurrentHashMap<>();
		settingInstance.put(guild, this);
		this.id = TServers.findBy(guild.getID()).id;
		loadSettings();
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
}