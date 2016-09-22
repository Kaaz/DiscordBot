package discordbot.util;

import com.google.common.base.Joiner;
import discordbot.main.Launcher;
import discordbot.main.ProgramVersion;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 22-9-2016
 */
public class SelfUpdater {
	private final static String PROJECT_PATH = "H:/bot/discordbot";
	private static final Pattern versionPattern = Pattern.compile("[A-Za-z]+[-_]([0-9]+\\.[0-9]+\\.[0-9]+)[-_][A-Za-z]+.jar");

	private static ProcessBuilder makebuilder(String... arguments) {
		System.out.println("PROCESS: " + Joiner.on(" ").join(arguments));
		ProcessBuilder builder = new ProcessBuilder().command(arguments);
		builder.redirectErrorStream(true);
		Process process = null;
		return builder;
	}

	public static boolean update() throws IOException {
		try {
			File directory = new File(PROJECT_PATH);
			File[] dirContent = directory.listFiles();
			ProcessBuilder pb;
			if (!directory.exists() || (dirContent == null || dirContent.length == 0)) {
				pb = makebuilder("cmd", "/c", "git", "clone", "https://github.com/MaikWezinkhof/DiscordBot", PROJECT_PATH);
			} else {
				pb = makebuilder("cmd", "/c", "git", "-C", PROJECT_PATH, "pull", "origin", "master");
			}
			Process gitProcess = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(gitProcess.getInputStream()));
			String line;
			if (!gitProcess.waitFor(60, TimeUnit.SECONDS)) {
				System.out.println("Update process took too long");
				throw new RuntimeException("Update process took too long");
			}
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			File pomFile = new File(directory.getAbsoluteFile() + "/pom.xml");
			pb = makebuilder("cmd", "/c", "mvn", "-f", pomFile.getAbsolutePath(), "clean", "process-resources", "assembly:single");
			Process mvnProcess = pb.start();
			reader = new BufferedReader(new InputStreamReader(mvnProcess.getInputStream()));
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
			if (!mvnProcess.waitFor(60, TimeUnit.SECONDS)) {
				System.out.println("Update process took too long");
				throw new RuntimeException("Update process took too long");
			}
			File mvnTarget = new File(directory.getAbsoluteFile() + "/target");
			File[] files = mvnTarget.listFiles((dir, name) -> name.endsWith(".jar"));
			if (files.length == 0) {
				throw new RuntimeException("Maven build failed");
			}
			File newJar = files[0];
			Matcher matcher = versionPattern.matcher(newJar.getName());
			boolean needsToUpdate = false;
			if (matcher.matches()) {
				needsToUpdate = ProgramVersion.fromString(matcher.group(1)).isHigherThan(Launcher.getVersion());
			}
			return needsToUpdate;

		} catch (InterruptedException | IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}