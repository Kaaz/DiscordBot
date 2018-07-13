/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.command.bot_administration;

import com.google.common.base.Joiner;
import emily.command.meta.AbstractCommand;
import emily.db.WebDb;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import emily.util.DebugUtil;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (!bot.security.getSimpleRank(author).isAtLeast(SimpleRank.BOT_ADMIN)) {
            return Templates.no_permission.formatGuild(channel);
        }
        if (args.length == 0) {
            return Templates.invalid_use.formatGuild(channel);
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
                DebugUtil.handleDebug(bot, channel, query + "\n\n" + output);
                return "";
            }
        } catch (SQLException e) {
            return Emojibet.ERROR + " " + e.getMessage();
        }
    }
}