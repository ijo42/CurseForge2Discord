# CurseForge2Discord

***

## Difference from [ErdbeerbaerLP/Curseforge-Bot](https://github.com/ErdbeerbaerLP/Curseforge-Bot)

My fork based on *Minnced* WebHook Discord API instead JDA.

feats:

* This does not require the creation of a separate bot account.

* A more lightweight library

* Not use github conf storing

* A lit bit of Improvements

* Maintability

* [Support](https://discord.gg/4ZYazbM)

![](https://amity.is-inside.me/tHBV5Xkj.png)

***

## Deployment

#### Docker. **
Preferer** ([Griefed](https://github.com/Griefed)/**[docker-Curseforge-Bot](https://github.com/Griefed/docker-Curseforge-Bot)**) *
using docker-compose:*

```docker-compose.yml
version: "2"
services:
  curseforge-bot:
    image: griefed/curseforge-bot:ijo42-latest
    container_name: curseforge-bot
    restart: unless-stopped
    environment:
      - TZ=Europe/Berlin # Timezone
      - ROLE_ID=000000000 # (Optional) The ID of the discord role mentioned when the bot makes a post
      - PUID=1000 # User ID
      - PROJECT_ID=430517 # The ID of your Curseforge project
      - PGID=1000 # Group ID
      - FILE_LINK=curse # direct-link to file or curseforge-link on project page or nolink.
      - DISCORD_CHANNEL_ID=000000000 # The ID of the channel you want the bot to post in. Must be the Channel ID from your Webhook URL.
      - WEBHOOK_TOKEN=InsertHere # Your discord-server webhook
      - DESCRIPTION=New File(s) Detected For CurseForge Project(s) # This sets the text that appears as the message description in the update notification
      - CHANGELOG_FORMAT=md # yml or md or css. Only choose one syntax. Can be very usefull if project owner/author uses discord MarkDown formatting in their changelog.
    volumes:
      - /host/path/to/config:/config # Where the bot-conf will be stored
```

#### Setup by GitHub Release's

1. Install JDK 15
2. Download [latest binary](https://github.com/ijo42/CurseForge2Discord/releases/latest)
3. (Optional) Redefine config path:

* By argument `--path /opt/curseforge2discord`
* By ENV VAR `set CONFIG_PATH=/opt/curseforge2discord`

4. Start: `java -jar build/libs/CurseForge2Discord.jar`

#### Building and Setup. Gradlew

1. Install JDK 15, git
2. Clone repo: `git clone https://github.com/ijo42/CurseForge2Discord.git .`
3. Build: `gradlew build`
4. (Optional) Redefine config path:

* By argument `--path /opt/curseforge2discord`
* By ENV VAR `set CONFIG_PATH=/opt/curseforge2discord`

5. Start: `java -jar build/libs/CurseForge2Discord.jar`

***

## Configuration

```
# Don´t change this! Used internally to backup and reset the config if needed!
ver = 5

# Provide some Curseforge project IDs you want the bot to listen to
#
# If you want a specific mod in a different channel and also ping a differnt role, you can add the 
# channel id and role id like this:
# "projectId;;channelId;;webhookToken;;RoleID"
# Role ID doesnt have to be set, if its not dont include second ;;
# NOTE: Role ID can only be set if a channel id is set as well
# NOTE 2: channelId must me grabbed from DISCORD WEBHOOK
# e.g. https://discord.com/api/webhooks/xxx/zzzz
# which xxx is channel id, zzzz is webhook token
ids = [
    # Uplink
    "435552;;xxx;;???"
]

## UPDATE MESSAGE OPTIONS ##
# Provide a language syntax name to have the changelog formatted inside the embeded message for easier viewing if desired.
#
# Can be very usefull if project owner/author uses discord MarkDown formatting in their changelog
# Uploads as plain text if not changed (example: yml, md, css) Only choose one syntax
changelogDiscordFormat = "Syntax"

# This sets the text that appears as the message description in the update notification
# (the text directly under the project name which is the message title)
#
# This can adhere to discord markdown rules but due to how the message is formatted as a whole, keep this
# message under 250 characters
messageDescription = "New File Detected For CurseForge Project"

# Sets the message to include a download link for the new project file
#
# Use the following 3 options only to set the link
# "direct" = Direct link to download the file
# "curse" = Link to the file download page on curseforge.com
# "nolink" = Do not include a download link
updateFileLink="curse"

# If you want the message to mention a particualr role when a update message is sent, add the Role ID here
#
# Only supports 1 role ID at this time
mentionRole=000000000

# You may provide custom image by url into Message Footer
footerImage=""
```

## License

Inherited from origin [license](https://github.com/ijo42/CurseForge2Discord/blob/master/LICENSE).
