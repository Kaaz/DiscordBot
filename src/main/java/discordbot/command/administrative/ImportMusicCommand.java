package discordbot.command.administrative;

import com.mpatric.mp3agic.*;
import discordbot.core.AbstractCommand;
import discordbot.db.model.OMusic;
import discordbot.db.table.TMusic;
import discordbot.handler.Template;
import discordbot.main.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 27-9-2016
 */
public class ImportMusicCommand extends AbstractCommand {
	private AtomicBoolean isInProgress = new AtomicBoolean(false);
	private AtomicInteger filesImported = new AtomicInteger(0);
	private AtomicInteger filesScanned = new AtomicInteger(0);

	public ImportMusicCommand(DiscordBot bot) {
		super(bot);
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
	public String execute(String[] args, IChannel channel, IUser author) {
		if (author.getID().equals("97284813643329536") || bot.isCreator(author)) {
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
		return Template.get("command_no_permission");
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
		Mp3File mp3file = null;
		try {
			mp3file = new Mp3File(f);
		} catch (InvalidDataException | UnsupportedTagException | IOException e) {
			return false;
		}
		String title, artist;
		if (mp3file.hasId3v2Tag()) {
			ID3v2 tags = mp3file.getId3v2Tag();
			title = tags.getTitle();
			artist = tags.getArtist();
		} else if (mp3file.hasId3v1Tag()) {
			ID3v1 tags = mp3file.getId3v1Tag();
			title = tags.getTitle();
			artist = tags.getArtist();
		} else {
			return false;
		}
		if (artist == null || title == null || artist.isEmpty() || title.isEmpty()) {
			return false;
		}
		System.out.println(String.format("%s - %s", artist, title));
		OMusic record = TMusic.findByFileName(f.getAbsolutePath());
		record.artist = artist;
		record.title = title;
		record.filename = f.getAbsolutePath();
		TMusic.insert(record);
		return true;
	}
}
