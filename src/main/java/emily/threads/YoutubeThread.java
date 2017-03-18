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

package emily.threads;

import com.vdurmont.emoji.EmojiParser;
import emily.db.controllers.CBotEvent;
import emily.db.controllers.CMusic;
import emily.db.model.OBotEvent;
import emily.db.model.OMusic;
import emily.handler.Template;
import emily.main.BotContainer;
import emily.main.Config;
import emily.main.Launcher;
import emily.util.YTUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.PermissionUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Threads for grabbing tracks from youtube
 */
public class YoutubeThread extends Thread {
    private final HashSet<String> itemsInProgress = new HashSet<>();
    private final AtomicInteger counter = new AtomicInteger(0);
    private final ExecutorService executor;
    private final LinkedBlockingQueue<YoutubeTask> queue = new LinkedBlockingQueue<>();
    private final BotContainer container;
    private volatile boolean shutdownMode = false;

    public YoutubeThread(BotContainer container) throws InterruptedException {
        super("yt-to-mp3");
        this.container = container;
        executor = Executors.newFixedThreadPool(4);
    }

    /**
     * downloads a youtube video as an mp3
     *
     * @param videocode youtube video id
     * @return success or not
     */
    private static boolean downloadFileFromYoutube(String videocode) {
        List<String> infoArgs = new LinkedList<>();
        infoArgs.add(Config.YOUTUBEDL_EXE);
        infoArgs.add("--no-check-certificate");
        infoArgs.add("-x");
        if (Config.YOUTUBEDL_DEBUG_PROCESS) {
            infoArgs.add("-v");
        }
        infoArgs.add("--audio-format");
        infoArgs.add("opus");
        infoArgs.add("--audio-quality");
        infoArgs.add("0");
        infoArgs.add("--prefer-ffmpeg");
        infoArgs.add("--max-filesize");
        infoArgs.add("64m");
        if (Config.MUSIC_USE_CACHE_DIR) {
            infoArgs.add("--exec");
            infoArgs.add("mv {} " + Config.MUSIC_DIRECTORY);
            infoArgs.add("--output");
            infoArgs.add(Config.MUSIC_CACHE_DIR + videocode + ".%(ext)s");
        } else {
            infoArgs.add("--output");
            infoArgs.add(Config.MUSIC_DIRECTORY + videocode + ".%(ext)s");
        }
        infoArgs.add("https://www.youtube.com/watch?v=" + videocode);
        ProcessBuilder builder = new ProcessBuilder().command(infoArgs);
        builder.redirectErrorStream(true);
        try {
            Process process = builder.start();
            if (Config.YOUTUBEDL_DEBUG_PROCESS) {
                StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
                StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
                errorGobbler.start();
                outputGobbler.start();
            }
            process.waitFor(2, TimeUnit.MINUTES);
            process.destroy();
        } catch (IOException | InterruptedException e) {
            Launcher.logToDiscord(e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private synchronized void registerProgress(String youtubeCode) {
        itemsInProgress.add(youtubeCode);
    }

    private synchronized void unRegisterProgress(String youtubeCode) {
        itemsInProgress.remove(youtubeCode);
        counter.decrementAndGet();
    }

    public void shutown() {
        shutdownMode = true;
    }

    public synchronized boolean isInProgress(String youtubeCode) {
        return itemsInProgress.contains(youtubeCode);
    }

    public int getQueueSize() {
        return counter.get();
    }

    public void run() {
        try {
            YoutubeTask task;

            while (!shutdownMode && !Launcher.isBeingKilled) {
                try {
                    task = queue.take();
                    executor.execute(new YTWorkerWorker(task.getCode(), task));
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Launcher.logToDiscord(e);
                }
            }
        } finally {
            if (!shutdownMode && !Launcher.isBeingKilled) {
                CBotEvent.insert(OBotEvent.Level.FATAL, ":octagonal_sign:", ":musical_note:", "Youtube-dl thread is dead!");
            }
            shutdownMode = true;
        }
    }

    public void addToQueue(String youtubeCode, String youtubeTitle, Message message, Consumer<Message> callback) {
        if (shutdownMode) {
            return;
        }
        queue.offer(new YoutubeTask(youtubeCode, youtubeTitle, message, callback));
    }

    private static class StreamGobbler extends Thread {
        private InputStream is;

        public StreamGobbler(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException ex) {
                // ex.printStackTrace();
            }
        }
    }

    private class YoutubeTask {
        private final String code;
        private final String title;
        private final Message message;
        private final Consumer<Message> callback;

        public YoutubeTask(String code, String title, Message message, Consumer<Message> callback) {
            this.code = code;
            this.message = message;
            this.callback = callback;
            this.title = title;

        }

        public Consumer<Message> getCallback() {
            return callback;
        }

        public Message getMessage() {
            return message;
        }

        public String getCode() {
            return code;
        }

        public String getTitle() {
            return title;
        }
    }

    class YTWorkerWorker extends Thread {
        private final YoutubeTask task;

        YTWorkerWorker(String identifier, YoutubeTask task) {
            super("yt-to-mp3 worker #" + identifier);
            this.task = task;
        }

        public void run() {
            try {
                if (shutdownMode) {
                    return;
                }

                if (isInProgress(task.getCode())) {
                    if (task.message != null) {
                        container.getShardFor(task.getMessage().getTextChannel().getGuild().getId())
                                .out.editBlocking(task.message, Template.get("music_downloading_in_progress", task.getTitle()));
                    }
                    return;
                }
                registerProgress(task.getCode());

                final File fileCheck = new File(YTUtil.getOutputPath(task.getCode()));
                if (!fileCheck.exists()) {
                    if (task.message != null) {
                        container.getShardFor(task.getMessage().getTextChannel().getGuild().getId())
                                .out.editBlocking(task.message, Template.get("music_downloading_hang_on"));
                    }
                    downloadFileFromYoutube(task.getCode());
                }
                if (fileCheck.exists()) {
                    OMusic rec = CMusic.findByYoutubeId(task.getCode());
                    rec.youtubeTitle = (!task.getTitle().isEmpty() && !task.getTitle().equals(task.getCode())) ? EmojiParser.parseToAliases(task.getTitle()) : EmojiParser.parseToAliases(YTUtil.getTitleFromPage(task.getCode()));
                    rec.youtubecode = task.getCode();
                    rec.filename = fileCheck.toPath().toRealPath().toString();
                    rec.playCount += 1;
                    rec.fileExists = 1;
                    rec.lastManualPlaydate = System.currentTimeMillis() / 1000L;
                    CMusic.update(rec);
                    if (rec.duration == 0) {
                        YTUtil.getTrackDuration(rec);
                    }
                }
                if (task.getCallback() != null) {
                    if (task.getMessage() == null) {
                        task.getCallback().accept(null);
                        return;
                    }
                    TextChannel channel = container.getShardFor(task.getMessage().getTextChannel().getGuild().getId()).getJda().getTextChannelById(task.getMessage().getTextChannel().getId());
                    if (channel == null || !PermissionUtil.checkPermission(channel, channel.getGuild().getSelfMember(), Permission.MESSAGE_READ, Permission.MESSAGE_HISTORY)) {
                        task.getCallback().accept(null);
                        return;
                    }
                    container.getShardFor(task.getMessage().getTextChannel().getGuild().getId()).queue.add(channel.getMessageById(task.getMessage().getId()),
                            message -> {
                                if (message != null) {
                                    task.getCallback().accept(message);
                                    return;
                                }
                                task.getCallback().accept(null);
                            });
                }
            } catch (Exception e) {
                Launcher.logToDiscord(e, "yt-code", task.getCode());
            } finally {
                unRegisterProgress(task.getCode());
            }
        }
    }
}