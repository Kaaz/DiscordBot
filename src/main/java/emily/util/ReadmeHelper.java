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

package emily.util;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.kaaz.configuration.ConfigurationBuilder;
import emily.command.meta.AbstractCommand;
import emily.db.WebDb;
import emily.games.meta.AbstractGame;
import emily.guildsettings.GSetting;
import emily.guildsettings.GuildSettingType;
import emily.guildsettings.IGuildSettingType;
import emily.guildsettings.types.EnumSettingType;
import emily.handler.CommandHandler;
import emily.handler.GameHandler;
import emily.main.BotConfig;
import emily.role.MemberShipRole;
import emily.role.RoleRankings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Collection of methods to help me out in maintaining the readme file
 */
public class ReadmeHelper {

    public static void main(String[] args) throws Exception {
        new ConfigurationBuilder(BotConfig.class, new File("application.cfg")).build(true);
        WebDb.init();
        RoleRankings.init();
        CommandHandler.initialize();

        String template = readFile("readme_template.md", StandardCharsets.UTF_8);
        template = template.replace("%_COMMANDS_LIST_SIMPLE_%", readmeCommandSimpleList());
        template = template.replace("%_LIST_OF_GAMES_%", readmeListOfgames());
        template = template.replace("%_LIST_OF_AUTO_RANKS_%", readmeListOfAutoRanks());
        template = template.replace("%_CONFIG_PER_GUILD_%", readmeGuildConfiguration());
        template = template.replace("%_COMMANDS_LIST_DETAILS_%", readmeCommandDetailsList());
        Files.write(Paths.get("./readme.md"), template.getBytes(StandardCharsets.UTF_8));
    }

    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static String readmeListOfAutoRanks() {
        StringBuilder s = new StringBuilder();
        List<MemberShipRole> allRoles = RoleRankings.getAllRoles();
        s.append("Name | Time spend |" + "\n");
        s.append("--- | --- | " + "\n");
        for (MemberShipRole role : allRoles) {
            s.append(role.getName()).append(" | ");
            s.append(TimeUtil.getRelativeTime((System.currentTimeMillis() + role.getMembershipTime()) / 1000L + 1000L, false, false)).append("\n");
        }

        return s.toString();
    }

    private static String readmeListOfgames() {
        GameHandler.initialize();
        GameHandler gameHandler = new GameHandler(null);
        List<AbstractGame> gameList = gameHandler.getGameList();
        StringBuilder s = new StringBuilder();
        s.append("Key | Name | Players |" + "\n");
        s.append("--- | --- | --- |" + "\n");
        for (AbstractGame game : gameList) {
            s.append(game.getCodeName()).append(" | ");
            s.append(game.getFullname()).append(" | ");
            s.append(game.getTotalPlayers());
            s.append("\n");
        }

        return s.toString();
    }

    private static String readmeGuildConfiguration() {
        StringBuilder s = new StringBuilder();
        Map<String, GSetting> defaults = new HashMap<>();
        for (GSetting setting : GSetting.values()) {
            defaults.put(setting.name(), setting);
        }
        ArrayList<String> skeys = new ArrayList<>(defaults.keySet());
        Collections.sort(skeys);
        for (String skey : skeys) {
            IGuildSettingType settingType = defaults.get(skey).getSettingType();
            if (settingType == GuildSettingType.INTERNAL) {
                continue;
            }
            s.append("\n### ").append(defaults.get(skey).name()).append("\n");
            s.append("default: ");
            String def = defaults.get(skey).getDefaultValue();
            if (def != null && !def.isEmpty()) {
                s.append("`").append(def).append("`");
            }
            s.append("  \nsetting-type: `").append(settingType.typeName());
            if (settingType instanceof EnumSettingType) {
                s.append(" [");
                s.append(Joiner.on(", ").join(((EnumSettingType) settingType).getValidOptions()));
                s.append("]");
            }
            s.append("`\n\n");
            s.append(defaults.get(skey).getDescription().replace("\n", "  \n"));
        }

        return s.toString();
    }

    /**
     * makes a sorted list of all commands with description
     */
    private static String readmeCommandSimpleList() {
        StringBuilder s = new StringBuilder();
        ArrayList<String> sortedCommandList = new ArrayList<>();
        Collections.addAll(sortedCommandList, CommandHandler.getCommands());
        Collections.sort(sortedCommandList);
        s.append("Commands | | | | |" + "\n");
        s.append("--- | --- | ---| ---| ---" + "\n");
        int columns = 5;
        int currentColumn = 0;
        for (String commandName : sortedCommandList) {
            AbstractCommand command = CommandHandler.getCommand(commandName);
            if (command != null && command.isListed() && command.isEnabled()) {
                s.append("[").append(command.getCommand()).append("](#").append(command.getCommand()).append(")");
                if (currentColumn % columns <= (columns - 2)) {
                    s.append(" | ");
                } else {
                    s.append("\n");
                }
                currentColumn++;
            }
        }
        return s.toString();
    }

    private static String readmeCommandDetailsList() {
        StringBuilder text = new StringBuilder();
        ArrayList<String> sortedCommandList = new ArrayList<>();
        Collections.addAll(sortedCommandList, CommandHandler.getCommands());
        Collections.sort(sortedCommandList);
        for (String commandName : sortedCommandList) {
            AbstractCommand command = CommandHandler.getCommand(commandName);
            if (command == null || !command.isEnabled() || !command.isListed()) {
                continue;
            }
            text.append("### ").append(command.getCommand()).append("\n").append("\n");
            text.append(command.getDescription()).append("\n").append("\n");
            text.append("Aliases: ").append(command.getCommand());
            for (String alias : command.getAliases()) {
                text.append(", ").append(alias);
            }
            text.append("\n" + "\n");
            String visibility;
            switch (command.getVisibility()) {
                case PRIVATE:
                    visibility = "in private channels";
                    break;
                case PUBLIC:
                    visibility = "in public  channels";
                    break;
                case BOTH:
                    visibility = "in public and private channels";
                    break;
                default:
                    visibility = "Nowhere";
                    break;
            }
            text.append("Usable ").append(visibility).append("\n");
            if (command.getUsage().length > 0) {
                text.append("\n");
                text.append("#### Usage" + "\n" + "\n");
                text.append("```php" + "\n");
                for (String line : command.getUsage()) {
                    text.append(line).append("\n");
                }
                text.append(("```") + "\n");
            }
        }
        return text.toString();
    }
}