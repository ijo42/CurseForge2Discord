package de.erdbeerbaerlp.curseforgeBot;

import java.util.concurrent.TimeUnit;

public class ChacheSaveThread extends Thread {
	@Override
	public void run() {
		try {
			Thread.sleep(TimeUnit.SECONDS.toMillis(90));
			System.out.println("MAIN Tick");
			System.out.println(Main.threads);
			if (Main.cacheChanged) {
				System.out.println("Saving changed caches...");
				Main.cacheChanged = false;
				Main.cfg.saveCache();
			}
			Main.cfg.loadCache();
		} catch (InterruptedException e) {
			System.out.println("Main Thread interrupted!");
		}
	}
}
