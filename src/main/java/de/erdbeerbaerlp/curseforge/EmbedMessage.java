package de.erdbeerbaerlp.curseforge;

import com.mrpowergamerbr.temmiewebhook.DiscordEmbed;
import com.mrpowergamerbr.temmiewebhook.DiscordMessage;
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook;
import com.mrpowergamerbr.temmiewebhook.embed.AuthorEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.FieldEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.FooterEmbed;
import com.mrpowergamerbr.temmiewebhook.embed.ThumbnailEmbed;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseReleaseType;
import com.therandomlabs.curseapi.project.CurseProject;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EmbedMessage {

	private final static Color
			release = new Color(20, 184, 102),
			beta = new Color(14, 155, 216),
			alpha = new Color(211, 202, 232);
	private final Config config;
	private final String syntax;

	public EmbedMessage(Config config) {
		this.config = config;
		this.syntax = getSyntax(this.config.changelogDiscordFormat);
	}

	private static Color getColorFromReleaseType(CurseReleaseType releaseType) {
		switch (releaseType) {
			case RELEASE: // Got from CurseForge
				return release;
			case BETA:
				return beta;
			case ALPHA:
				return alpha;
			default:
				throw new IllegalStateException("Unexpected value: " + releaseType);
		}
	}

	/**
	 * Format changelog.
	 *
	 * @param s the s
	 * @return the string
	 */
	private static String formatChangelog(String s) {
		final String proceed =
				Pattern.compile("^[\\W\\n]+$|( {2,})|https.*?\\s", Pattern.MULTILINE).matcher(s).
						replaceAll("").
						replaceAll("\n+", "\n");
		final StringBuilder out = new StringBuilder();
		final AtomicBoolean isLocked = new AtomicBoolean(false);
		Arrays.stream(proceed.split("\n")).mapToInt(x -> {
			if ((out + x + '\n').length() > 950 || isLocked.get()) {
				isLocked.set(true);
				return 1;
			} else
				out.append(x).append('\n');
			return 0;
		}).reduce(Integer::sum).ifPresent(
				additionalLines -> {
					if (additionalLines > 0)
						out.append("... And ").append(additionalLines).append(" more lines");
				});

		return out.toString();
	}

	/**
	 * Gets the game versions.
	 *
	 * @param proj the proj
	 * @return the game versions
	 * @throws CurseException the curse exception
	 */
	private static String getGameVersions(final CurseProject proj) throws CurseException {
		if (proj.files().first().gameVersionStrings().isEmpty())
			return "UNKNOWN";
		return proj.files().first().gameVersionStrings().stream().sorted().
				collect(Collectors.joining(", "));
	}

	/**
	 * returns the discord markdown syntax set in bot.conf this method does not
	 * throw an error if syntax is not supported or if multiple syntax's are
	 * specified.
	 * <p>
	 * Non-supported syntax auto default to plain text in discord
	 *
	 * @return discord code syntax
	 */
	private static String getSyntax(String md) {
		return md.equals("Syntax") ? "\n" : md + "\n";
	}

	/**
	 * Return the newest file curseforge page url to embed into message.
	 *
	 * @param proj the proj
	 * @return url link to file page
	 * @throws CurseException the curse exception
	 */
	private static String getUrl(final CurseProject proj) throws CurseException {
		String urlPre = proj.url().toString();
		int id = proj.files().first().id();
		return urlPre + "/files/" + id;
	}

	/**
	 * Message without link.
	 *
	 * @param proj the proj
	 * @param file the file
	 * @throws CurseException the curse exception
	 */
	public void messageWithoutLink(CurseProject proj, CurseFile file, TemmieWebhook webhook)
			throws CurseException {
		final DiscordEmbed embed = DiscordEmbed.builder().
				author(new AuthorEmbed(proj.name(), proj.url().toString(), null, null)).
				color(getColorFromReleaseType(file.releaseType()).getRGB()).
				thumbnail(new ThumbnailEmbed(proj.logo().thumbnailURL().toString(), null, 80, 80)).
				footer(new FooterEmbed("Update now!", this.config.footerImage, null)).
				field(new FieldEmbed("Build",
						"**Release Type**: `" + file.releaseType().name() + "`" + "\n **File Name**: `" + file.displayName()
								+ "`" + "\n **Category**: `" + proj.categorySection().name() + "`" + "\n **GameVersion**: `"
								+ getGameVersions(proj) + "`", false)).
				field(new FieldEmbed("Changelog:",
						"```" + syntax + "\n" + formatChangelog(file.changelogPlainText(1000)) + "\n```",
						false)).build();
		final DiscordMessage build = DiscordMessage.builder().
				content(getMessageDescription()).
				username("Update Detector").
				embed(embed).
				build();
		webhook.sendMessage(build);
	}

	/**
	 * Message with curse link.
	 *
	 * @param proj the proj
	 * @param file the file
	 * @throws CurseException the curse exception
	 */
	public void messageWithCurseLink(CurseProject proj, CurseFile file, TemmieWebhook webhook)
			throws CurseException {
		final DiscordEmbed embed = DiscordEmbed.builder().
				author(new AuthorEmbed(proj.name(), proj.url().toString(), null, null)).
				color(getColorFromReleaseType(file.releaseType()).getRGB()).
				thumbnail(new ThumbnailEmbed(proj.logo().thumbnailURL().toString(), null, 80, 80)).
				footer(new FooterEmbed("Update now!", this.config.footerImage, null)).
				field(new FieldEmbed("Build",
						"**Release Type**: `" + file.releaseType().name() + "`" + "\n **File Name**: `" + file.displayName()
								+ "`" + "\n **Category**: `" + proj.categorySection().name() + "`" + "\n **GameVersion**: `"
								+ getGameVersions(proj) + "`" + "\n **Website Link**: " + "[CurseForge](" + getUrl(proj) + ")",
						false)).
				field(new FieldEmbed("Changelog:",
						"```" + syntax + "\n" + formatChangelog(file.changelogPlainText(1000)) + "\n```",
						false)).
				build();
		final DiscordMessage build = DiscordMessage.builder().
				content(getMessageDescription()).
				username("Update Detector").
				embed(embed).
				build();
		webhook.sendMessage(build);
	}

	/**
	 * Message with direct link.
	 *
	 * @param proj the proj
	 * @param file the file
	 * @throws CurseException the curse exception
	 */
	public void messageWithDirectLink(CurseProject proj, CurseFile file, TemmieWebhook webhook)
			throws CurseException {
		final DiscordEmbed embed = DiscordEmbed.builder().
				author(new AuthorEmbed(proj.name(), proj.url().toString(), null, null)).
				color(getColorFromReleaseType(file.releaseType()).getRGB()).
				thumbnail(new ThumbnailEmbed(proj.logo().thumbnailURL().toString(), null, 80, 80)).
				footer(new FooterEmbed("Update now!", this.config.footerImage, null)).
				field(new FieldEmbed("Build",
						"**Release Type**: `" + file.releaseType().name() + "`" + "\n **File Name**: `" + file.displayName()
								+ "`" + "\n **Category**: `" + proj.categorySection().name() + "`" + "\n **GameVersion**: `"
								+ getGameVersions(proj) + "`" + "\n **Download Link**: " + "[Download](" + file.downloadURL()
								+ ")", false)).
				field(new FieldEmbed("Changelog:",
						"```" + syntax + "\n" + formatChangelog(file.changelogPlainText(1000)) + "\n```",
						false)).build();
		final DiscordMessage build = DiscordMessage.builder().
				content(getMessageDescription()).
				username("Update Detector").
				embed(embed).
				build();
		webhook.sendMessage(build);
	}

	/**
	 * Send pingable update notification.
	 *
	 * @param role    the role
	 * @param proj    the proj
	 * @param webhook webhook to send
	 * @throws CurseException the curse exception
	 */
	public void sendPingableUpdateNotification(String role, CurseProject proj, TemmieWebhook webhook)
			throws CurseException {
		if (!role.isEmpty())
			webhook.sendMessage(DiscordMessage.builder().content(String.format("<@&%s>", role)).build());
		sendUpdateNotification(proj, webhook);
	}

	/**
	 * Send update notification.
	 *
	 * @param proj    the proj
	 * @param webhook webhook to send
	 * @throws CurseException the curse exception
	 */
	public void sendUpdateNotification(CurseProject proj, TemmieWebhook webhook) throws CurseException {
		switch (this.config.updateFileLink) {
			case NO_LINK:
				messageWithoutLink(proj, proj.files().first(), webhook);
				break;
			case CURSE:
				messageWithCurseLink(proj, proj.files().first(), webhook);
				break;
			case DIRECT:
				messageWithDirectLink(proj, proj.files().first(), webhook);
				break;
		}
	}

	/**
	 * Returns the custom message description set in bot.conf Description will be
	 * set to default description if over 500 characters
	 *
	 * @return description
	 */
	private String getMessageDescription() {
		String desc = this.config.messageDescription;
		if (desc.length() > 500) {
			System.out.println(
					"Your messageDescription is over 500 characters, setting to default value **PLEASE CHANGE THIS**");
			return "New File detected For CurseForge Project";
		} else {
			return desc;
		}
	}

	enum UpdateFileLinkMode {
		NO_LINK, CURSE, DIRECT
	}
}
