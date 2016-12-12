package discordbot.command.fun;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Emojibet;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * !ud
 * urban dictionary
 */
public class UrbanDictionaryCommand extends AbstractCommand {
	public UrbanDictionaryCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "A veritable cornucopia of streetwise lingo";
	}

	@Override
	public String getCommand() {
		return "ud";
	}

	@Override
	public boolean isListed() {
		return true;
	}

	@Override
	public String[] getUsage() {
		return new String[]{"ud <anything>  //looks up what it means on urban dictionary"};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (args.length == 0) {
			return Template.get("command_invalid_use");
		}
		String search = Joiner.on(" ").join(args);
		try {
			Future<HttpResponse<JsonNode>> future = Unirest.get("http://api.urbandictionary.com/v0/define?term=" + URLEncoder.encode(search, "UTF-8")).asJsonAsync();
			HttpResponse<JsonNode> json = future.get(30, TimeUnit.SECONDS);
			JSONArray list = json.getBody().getObject().getJSONArray("list");
			if (list.length() == 0) {
				return Template.get("command_ud_no_results", search);
			}
			JSONObject item = list.getJSONObject(0);
			return String.format("Urban Dictionary " + Config.EOL + Config.EOL
							+ "Definition for **%s**: " + Config.EOL
							+ "```" + Config.EOL
							+ "%s" + Config.EOL
							+ "```" + Config.EOL
							+ "**example**: " + Config.EOL
							+ "%s" + Config.EOL + Config.EOL
							+ "_by %s (" + Emojibet.THUMBS_UP + "%s  " + Emojibet.THUMBS_DOWN + "%s)_"
					, item.getString("word"), item.getString("definition"), item.getString("example"),
					item.getString("author"), item.getInt("thumbs_up"), item.getInt("thumbs_down"));
		} catch (Exception ignored) {
			System.out.println(ignored.getMessage());
			ignored.printStackTrace();
		}
		return Template.get("command_ud_no_results", search);
	}
}