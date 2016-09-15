package novaz.command.fun;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import org.apache.commons.lang3.StringEscapeUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

/**
 * !joke
 * gives you a random chuck norris joke with chuck norris replaced by <@user>
 */
public class Joke extends AbstractCommand {
	public Joke(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "An attempt to be funny";
	}

	@Override
	public String getCommand() {
		return "joke";
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
	public String execute(String[] args, IChannel channel, IUser author) {
		IMessage msg = bot.out.sendMessage(channel, TextHandler.get("command_joke_wait"));
		String joketxt = "";
		if (new Random().nextInt(100) < 80) {
			joketxt = bot.commands.getCommand("r").execute(new String[]{"jokes"}, channel, author);
		} else {
			joketxt = getJokeFromWeb(author.getName());
		}
		try {
			msg.delete();
		} catch (MissingPermissionsException | RateLimitException | DiscordException e) {
			e.printStackTrace();
		}
		if (joketxt != null) {
			return StringEscapeUtils.unescapeHtml4(joketxt.replace(author.getName(), "<@" + author.getID() + ">"));
		}
		return TextHandler.get("command_joke_not_today");
	}

	private String getJokeFromWeb(String username) {
		try {
			URL loginurl = new URL("http://api.icndb.com/jokes/random?firstName=&lastName=" + username);
			URLConnection yc = loginurl.openConnection();
			yc.setConnectTimeout(10 * 1000);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
			String inputLine = in.readLine();
			JsonParser parser = new JsonParser();
			JsonObject array = parser.parse(inputLine).getAsJsonObject();
			return array.get("value").getAsJsonObject().get("joke").getAsString();
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}
}