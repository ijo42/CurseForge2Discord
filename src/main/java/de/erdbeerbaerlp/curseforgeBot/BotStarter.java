package de.erdbeerbaerlp.curseforgeBot;


import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import org.apache.commons.cli.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BotStarter {
	public static final int CFG_VERSION = 5;
	private static BotStarter instance;
	public final Map<String, Integer> cache = new HashMap<>();
	public final List<Timer> threads = new ArrayList<>();
	public final boolean cacheGenerated;
	public final boolean debug;
	private final Config config;
	public boolean cacheChanged;

	public BotStarter(boolean debug, String path) {
		instance = this;
		this.debug = debug;
		this.config = new Config(path, this.cache);
		this.cacheGenerated = this.config.cacheFile.exists();
		if (!(cacheGenerated || debug)) {
			System.out.println("Generating cache...");
			for (String p : config.IDs) {
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
					e.printStackTrace();
				}
			}
			config.saveCache();
			System.out.println("Done!");
		} else config.loadCache();
		final EmbedMessage embedMessage = new EmbedMessage(this.config);
		for (String p : config.IDs)
			try {
				new CurseForgeUpdateThread(p, embedMessage, config).run();
			} catch (CurseException e) {
				e.printStackTrace();
			}

		new Timer(CacheSaveThread.class.getName()).
				scheduleAtFixedRate(new CacheSaveThread(), TimeUnit.SECONDS.toMillis(60),
						TimeUnit.SECONDS.toMillis(120));
	}

	public static BotStarter getInstance() {
		return instance;
	}

	public static void main(String[] args) {
		final Options o = new Options();
		o.addOption("debug", false, "Enables debug log");
		o.addOption("path", true, "Where stores config and cache files");
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(o, args);
			boolean debug = line.hasOption("debug");
			String path = line.getOptionValue("path", "");
			new BotStarter(debug, path);
		} catch (ParseException exp) {
			System.err.println(exp.getMessage());
		}
	}

	public static class CacheSaveThread extends TimerTask {
		private static final boolean debug = getInstance().debug;

		@Override
		public void run() {
			if (debug)
				System.out.println("MAIN Tick");
			if (getInstance().cacheChanged) {
				if (debug)
					System.out.println("Saving changed caches...");
				getInstance().cacheChanged = false;
				getInstance().config.saveCache();
			}
			getInstance().config.loadCache();
		}
	}
}
