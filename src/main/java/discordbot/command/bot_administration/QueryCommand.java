package discordbot.command.bot_administration;

import com.google.common.base.Joiner;
import discordbot.core.AbstractCommand;
import discordbot.db.WebDb;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.permission.SimpleRank;
import discordbot.util.DebugUtil;
import discordbot.util.Misc;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class QueryCommand extends AbstractCommand {
	public QueryCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "executes commandline stuff";
	}

	@Override
	public String getCommand() {
		return "query";
	}

	@Override
	public boolean isListed() {
		return false;
	}

	@Override
	public String[] getUsage() {
		return new String[]{};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (!bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN)) {
			return ":upside_down: You have to be a bot administrator";
		}
		if (args.length == 0) {
			return ":face_palm: I expected you to know how to use it";
		}
		String query = Joiner.on(" ").join(args);
		if (!query.startsWith("select")) {
			return "statements **must** start with select";
		}
		query += " LIMIT 0, 1000";
		List<String> header = new ArrayList<>();
		List<List<String>> table = new ArrayList<>();
		try (ResultSet r = WebDb.get().select(query)) {
			ResultSetMetaData metaData = r.getMetaData();
			int columnsCount = metaData.getColumnCount();
			for (int i = 0; i < columnsCount; i++) {
				header.add(metaData.getColumnName(i + 1));
			}
			while (r.next()) {
				List<String> row = new ArrayList<>();
				for (int i = 0; i < columnsCount; i++) {
					String s = String.valueOf(r.getString(i + 1)).trim();
					row.add(s.substring(0, Math.min(30, s.length())));
				}
				table.add(row);
			}
			r.getStatement().close();
			String output = Misc.makeAsciiTable(header, table, null);
			if (output.length() < 2000) {
				return output;
			} else {
				DebugUtil.handleDebug(channel, query + Config.EOL + Config.EOL + output);
				return "";
			}
		} catch (Exception e) {
			return "error in query! " + e.getMessage() + Config.EOL;
		}
	}
}