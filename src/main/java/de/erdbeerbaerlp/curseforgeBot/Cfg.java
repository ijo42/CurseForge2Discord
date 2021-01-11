package de.erdbeerbaerlp.curseforgeBot;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Cfg {
	public static final File configFile = new File("bot.conf");
	public static final File cacheFile = new File("Caches_DONT-DELETE");
	private final Config conf;
	public List<String> IDs;
	public String DefaultChannel;
	public String changlogDiscordFormat;
	public String messageDescription;
	public String updateFileLink;
	public String mentionRole;

	Cfg() {
		if (!configFile.exists()) {
			//noinspection finally
			try {
				InputStream link = (getClass().getResourceAsStream("/" + configFile.getName()));
				Files.copy(link, configFile.getAbsoluteFile().toPath());
				link.close();
				System.err.println("Please set the token and the Channel ID in the new config file");
			} catch (IOException e) {
				System.err.println("Could not extract default config file");
				e.printStackTrace();
			} finally {
				System.exit(0);
			}
		}

		conf = ConfigFactory.parseFile(configFile);
		if (!conf.hasPath("ver") || conf.getInt("ver") != Main.CFG_VERSION) {
			//noinspection finally
			try {
				System.out.println("Resetting config, creating backup...");
				final Path backupPath = Paths.get(configFile.getAbsolutePath() + ".backup.txt");
				if (backupPath.toFile().exists()) {
					System.out.println("REPLACING OLD BACKUP!!!!");
					backupPath.toFile().delete();
				}
				Files.move(configFile.toPath(), backupPath);
				InputStream link = (getClass().getResourceAsStream("/" + configFile.getName()));
				Files.copy(link, configFile.getAbsoluteFile().toPath());
				link.close();
				System.err.println("Reset completed! Please reconfigurate.");
			} catch (IOException e) {
				System.err.println("Could not reset config file!");
				e.printStackTrace();
			} finally {
				System.exit(0);
			}
		}
		loadConfig();
	}

	public void loadConfig() {
		IDs = conf.getStringList("ids");
		DefaultChannel = conf.getString("DefaultChannelID");
		changlogDiscordFormat = conf.getString("changelogDiscordFormat");
		messageDescription = conf.getString("messageDescription");
		updateFileLink = conf.getString("updateFileLink");
		mentionRole = conf.getString("mentionRole");
		//USERs = conf.getStringList("users");
	}

	void saveCache() {
		System.out.println("Attempting to save cache...");
		try {
			if (!cacheFile.exists()) //noinspection ResultOfMethodCallIgnored
				cacheFile.createNewFile();
			final PrintWriter out = new PrintWriter(cacheFile);
			Main.cache.forEach((a, b) -> out.println(a + ";;" + b));
			out.close();
		} catch (IOException e) {
			System.err.println("Failed to save cache file!+\n" + e.getMessage());
		}
	}

	void loadCache() {
		try {
			BufferedReader r = new BufferedReader(new FileReader(cacheFile));
			r.lines().forEach(this::putToCache);
			r.close();
		} catch (IOException e) {
			System.err.println("Could not load caches!\n" + e.getMessage());
		}
	}

	private void putToCache(String s) {
		final String[] ca = s.split(";;");
		if (ca.length != 2) {
			System.err.println("Could not load cache line " + s);
			return;
		}
		Main.cache.put(ca[ 0 ], Integer.parseInt(ca[ 1 ]));

	}

	boolean isNewFile(String name, int id) {
		if (!Main.cache.containsKey(name)) return true;
		return Main.cache.get(name) < id;
	}
}
