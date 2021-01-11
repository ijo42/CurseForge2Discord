package de.erdbeerbaerlp.curseforgeBot;

import com.therandomlabs.curseapi.CurseAPI;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.project.CurseProject;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CurseforgeUpdateThread extends Thread {
	private final CurseProject proj;
	private final DiscordWebhook webhook;
	private String roleID = "";

	CurseforgeUpdateThread(String id) throws CurseException {
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
		setName("Curseforge Update Detector for " + proj.name() + " (ID: " + proj.id() + ")");
		Main.threads.add(this);
	}

	@Override
	public void run() {
		try {
			proj.refreshFiles();
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
