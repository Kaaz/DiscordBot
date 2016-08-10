package novaz.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import novaz.core.AbstractCommand;
import novaz.handler.TextHandler;
import novaz.main.NovaBot;
import org.apache.commons.lang3.StringEscapeUtils;
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
		setCmd("catfact");
	}

	@Override
	public String execute(String[] args, IUser author) {
		boolean first = true;
		String ret = "";
		String catFact = getCatFact();
		if (catFact != null) {
			return StringEscapeUtils.unescapeHtml4(catFact);
		}
		return TextHandler.get("command_joke_not_today");
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
			return array.get("facts").getAsString();
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}
}
