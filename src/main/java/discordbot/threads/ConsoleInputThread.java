package discordbot.threads;

import discordbot.main.Launcher;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleInputThread extends Thread {

	private volatile TextChannel textChannel;

	public ConsoleInputThread() {
		textChannel = null;
	}

	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (!Launcher.isBeingKilled) {
			try {
				String input = br.readLine();
				System.out.println("your input was: `" + input + "`");
				if (textChannel != null) {
					textChannel.sendMessage("\u2328  " + input).queue();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized TextChannel getTextChannel() {
		return textChannel;
	}

	public synchronized void setTextChannel(TextChannel textChannel) {
		this.textChannel = textChannel;
	}
}