package de.erdbeerbaerlp.curseforgeBot;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import net.ranktw.DiscordWebHooks.DiscordWebhook;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CurseForgeUpdateThread extends TimerTask {
	private final CurseProject proj;
	private final DiscordWebhook webhook;
	private final BotStarter starter = BotStarter.getInstance();
	private final EmbedMessage embedMessage;
	private final Config config;
	private final boolean debug = starter.debug;
	private String roleID = "";

	public CurseForgeUpdateThread(String id, EmbedMessage embedMessage, Config config) throws CurseException {
		this.config = config;
		if (!id.contains(";;"))
			throw new RuntimeException("Missed configuration");
		this.embedMessage = embedMessage;
		String[] ids = id.split(";;");
		webhook = new DiscordWebhook(ids[ 1 ], ids[ 2 ]);
		if (ids.length == 4)
			roleID = ids[ 3 ];

		final Optional<CurseProject> project = CurseAPI.project(Integer.parseInt(id.split(";;")[ 0 ]));
		if (!project.isPresent()) throw new CurseException("Project not found");
		this.proj = project.get();
		final Timer timer = new Timer("Curseforge Update Detector for " + proj.name() + " (ID: " + proj.id() + ")");
		timer.scheduleAtFixedRate(this, TimeUnit.SECONDS.toMillis(60), TimeUnit.SECONDS.toMillis(30));
		starter.threads.add(timer);
	}

	@Override
	public void run() {
		try {
			proj.refreshFiles();
			if (proj.files().isEmpty())
				return;
			if (debug)
				System.out.println("<" + proj.name() + "> Cached: " + starter.cache.get(proj.name()) + " Newest:" + proj.files().first().id());
			if (config.isNewFile(proj.name(), proj.files().first().id())) {
				embedMessage.sendPingableUpdateNotification(roleID, proj, webhook);
				starter.cache.put(proj.name(), proj.files().first().id());
				starter.cacheChanged = true;
			}
		} catch (CurseException exception) {
			exception.printStackTrace();
		}
	}
}
