package novaz.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import org.apache.commons.lang3.StringEscapeUtils;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * !catfact
 * gives you a random cat fact
 */
public class Cats extends AbstractCommand {
	public Cats(NovaBot b) {
		super(b);
	}

	@Override
	public String getDescription() {
		return "Cat facts!";
	}

	@Override
	public String getCommand() {
		return "catfact";
	}

	@Override
	public String getUsage() {
		return "";
	}

	public static String getCatFact() {
		try {
			URL loginurl = new URL("http://catfacts-api.appspot.com/api/facts");
			URLConnection yc = loginurl.openConnection();
			yc.setConnectTimeout(10 * 1000);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							yc.getInputStream()));
			String inputLine = in.readLine();
			JsonParser parser = new JsonParser();
			JsonObject array = parser.parse(inputLine).getAsJsonObject();
			return ":cat:  " + array.get("facts").getAsString();
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	@Override
	public String execute(String[] args, IChannel channel, IUser author) {
		String catFact = getCatFact();
		if (catFact != null) {
			return StringEscapeUtils.unescapeHtml4(catFact);
		}
		return TextHandler.get("command_catfact_not_today");
	}
}
