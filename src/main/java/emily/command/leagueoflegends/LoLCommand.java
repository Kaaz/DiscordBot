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

package emily.command.leagueoflegends;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import emily.core.AbstractCommand;
import emily.main.BotConfig;
import emily.main.DiscordBot;
import emily.util.Emojibet;
import emily.util.Misc;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.rithms.riot.api.ApiConfig;
import net.rithms.riot.api.RiotApi;
import net.rithms.riot.api.RiotApiException;
import net.rithms.riot.api.endpoints.static_data.constant.ChampionListTags;
import net.rithms.riot.api.endpoints.static_data.dto.Champion;
import net.rithms.riot.api.endpoints.static_data.dto.ChampionSpell;
import net.rithms.riot.api.endpoints.static_data.dto.Image;
import net.rithms.riot.constant.Platform;

import java.util.HashMap;
import java.util.Map;

public class LoLCommand extends AbstractCommand {
    private final RiotApi api;
    private Map<String, Champion> dataChampionList = null;
    private String gameVersion = null;
    private String baseUrl = null;
    private String[] skillIndex = {Emojibet.getEmojiFor("q"), Emojibet.getEmojiFor("w"), Emojibet.getEmojiFor("e"), Emojibet.getEmojiFor("r")};

    public LoLCommand() {
        super();
        ApiConfig config = new ApiConfig().setKey(BotConfig.TOKEN_RIOT_GAMES);
        api = new RiotApi(config);

    }

    @Override
    public boolean isListed() {
        return false;
    }

    @Override
    public String getDescription() {
        return "check out a league of legends champion";
    }

    @Override
    public String getCommand() {
        return "lolchamp";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    private String getImage(Image img) {
        return baseUrl + img.getGroup() + "/" + img.getFull();
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author) {
        try {
            if (gameVersion == null) {
                gameVersion = api.getDataVersions(Platform.EUW).get(0);
                baseUrl = String.format("http://ddragon.leagueoflegends.com/cdn/%s/img/", gameVersion);
            }
            if (dataChampionList == null) {
                Map<String, Champion> tmp = api.getDataChampionList(Platform.EUW, null, null, false, ChampionListTags.ALL).getData();
                dataChampionList = new HashMap<>();
                for (Map.Entry<String, Champion> entry : tmp.entrySet()) {
                    dataChampionList.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
            if (args.length == 0) {
                return "Need a champion name";
            }
            String key = null;
            String search = args[0].toLowerCase();
            if (dataChampionList.containsKey(search)) {
                key = search;
            } else {
                for (String fullKey : dataChampionList.keySet()) {
                    if (fullKey.contains(search)) {
                        key = fullKey;
                        break;
                    }
                }
            }
            if (key == null) {
                return "Can't find a champion with the name `" + args[0] + "`";
            }
            Champion c = dataChampionList.get(key);
            String description = c.getBlurb().replace("<br><br>", "\n") + "\n\n";
            EmbedBuilder e = new EmbedBuilder();
            e.setAuthor(c.getName(), null, getImage(c.getImage()));
            e.setThumbnail(getImage(c.getImage()));
            e.setTitle(c.getTitle(), null);
            description += Joiner.on(", ").join(c.getTags());
            description += "\n\n";
            description += String.format("%s Attack\n", Misc.makeStackedBar(5, c.getInfo().getAttack() / 2, Emojibet.SWORDS));
            description += String.format("%s Magic\n", Misc.makeStackedBar(5, c.getInfo().getMagic() / 2, Emojibet.EXPLOSION));
            description += String.format("%s Defense\n", Misc.makeStackedBar(5, c.getInfo().getDefense() / 2, Emojibet.DEFENSE));
            description += String.format("%s Difficulty\n", Misc.makeStackedBar(5, c.getInfo().getDifficulty() / 2, Emojibet.QUESTION_MARK));
            description += "\n**Abilities**\n\n**" + Emojibet.getEmojiFor("p") + " " + c.getPassive().getName() + "**\n";
            description += c.getPassive().getSanitizedDescription() + "\n\n";
            int skillNum = 0;
            for (ChampionSpell spell : c.getSpells()) {
                description += "**" + skillIndex[skillNum] + " " + spell.getName() + "**\n";
                description += spell.getDescription().replace("<br><br>", "\n") + "\n";
                description += "\n";
                skillNum++;
            }
            e.setDescription(description);
            bot.queue.add(channel.sendMessage(e.build()));

        } catch (RiotApiException e) {
            e.printStackTrace();
        }
        return "";
    }

    private class ReactionData {
        String champion;
        String page;

        private ReactionData(String champion, String page) {
            this.champion = champion;
            this.page = page;
        }
    }
}
