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
	private String roleID = "";

	CurseForgeUpdateThread(String id) throws CurseException {
		if (!id.contains(";;"))
			throw new RuntimeException("Missed configuration");
		String[] ids = id.split(";;");
		webhook = new DiscordWebhook(ids[ 1 ], ids [ 2 ]);
		if (ids.length == 4) {
			roleID = ids[ 3 ];
		}
		final Optional<CurseProject> project = CurseAPI.project(Integer.parseInt(id.split(";;")[ 0 ]));
		if (!project.isPresent()) throw new CurseException("Project not found");
		proj = project.get();
		final Timer timer = new Timer("Curseforge Update Detector for " + proj.name() + " (ID: " + proj.id() + ")");
		timer.scheduleAtFixedRate(this, TimeUnit.SECONDS.toMillis(60),TimeUnit.SECONDS.toMillis(30));
		Main.threads.add(timer);
	}

	@Override
	public void run() {
		try {
			proj.refreshFiles();
			if(proj.files().isEmpty())
				return;
			if(Main.debug)
				System.out.println("<" + proj.name() + "> Cached: " + Main.cache.get(proj.name()) + " Newest:" + proj.files().first().id());
			if (Main.cfg.isNewFile(proj.name(), proj.files().first().id())) {
				EmbedMessage.sendPingableUpdateNotification(roleID, proj, webhook);
				Main.cache.put(proj.name(), proj.files().first().id());
				Main.cacheChanged = true;
			}
		} catch (CurseException exception) {
			exception.printStackTrace();
		}
	}
}
