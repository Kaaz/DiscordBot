package discordbot.command.fun;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import discordbot.command.CommandVisibility;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import discordbot.util.Misc;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MemeCommand extends AbstractCommand {
	private final HashSet<String> memeTypes = new HashSet<>();

	@Override
	public String getDescription() {
		return "generate a meme!";
	}

	@Override
	public String getCommand() {
		return "meme";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"meme type                               //list of all valid types",
				"meme <type> <toptext> || <bottomtext>   //make the meme!",
				"",
				"example: ",
				"meme sohappy If I could use this meme || I would be so happy"
		};
	}

	@Override
	public CommandVisibility getVisibility() {
		return CommandVisibility.BOTH;
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		if (channel instanceof TextChannel) {
			if (!((TextChannel) channel).checkPermission(bot.client.getSelfInfo(), Permission.MESSAGE_ATTACH_FILES)) {
				return Template.get("permission_missing_attach_files");
			}
		}
		channel.sendTyping();
		if (memeTypes.isEmpty()) {
			loadMemeOptions();
		}
		if (args.length == 0) {
			return Template.get("command_invalid_usage");
		}
		switch (args[0].toLowerCase()) {
			case "type":
			case "list":
				String msg = "All options for type:" + Config.EOL;
				return msg + Misc.makeTable(new ArrayList<>(memeTypes));
			case "reload":
				loadMemeOptions();
				return "+1";
		}
		if (args.length < 4) {
			return Template.get("command_invalid_use");
		}
		String type = args[0].toLowerCase();
		if (!memeTypes.contains(type)) {
			return Template.get("command_meme_invalid_type");
		}
		String topText = args[0];
		String botText = "";
		if (args.length > 1) {
			String[] memeText = Joiner.on("-").join(Arrays.copyOfRange(args, 1, args.length)).replaceAll("/", "").split("\\|\\|");
			if (memeText.length > 0) {
				if (memeText.length > 1) {
					botText = memeText[1];
				}
				topText = memeText[0];
			}
		}
		Future<HttpResponse<String>> response = Unirest.get("https://memegen.link/" + type + "/" + topText + "/" + botText + ".jpg").asStringAsync();
		try {
			HttpResponse<String> theImg = response.get();
			BufferedImage image = ImageIO.read(theImg.getRawBody());
			File memeFile = new File("tmp/meme_" + channel.getId() + ".jpg");
			if (memeFile.exists()) {
				memeFile.delete();
			}
			ImageIO.write(image, "png", memeFile);
			channel.sendFileAsync(memeFile, null, message -> memeFile.delete());
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
			return "No memes for you :(";
		}
		return "";
	}

	private void loadMemeOptions() {
		try {
			Document document = Jsoup.connect("https://memegen.link/").userAgent(Config.USER_AGENT).get();
			if (document != null) {
				Elements fmls = document.select(".js-meme-selector option");
				if (!fmls.isEmpty()) {
					for (Element fml : fmls) {
						memeTypes.add(fml.val().toLowerCase());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
