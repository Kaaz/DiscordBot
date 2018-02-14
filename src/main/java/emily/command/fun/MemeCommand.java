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

package emily.command.fun;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import emily.command.CommandVisibility;
import emily.core.AbstractCommand;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.templates.Templates;
import emily.util.DisUtil;
import emily.util.Misc;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
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
                "meme type                             //list of all valid types",
                "meme <type> <toptext> | <bottomtext>  //make the meme!",
                "meme <type> <toptext>                 //with just toptext!",
                "",
                "example: ",
                "meme sohappy If I could use this meme | I would be so happy"
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
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (channel instanceof TextChannel) {
            TextChannel txt = (TextChannel) channel;
            if (!PermissionUtil.checkPermission(txt, txt.getGuild().getSelfMember(), Permission.MESSAGE_ATTACH_FILES)) {
                return Templates.permission_missing.format("MESSAGE_ATTACH_FILES");
            }
        }
        String msg = "Use one of the following meme types:" + "\n";
        if (memeTypes.isEmpty()) {
            loadMemeOptions();
        }
        if (args.length == 0) {
            return Templates.invalid_use.format() + "\n" +
                    msg + Misc.makeTable(new ArrayList<>(memeTypes)) + "\n" +
                    "Usage:" + "\n" +
                    DisUtil.getCommandPrefix(channel) + "meme <type> <toptext> | <bottomtext>Config.EOL+Config.EOL" + "\n" + "\n" +
                    "Example:" + "\n" +
                    "meme sohappy If I could use this meme | I would be so happy";
        }
        switch (args[0].toLowerCase()) {
            case "type":
            case "list":
                return msg + Misc.makeTable(new ArrayList<>(memeTypes));
            case "reload":
                loadMemeOptions();
                return "+1";
        }
        String type = args[0].toLowerCase();
        if (!memeTypes.contains(type)) {
            return Templates.command.meme_invalid_type.format() +
                    msg + Misc.makeTable(new ArrayList<>(memeTypes)) + "\n" + "\n" +
                    "Example:" + "\n" +
                    "meme sohappy If I could use this meme | I would be so happy";
        }
        String topText = "-";
        String botText = "-";

        if (args.length > 1) {
            String[] memeText = Joiner.on("-").join(Arrays.copyOfRange(args, 1, args.length)).replaceAll("/", "").split("\\|");
            if (memeText.length > 0) {
                if (memeText.length > 1) {
                    botText = memeText[1];
                }
                topText = memeText[0];
            }
        }
        try {
            Future<HttpResponse<String>> response = Unirest.get("https://memegen.link/" + type + "/" + URLEncoder.encode(topText, "UTF-8") + "/" + URLEncoder.encode(botText, "UTF-8") + ".jpg").asStringAsync();
            HttpResponse<String> theImg = response.get();
            BufferedImage image = ImageIO.read(theImg.getRawBody());
            File memeFile = new File("tmp/meme_" + channel.getId() + ".jpg");
            memeFile.getParentFile().mkdirs();
            if (memeFile.exists()) {
                memeFile.delete();
            }
            if (image != null) {
                ImageIO.write(image, "png", memeFile);
                bot.queue.add(channel.sendFile(memeFile), message -> memeFile.delete());
                return "";
            }
        } catch (InterruptedException | ExecutionException | IOException e) {
            e.printStackTrace();
            return "No memes for you :(";
        }
        return "The site seems to be down :thinking:";
    }

    private void loadMemeOptions() {
        try {
            Document document = Jsoup.connect("https://memegen.link/").userAgent(BotConfig.USER_AGENT).get();
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
