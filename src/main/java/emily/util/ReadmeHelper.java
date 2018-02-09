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

import com.kaaz.configuration.ConfigurationBuilder;
import emily.core.AbstractCommand;
import emily.db.WebDb;
import emily.games.AbstractGame;
import emily.guildsettings.GSetting;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String s = "";
        List<MemberShipRole> allRoles = RoleRankings.getAllRoles();
        s += "Name | Time spend |" + "\n";
        s += "--- | --- | " + "\n";
        for (MemberShipRole role : allRoles) {
            s += role.getName() + " | ";
            s += TimeUtil.getRelativeTime((System.currentTimeMillis() + role.getMembershipTime()) / 1000L + 1000L, false, false) + "\n";
        }

        return s;
    }

    private static String readmeListOfgames() {
        GameHandler.initialize();
        GameHandler gameHandler = new GameHandler(null);
        List<AbstractGame> gameList = gameHandler.getGameList();
        String s = "";
        s += "Key | Name | Players |" + "\n";
        s += "--- | --- | --- |" + "\n";
        for (AbstractGame game : gameList) {
            s += game.getCodeName() + " | ";
            s += game.getFullname() + " | ";
            s += game.getTotalPlayers();
            s += "\n";
        }

        return s;
    }

    private static String readmeGuildConfiguration() {
        String s = "";
        Map<String, GSetting> defaults = new HashMap<>();
        for (GSetting setting : GSetting.values()) {
            defaults.put(setting.name(), setting);
        }
        ArrayList<String> skeys = new ArrayList<>(defaults.keySet());
        Collections.sort(skeys);
        s += "Key | Default | Description |" + "\n";
        s += "--- | --- | ---|" + "\n";
        for (String skey : skeys) {

            s += defaults.get(skey).name().toLowerCase() + " | ";
            s += defaults.get(skey).getDefaultValue() + " | ";
            s += defaults.get(skey).getDescription();
            s += "\n";
        }

        return s;
    }

    /**
     * makes a sorted list of all commands with description
     */
    private static String readmeCommandSimpleList() {
        String s = "";
        ArrayList<String> sortedCommandList = new ArrayList<>();
        Collections.addAll(sortedCommandList, CommandHandler.getCommands());
        Collections.sort(sortedCommandList);
        s += "Commands | | | | |" + "\n";
        s += "--- | --- | ---| ---| ---" + "\n";
        int columns = 5;
        int currentColumn = 0;
        for (String commandName : sortedCommandList) {
            AbstractCommand command = CommandHandler.getCommand(commandName);
            if (command.isListed() && command.isEnabled()) {
                s += "[" + command.getCommand() + "](#" + command.getCommand() + ")";
                if (currentColumn % columns <= (columns - 2)) {
                    s += " | ";
                } else {
                    s += "\n";
                }
                currentColumn++;
            }
        }
        return s;
    }

    private static String readmeCommandDetailsList() {
        String text = "";
        ArrayList<String> sortedCommandList = new ArrayList<>();
        Collections.addAll(sortedCommandList, CommandHandler.getCommands());
        Collections.sort(sortedCommandList);
        for (String commandName : sortedCommandList) {
            AbstractCommand command = CommandHandler.getCommand(commandName);
            if (!command.isEnabled() || !command.isListed()) {
                continue;
            }
            text += "### " + command.getCommand() + "\n" + "\n";
            text += command.getDescription() + "\n" + "\n";
            text += "Accessible though: " + command.getCommand();
            for (String alias : command.getAliases()) {
                text += ", " + alias;
            }
            text += "\n" + "\n";
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
            text += "Usable " + visibility + "\n";
            if (command.getUsage().length > 0) {
                text += "\n";
                text += "#### Usage" + "\n" + "\n";
                text += "```php" + "\n";
                for (String line : command.getUsage()) {
                    text += line + "\n";
                }
                text += ("```") + "\n";
            }
        }
        return text;
    }
}