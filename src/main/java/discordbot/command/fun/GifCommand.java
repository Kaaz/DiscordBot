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

package discordbot.command.fun;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import discordbot.core.AbstractCommand;
import discordbot.handler.Template;
import discordbot.main.Config;
import discordbot.main.DiscordBot;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * !joke
 * gives you a random chuck norris joke with chuck norris replaced by <@user>
 */
public class GifCommand extends AbstractCommand {
	public GifCommand() {
		super();
	}

	@Override
	public String getDescription() {
		return "Gifs from giphy";
	}

	@Override
	public String getCommand() {
		return "gif";
	}

	@Override
	public String[] getUsage() {
		return new String[]{
				"gif         //shows random gif",
				"gif <tags>  //random gif based on tags"
		};
	}

	@Override
	public String[] getAliases() {
		return new String[]{};
	}

	@Override
	public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
		try {
			String tags = "";
			if (args.length > 0) {
				tags = "&tag=" + Joiner.on("+").join(args);
			}
			HttpResponse<JsonNode> response = Unirest.get("http://api.giphy.com/v1/gifs/random?api_key=" + Config.GIPHY_TOKEN + tags).asJson();
			return response.getBody().getObject().getJSONObject("data").getString("url");
		} catch (Exception ignored) {
			//this exception is about as useful as a nipple on a male
		}
		return Template.get("command_gif_not_today");
	}
}