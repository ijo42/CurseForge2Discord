package de.erdbeerbaerlp.curseforge;


import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Starter {
    public static final int CFG_VERSION = 5;
    public static final String PATH_PARAM = "path";
    public static final String DEBUG_PARAM = "debug";
    private static Starter instance;
    public final Map<String, Integer> cache = new HashMap<>();
    public final List<Timer> threads = new ArrayList<>();
    public final boolean cacheGenerated;
    public final boolean debug;
    private final Config config;
    public boolean cacheChanged;

    public Starter(boolean debug, String path) {
        instance = this;
        this.debug = debug;
        this.config = new Config(path, this.cache);
        this.cacheGenerated = this.config.cacheFile.exists();
        if (!(cacheGenerated || debug)) {
            System.out.println("Generating cache...");
            for (String p : config.ids) {
                try {
                    CurseAPI.project(Integer.parseInt(p.split(";;")[ 0 ])).
                            ifPresent(pr -> {
                                try {
                                    cache.put(pr.name(), pr.files().first().id());
                                } catch (CurseException e) {
                                    e.printStackTrace();
                                }
                            });
                } catch (CurseException e) {
                    if (e.getCause() instanceof SocketTimeoutException) {
                        System.out.println(e.getMessage());
                    } else {
                        e.printStackTrace();
                    }
                }
            }
            config.saveCache();
            System.out.println("Done!");
        } else {
            config.loadCache();
        }
        final EmbedMessage embedMessage = new EmbedMessage(this.config);
        for (String p : config.ids) {
            try {
                new CurseForgeUpdateThread(p, embedMessage, config).run();
            } catch (CurseException e) {
                if (!(e.getCause() instanceof SocketTimeoutException)) {
                    e.printStackTrace();
                } else {
                    System.err.println(e.getMessage());
                }
            }
        }

        new Timer(CacheSaveThread.class.getName()).
                scheduleAtFixedRate(new CacheSaveThread(), TimeUnit.SECONDS.toMillis(60),
                        TimeUnit.SECONDS.toMillis(120));
    }

    public static Starter getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        final Options o = new Options();
        o.addOption(DEBUG_PARAM, false, "Enables debug log");
        o.addOption(PATH_PARAM, true, "Where stores config and cache files");
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(o, args);
            boolean debug = line.hasOption(DEBUG_PARAM);
            String path = line.getOptionValue(PATH_PARAM, "");
            final String envVar = System.getenv("CONFIG_PATH");
            if (path.isEmpty() && envVar != null && !envVar.isEmpty() && !envVar.isBlank()) {
                path = envVar;
            }
            new Starter(debug, path);
        } catch (ParseException exp) {
            System.err.println(exp.getMessage());
        }
    }

    public static class CacheSaveThread extends TimerTask {
        private static final boolean debug = getInstance().debug;

        @Override
        public void run() {
            if (debug) {
                System.out.println("MAIN Tick");
            }
            if (getInstance().cacheChanged) {
                if (debug) {
                    System.out.println("Saving changed caches...");
                }
                getInstance().cacheChanged = false;
                getInstance().config.saveCache();
            }
            getInstance().config.loadCache();
        }
    }
}
