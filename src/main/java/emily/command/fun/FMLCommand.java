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

import emily.core.AbstractCommand;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.main.Launcher;
import emily.templates.Templates;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * !fml
 */
public class FMLCommand extends AbstractCommand {

    private static final int MIN_QUEUE_ITEMS = 40;
    private final BlockingQueue<String> items;

    public FMLCommand() {
        super();
        items = new LinkedBlockingQueue<>();
    }

    @Override
    public String getDescription() {
        return "fmylife! Returns a random entry from fmylife.com";
    }

    @Override
    public String getCommand() {
        return "fml";
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

        if (items.size() < MIN_QUEUE_ITEMS) {
            bot.queue.add(channel.sendTyping());
            getFMLItems();
        }
        if (!items.isEmpty()) {
            try {
                String item = StringEscapeUtils.unescapeHtml4(items.take());
                if (item.length() >= 2000) {
                    item = item.substring(0, 1999);
                }
                return item;
            } catch (InterruptedException e) {
                Launcher.logToDiscord(e, "fml-command", "interrupted");
            }
        }
        return Templates.command.fml_not_today.formatGuild(channel);
    }

    private void getFMLItems() {
        try {
            Document document = Jsoup.connect("http://fmylife.com/random").timeout(30_000).userAgent(BotConfig.USER_AGENT).get();
            if (document != null) {
                Elements fmls = document.select("p.block a[href^=/article/]");
                for (Element fml : fmls) {
                    items.add(fml.text().trim());
                }
            }
        } catch (IOException e) {
            Launcher.logToDiscord(e, "fml-command", "boken");
        }

    }
}