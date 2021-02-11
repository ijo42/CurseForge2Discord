package de.erdbeerbaerlp.curseforge;

import club.minnced.discord.webhook.WebhookClient;
import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CurseForgeUpdateThread extends TimerTask {
	private final CurseProject proj;
	private final WebhookClient webhook;
	private final Starter starter = Starter.getInstance();
	private final EmbedMessage embedMessage;
	private final Config config;
	private final boolean debug = starter.debug;
	private String roleID = "";

	public CurseForgeUpdateThread(String id, EmbedMessage embedMessage, Config config) throws CurseException {
		this.config = config;
		if (!id.contains(";;")) {
			throw new RuntimeException("Missed configuration");
		}
		this.embedMessage = embedMessage;
		String[] ids = id.split(";;");
		this.webhook = WebhookClient.withId(Long.parseLong(ids[ 1 ]), ids[ 2 ]);
		if (ids.length == 4) {
			roleID = ids[ 3 ];
		}
		this.proj = CurseAPI.project(Integer.parseInt(id.split(";;")[ 0 ])).
				orElseThrow(() -> new CurseException("Project not found"));
		final Timer timer = new Timer("CurseForge Update Detector for %s (ID: %d)".formatted(this.proj.name(), this.proj.id()));
		timer.scheduleAtFixedRate(this, TimeUnit.SECONDS.toMillis(60), TimeUnit.SECONDS.toMillis(30));
		starter.threads.add(timer);
	}

	@Override
	public void run() {
		try {
			proj.refreshFiles();
			if (proj.files().isEmpty()) {
				return;
			}
			if (debug) {
				System.out.printf("<%s> Cached: %d Newest:%d%n", proj.name(), starter.cache.get(proj.name()), proj.files().first().id());
			}
			if (config.isNewFile(proj.name(), proj.files().first().id())) {
				this.embedMessage.sendPingableUpdateNotification(roleID, proj, webhook);
				this.starter.cache.put(proj.name(), proj.files().first().id());
				this.starter.cacheChanged = true;
			}
		} catch (CurseException exception) {
			exception.printStackTrace();
		}
	}
}
