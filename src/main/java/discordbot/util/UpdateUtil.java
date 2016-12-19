package discordbot.util;

import discordbot.main.Launcher;
import discordbot.main.ProgramVersion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 22-9-2016
 */
public class UpdateUtil {
	private static final Pattern versionPattern = Pattern.compile("<version>([0-9]+\\.[0-9]+\\.[0-9]+)</version>");

	public static ProgramVersion getLatestVersion() {
		String request = HttpHelper.doRequest("https://raw.githubusercontent.com/Kaaz/DiscordBot/master/pom.xml");
		Matcher matcher = versionPattern.matcher(request);
		if (matcher.find()) {
			return ProgramVersion.fromString(matcher.group(1));
		}
		return Launcher.getVersion();
	}
}