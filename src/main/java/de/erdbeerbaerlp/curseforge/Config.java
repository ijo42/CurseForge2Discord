package de.erdbeerbaerlp.curseforge;

import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Config {
    private static final String configFileName = "bot.conf";
    private static final String cacheFileName = "Caches_DONT-DELETE";
    final public File cacheFile;
    private final com.typesafe.config.Config conf;
    private final Map<String, Integer> cache;
    public List<String> ids;
    public String changelogDiscordFormat;
    public String footerImage;
    public String messageDescription;
    public EmbedMessage.UpdateFileLinkMode updateFileLink;
    public String mentionRole;

    public Config(String filePath, Map<String, Integer> cache) {
        File configFile = Paths.get(filePath).resolve(configFileName).toFile();
        this.cacheFile = Paths.get(filePath).resolve(cacheFileName).toFile();
        this.cache = cache;
        if (!configFile.exists()) {
            try (InputStream link = getClass().getResourceAsStream("/" + configFileName)) {
                Files.copy(link, configFile.getAbsoluteFile().toPath());
                link.close();
                System.err.println("Please set the token and the Channel ID " +
                        "in the new config file");
            } catch (IOException e) {
                System.err.println("Could not extract default config file");
                e.printStackTrace();
            }
            System.exit(0);
        }

        this.conf = ConfigFactory.parseFile(configFile);
        if (!this.conf.hasPath("ver") || this.conf.getInt("ver") != Starter.CFG_VERSION) {
            try {
                System.out.println("Resetting config, creating backup...");
                final Path backupPath = Paths.get(configFile.getAbsolutePath() + ".backup.txt");
                if (backupPath.toFile().exists()) {
                    System.out.println("REPLACING OLD BACKUP!!!!");
                    //noinspection ResultOfMethodCallIgnored
                    backupPath.toFile().delete();
                }
                Files.move(configFile.toPath(), backupPath);
                InputStream link = getClass().getResourceAsStream(configFileName);
                Files.copy(link, configFile.getAbsoluteFile().toPath());
                link.close();
                System.err.println("Reset completed! Please reconfigurate.");
            } catch (IOException e) {
                System.err.println("Could not reset config file!");
                e.printStackTrace();
            }
            System.exit(0);
        }
        loadConfig();
    }

    public void loadConfig() {
        ids = conf.getStringList("ids");
        changelogDiscordFormat = conf.getString("changelogDiscordFormat");
        footerImage = conf.getString("footerImage");
        messageDescription = conf.getString("messageDescription");
        updateFileLink = conf.getEnum(EmbedMessage.UpdateFileLinkMode.class, "updateFileLink");
        mentionRole = conf.getString("mentionRole");
    }

    void saveCache() {
        System.out.println("Attempting to save cache...");
        try {
            if (!cacheFile.exists()) {
                cacheFile.createNewFile();
            }
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
        if (!cache.containsKey(name)) {
            return true;
        }
        return cache.get(name) < id;
    }
}
