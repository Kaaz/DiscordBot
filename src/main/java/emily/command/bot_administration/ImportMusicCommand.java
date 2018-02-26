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

package emily.command.bot_administration;

import emily.command.meta.AbstractCommand;
import emily.main.DiscordBot;
import emily.permission.SimpleRank;
import emily.templates.Templates;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 27-9-2016
 */
public class ImportMusicCommand extends AbstractCommand {
    private AtomicBoolean isInProgress = new AtomicBoolean(false);
    private AtomicInteger filesImported = new AtomicInteger(0);
    private AtomicInteger filesScanned = new AtomicInteger(0);

    @Override
    public boolean isListed() {
        return false;
    }

    public ImportMusicCommand() {
        super();
    }

    @Override
    public String getDescription() {
        return "Special command for special use case. Imports music files from a directory. Only imports files with a valid id3v[1-2] tag";
    }

    @Override
    public String getCommand() {
        return "importmusic";
    }

    @Override
    public String[] getUsage() {
        return new String[]{
                "importmusic <path/to/music>  //imports a folder"
        };
    }

    private void reset() {
        filesImported.set(0);
        filesScanned.set(0);
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        SimpleRank rank = bot.security.getSimpleRank(author);
        if (author.getId().equals("97284813643329536") || rank.isAtLeast(SimpleRank.CREATOR)) {
            if (isInProgress.get()) {
                return "currently in progress :D";
            } else if (args.length > 0) {
                File file = new File(args[0]);
                if (!file.isDirectory()) {
                    return "Target is not a directory";
                }
                if (!isInProgress.getAndSet(true)) {
                    reset();
                    importDirectory(file);
                    isInProgress.set(false);
                    return String.format("Scanned %s files and imported %s files", filesScanned.toString(), filesImported.toString());
                }
            }
            return ":face_palm: Not how you use it";
        }
        return Templates.no_permission.formatGuild(channel);
    }

    public void importDirectory(File file) {
        File[] flist = file.listFiles();
        if (flist == null) {
            return;
        }
        for (File f : flist) {
            if (f.isDirectory()) {
                importDirectory(f);
            } else {
                if (importFile(f)) {
                    filesImported.incrementAndGet();
                }
                filesScanned.incrementAndGet();
            }
        }
    }

    private boolean importFile(File f) {
//		Mp3File mp3file = null;
//		OMusic record = TMusic.findByFileName(f.getAbsolutePath());
//		if (record.id > 0) {
//			return false;
//		}
//		try {
//			mp3file = new Mp3File(f);
//		} catch (InvalidDataException | UnsupportedTagException | IOException e) {
//			return false;
//		}
//		String title, artist;
//		if (mp3file.hasId3v2Tag()) {
//			ID3v2 tags = mp3file.getId3v2Tag();
//			title = tags.getTitle();
//			artist = tags.getArtist();
//		} else if (mp3file.hasId3v1Tag()) {
//			ID3v1 tags = mp3file.getId3v1Tag();
//			title = tags.getTitle();
//			artist = tags.getArtist();
//		} else {
//			return false;
//		}
//		if (artist == null || title == null || artist.isEmpty() || title.isEmpty()) {
//			return false;
//		}
//		System.out.println(String.format("%s - %s", artist, title));
//
//		record.artist = artist;
//		record.title = title;
//		record.filename = f.getAbsolutePath();
//		TMusic.insert(record);
        return true;
    }
}
