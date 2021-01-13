package de.erdbeerbaerlp.curseforgeBot;

import com.typesafe.config.ConfigFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Config {
	private final static String configFileName = "bot.conf";
	private final static String cacheFileName = "Caches_DONT-DELETE";
	public final File cacheFile;
	private final com.typesafe.config.Config conf;
	public List<String> IDs;
	public Map<String, Integer> cache;
	public String changlogDiscordFormat;
	public String footerImage;
	public String messageDescription;
	public EmbedMessage.UpdateFileLinkMode updateFileLink;
	public String mentionRole;

	public Config(String filePath, Map<String, Integer> cache) {
		File configFile = new File(filePath + configFileName);
		this.cacheFile = new File(filePath + cacheFileName);
		this.cache = cache;
		if (!configFile.exists()) {
			//noinspection finally
			try (InputStream link = getClass().getResourceAsStream("/" + configFileName)) {
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

		this.conf = ConfigFactory.parseFile(configFile);
		if (!this.conf.hasPath("ver") || this.conf.getInt("ver") != BotStarter.CFG_VERSION) {
			//noinspection finally
			try {
				System.out.println("Resetting config, creating backup...");
				final Path backupPath = Paths.get(configFile.getAbsolutePath() + ".backup.txt");
				if (backupPath.toFile().exists()) {
					System.out.println("REPLACING OLD BACKUP!!!!");
					backupPath.toFile().delete();
				}
				Files.move(configFile.toPath(), backupPath);
				InputStream link = getClass().getResourceAsStream("/" + configFile.getName());
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
		changlogDiscordFormat = conf.getString("changelogDiscordFormat");
		footerImage = conf.getString("footerImage");
		messageDescription = conf.getString("messageDescription");
		updateFileLink = EmbedMessage.UpdateFileLinkMode.valueOf(
				conf.getString("updateFileLink").toUpperCase(Locale.US));
		mentionRole = conf.getString("mentionRole");
	}

	void saveCache() {
		System.out.println("Attempting to save cache...");
		try {
			if (!cacheFile.exists()) //noinspection ResultOfMethodCallIgnored
				cacheFile.createNewFile();
			final PrintWriter out = new PrintWriter(cacheFile);
			cache.forEach((a, b) -> out.println(a + ";;" + b));
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
		cache.put(ca[ 0 ], Integer.parseInt(ca[ 1 ]));
	}

	public boolean isNewFile(String name, int id) {
		if (!cache.containsKey(name)) return true;
		return cache.get(name) < id;
	}
}
