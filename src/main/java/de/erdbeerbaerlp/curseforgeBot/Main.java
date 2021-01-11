package de.erdbeerbaerlp.curseforgeBot;


import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import org.apache.commons.cli.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {
	public static final Cfg cfg = new Cfg();
	static final Map<String, Integer> cache = new HashMap<>();
	static final int CFG_VERSION = 4;
	static List<Timer> threads = new ArrayList<>();
	static boolean cacheGenerated = Cfg.cacheFile.exists();
	static boolean debug = false;
	static boolean cacheChanged;

	public static void main(String[] args) {
		final Options o = new Options();
		o.addOption("debug", false, "Enables debug log");
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(o, args);
			debug = line.hasOption("debug");

			if (!cacheGenerated) {
				System.out.println("Generating cache...");
				for (String p : cfg.IDs) {
					try {
						CurseAPI.project(Integer.parseInt(p.split(";;")[ 0 ])).
								ifPresent(Main::cacheAccept);
					} catch (CurseException e) {
						e.printStackTrace();
					}
				}
				cfg.saveCache();
				System.out.println("Done!");
			} else cfg.loadCache();
			for (String p : cfg.IDs)
				try {
					new CurseForgeUpdateThread(p).run();
				} catch (CurseException e) {
					e.printStackTrace();
				}

			new Timer(CacheSaveThread.class.getName()).
					scheduleAtFixedRate(new CacheSaveThread(), TimeUnit.SECONDS.toMillis(60),
							TimeUnit.SECONDS.toMillis(120));
		} catch (ParseException exp) {
			System.err.println(exp.getMessage());
		}
	}

	private static void cacheAccept(CurseProject pr){
		try {
			cache.put(pr.name(), pr.files().first().id());
		} catch (CurseException e) {
			e.printStackTrace();
		}
	}
}
