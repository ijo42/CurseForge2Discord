package de.erdbeerbaerlp.curseforgeBot;

import java.util.TimerTask;

public class CacheSaveThread extends TimerTask {
	@Override
	public void run() {
		if(Main.debug)
			System.out.println("MAIN Tick");
		if (Main.cacheChanged) {
			if(Main.debug)
				System.out.println("Saving changed caches...");
			Main.cacheChanged = false;
			Main.cfg.saveCache();
		}
		Main.cfg.loadCache();
	}
}
