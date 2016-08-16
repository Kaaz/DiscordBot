package novaz.handler;

import novaz.db.WebDb;
import novaz.db.table.TServers;
import novaz.main.NovaBot;
import sx.blah.discord.handle.obj.IGuild;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
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
				"SELECT s.id, name, display_name, default_value, gs.value " +
						"FROM settings s " +
						"LEFT JOIN guild_settings gs ON gs.setting_id = s.id AND gs.guild_id = ? ", id)) {
			while (rs.next()) {
				String key = rs.getString("name");
				String defaultvalue = rs.getString("default_value");
				String value = rs.getString("value");

			}
			initialized = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}