package de.erdbeerbaerlp.curseforge;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.therandomlabs.curseapi.CurseException;
import com.therandomlabs.curseapi.file.CurseFile;
import com.therandomlabs.curseapi.file.CurseReleaseType;
import com.therandomlabs.curseapi.project.CurseProject;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class EmbedMessage {

	private final static int// Got from CurseForge
			release = 0x14b866, // new Color(20, 184, 102).getRGB()
			beta = 0xe9bd8,     // new Color(14, 155, 216).getRGB()
			alpha = 0xd3cae8;   // new Color(211, 202, 232).getRGB()

	private final Config config;
	private final String syntax;

	public EmbedMessage(Config config) {
		this.config = config;
		this.syntax = getSyntax(this.config.changelogDiscordFormat);
	}

	private static int getColorFromReleaseType(CurseReleaseType releaseType) {
		return switch (releaseType) {
			case RELEASE -> release;
			case BETA -> beta;
			case ALPHA -> alpha;
		};
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
		return String.join(", ", proj.files().first().gameVersionStrings());
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
		return "%s/files/%d".formatted(urlPre, id);
	}

	/**
	 * Message without link.
	 *
	 * @param proj the proj
	 * @param file the file
	 * @throws CurseException the curse exception
	 */
	public void messageWithoutLink(CurseProject proj, CurseFile file, WebhookClient webhook)
			throws CurseException {
		final WebhookEmbed embed = new WebhookEmbedBuilder().
				setAuthor(new WebhookEmbed.EmbedAuthor(proj.name(), null, proj.url().toString())).
				setColor(getColorFromReleaseType(file.releaseType())).
				setThumbnailUrl(proj.logo().thumbnailURL().toString()).
				setFooter(new WebhookEmbed.EmbedFooter("Update now!", this.config.footerImage)).
				addField(new WebhookEmbed.EmbedField(false, "Build",
						("""
								**Release Type**: `%s`
								 **File Name**: `%s`
								 **Category**: `%s`
								 **GameVersion**: `%s`""").formatted(file.releaseType().name(), file.displayName(), proj.categorySection().name(), getGameVersions(proj)))).
				addField(new WebhookEmbed.EmbedField(false, "Changelog:",
						("""
								```%s
								%s
								```""").formatted(syntax, formatChangelog(file.changelogPlainText(1000))))).
				build();
		final @NotNull WebhookMessage message = new WebhookMessageBuilder().
				setContent(getMessageDescription()).
				setUsername("Update Detector").
				addEmbeds(embed).
				build();
		webhook.send(message);
	}

	/**
	 * Message with curse link.
	 *
	 * @param proj the proj
	 * @param file the file
	 * @throws CurseException the curse exception
	 */
	public void messageWithCurseLink(CurseProject proj, CurseFile file, WebhookClient webhook)
			throws CurseException {
		final WebhookEmbed embed = new WebhookEmbedBuilder().
				setAuthor(new WebhookEmbed.EmbedAuthor(proj.name(), null, proj.url().toString())).
				setColor(getColorFromReleaseType(file.releaseType())).
				setThumbnailUrl(proj.logo().thumbnailURL().toString()).
				setFooter(new WebhookEmbed.EmbedFooter("Update now!", this.config.footerImage)).
				addField(new WebhookEmbed.EmbedField(false, "Build",
						("""
								                        **Release Type**: `%s`
								**File Name**: `%s`
								**Category**: `%s`
								**GameVersion**: `%s`
								**Website Link**: [CurseForge](%s)""").formatted(file.releaseType().name(), file.displayName(), proj.categorySection().name(), getGameVersions(proj), getUrl(proj)))).
				addField(new WebhookEmbed.EmbedField(false, "Changelog:",
						("""
								```%s
								%s
								```""").formatted(syntax, formatChangelog(file.changelogPlainText(1000))))).
				build();
		final @NotNull WebhookMessage message = new WebhookMessageBuilder().
				setContent(getMessageDescription()).
				setUsername("Update Detector").
				addEmbeds(embed).
				build();
		webhook.send(message);
	}

	/**
	 * Message with direct link.
	 *
	 * @param proj the proj
	 * @param file the file
	 * @throws CurseException the curse exception
	 */
	public void messageWithDirectLink(CurseProject proj, CurseFile file, WebhookClient webhook)
			throws CurseException {
		final WebhookEmbed embed = new WebhookEmbedBuilder().
				setAuthor(new WebhookEmbed.EmbedAuthor(proj.name(), null, proj.url().toString())).
				setColor(getColorFromReleaseType(file.releaseType())).
				setThumbnailUrl(proj.logo().thumbnailURL().toString()).
				setFooter(new WebhookEmbed.EmbedFooter("Update now!", this.config.footerImage)).
				addField(new WebhookEmbed.EmbedField(false, "Build",
						("""
								**Release Type**: `%s`
								 **File Name**: `%s`
								 **Category**: `%s`
								 **GameVersion**: `%s`
								 **Download Link**: [Download](%s)""").formatted(file.releaseType().name(), file.displayName(), proj.categorySection().name(), getGameVersions(proj), file.downloadURL()))).
				addField(new WebhookEmbed.EmbedField(false, "Changelog:",
						("""
								```%s
								%s
								```""").formatted(syntax, formatChangelog(file.changelogPlainText(1000))))).
				build();
		final @NotNull WebhookMessage message = new WebhookMessageBuilder().
				setContent(getMessageDescription()).
				setUsername("Update Detector").
				addEmbeds(embed).
				build();
		webhook.send(message);
	}

	/**
	 * Send pingable update notification.
	 *
	 * @param role    the role
	 * @param proj    the proj
	 * @param webhook webhook to send
	 * @throws CurseException the curse exception
	 */
	public void sendPingableUpdateNotification(String role, CurseProject proj, WebhookClient webhook)
			throws CurseException {
		if (!role.isEmpty())
			webhook.send("<@&%s>".formatted(role));
		sendUpdateNotification(proj, webhook);
	}

	/**
	 * Send update notification.
	 *
	 * @param proj    the proj
	 * @param webhook webhook to send
	 * @throws CurseException the curse exception
	 */
	public void sendUpdateNotification(CurseProject proj, WebhookClient webhook) throws CurseException {
		switch (this.config.updateFileLink) {
			case NO_LINK -> messageWithoutLink(proj, proj.files().first(), webhook);
			case CURSE -> messageWithCurseLink(proj, proj.files().first(), webhook);
			case DIRECT -> messageWithDirectLink(proj, proj.files().first(), webhook);
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
