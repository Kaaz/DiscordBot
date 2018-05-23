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

package emily.main;

import com.kaaz.configuration.ConfigurationBuilder;
import emily.core.ExitCode;
import emily.core.Logger;
import emily.db.DbUpdate;
import emily.db.WebDb;
import emily.db.controllers.CBotPlayingOn;
import emily.db.controllers.CGuild;
import emily.db.controllers.CMusic;
import emily.db.model.OMusic;
import emily.threads.GrayLogThread;
import emily.threads.ServiceHandlerThread;
import emily.util.YTUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.managers.AudioManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

public class Launcher {
    public volatile static boolean isBeingKilled = false;
    private static GrayLogThread GRAYLOG;
    private static BotContainer botContainer = null;
    private static ProgramVersion version = new ProgramVersion(1);


    /**
     * log all the things!
     *
     * @param message the log message
     * @param type    the category of the log message
     * @param subtype the subcategory of a logmessage
     * @param args    optional extra arguments
     */
    public static void log(String message, String type, String subtype, Object... args) {
        if (GRAYLOG != null && BotConfig.BOT_GRAYLOG_ACTIVE) {
            GRAYLOG.log(message, type, subtype, args);
        }
    }

    public static void logToDiscord(Throwable e, Object... args) {
        if (botContainer != null) {
            botContainer.reportError(e, args);
        }
    }

    public static ProgramVersion getVersion() {
        return version;
    }

    public static void main(String[] args) throws Exception {
        new ConfigurationBuilder(BotConfig.class, new File("application.cfg")).build(true);
        WebDb.init();
        Launcher.init();
        if (BotConfig.BOT_ENABLED) {
            Runtime.getRuntime().addShutdownHook(new Thread(Launcher::shutdownHook));
            try {
                botContainer = new BotContainer((CGuild.getActiveGuildCount()));
                Thread serviceHandler = new ServiceHandlerThread(botContainer);
                serviceHandler.start();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                Launcher.stop(ExitCode.SHITTY_CONFIG, e);
            }
        } else {
            Logger.fatal("Bot not enabled, enable it in the config. You can do this by setting bot_enabled=true");
            Launcher.stop(ExitCode.SHITTY_CONFIG);
        }
    }

    //
    private static void init() throws IOException, InterruptedException, SQLException {
        Properties props = new Properties();
        props.load(Launcher.class.getClassLoader().getResourceAsStream("project.properties"));
        Launcher.version = ProgramVersion.fromString(String.valueOf(props.getOrDefault("version", "1")));
        System.out.println(String.format("Started Emily with version %s", Launcher.version));
        DbUpdate dbUpdate = new DbUpdate(WebDb.get());
        dbUpdate.updateToCurrent();
        Launcher.GRAYLOG = new GrayLogThread();
        Launcher.GRAYLOG.start();
    }

    /**
     * Stop the bot!
     *
     * @param reason why!?
     */
    public static void stop(ExitCode reason) {
        stop(reason, null);
    }

    public static void stop(ExitCode reason, Exception e) {
        if (isBeingKilled) {
            return;
        }
        isBeingKilled = true;
        DiscordBot.LOGGER.error("Exiting because: " + reason);
        if (e != null) {
            System.out.println(e);
        }
        System.exit(reason.getCode());
    }

    /**
     * shutdown hook, closing connections
     *
     */
    private static void shutdownHook() {
        if (botContainer != null) {
            for (DiscordBot discordBot : botContainer.getShards()) {
                for (Guild guild : discordBot.getJda().getGuilds()) {
                    AudioManager audio = guild.getAudioManager();
                    if (audio.isConnected()) {
                        CBotPlayingOn.insert(guild.getId(), audio.getConnectedChannel().getId());
                    }
                }
                discordBot.getJda().shutdown();
            }
        }
    }

    /**
     * helper function, retrieves youtubeTitle for mp3 files which contain youtube videocode as filename
     */
    public static void fixExistingYoutubeFiles() {
        for (String file : new String[]{}) {
            System.out.println(file);
            String videocode = file.replace(".mp3", "");
            OMusic rec = CMusic.findByYoutubeId(videocode);
            rec.youtubeTitle = YTUtil.getTitleFromPage(videocode);
            rec.youtubecode = videocode;
            rec.filename = videocode + ".mp3";
            CMusic.update(rec);
        }
    }
}