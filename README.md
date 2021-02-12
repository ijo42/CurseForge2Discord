# CurseForge2Discord

***

## Difference from [ErdbeerbaerLP/Curseforge-Bot](https://github.com/ErdbeerbaerLP/Curseforge-Bot)

Fork based on *Minnced* WebHook Discord API instead JDA. Personal thanks [Griefed](https://github.com/Griefed) for a
bunch of useful content

feats:

* This does not require the creation of a separate bot account.
* A more lightweight library
* Not use GitHub conf storing
* A lit bit of Improvements
* Maintability
* [Support](https://discord.gg/4ZYazbM)
* Built-in S6 Overlay

![](https://amity.is-inside.me/tHBV5Xkj.png)

---

The [lsiobase/alpine](https://hub.docker.com/r/lsiobase/alpine) image is a custom base image built
with [Alpine linux](https://alpinelinux.org/) and [S6 overlay](https://github.com/just-containers/s6-overlay). Using
this image allows us to use the same user/group ids in the container as on the host, making file transfers much easier

## Image-Variants

|                Tags | Description                                                  |
| ------------------: | ------------------------------------------------------------ |
|            `latest` | Using the `latest` tag will pull the latest release image for amd64/x86_64 architecture. |
|            `master` | Using the `latest` tag will pull latest master-branch image for amd64/x86_64 architecture. |
|               `dev` | Using the `latest` tag will pull latest dev image for amd64/x86_64 architecture. |
| `sha-([a-z0-9]{7})` | Using this tag will push image based on relevant git-commit `(sha-fbc170f)` |
|        `\d\.\d\.\d` | Using this tag will push relevant Release `(2.0.0)`          |

## Pre-built images Images

using docker-compose:

```docker-compose.yml
version: "2"
services:
  cf2d:
    image: ijo42/curseforge2discord:latest
    container_name: cf2d
    restart: unless-stopped
    environment:
      - TZ=Europe/Berlin # Timezone
      - ROLE_ID=000000000 # (Optional) The ID of the discord role mentioned when the bot makes a post
      - PUID=1000 # User ID
      - PROJECT_ID=435552 # The ID of your Curseforge project
      - PGID=1000 # Group ID
      - FOOTER_URL=https://avatars.githubusercontent.com/u/53531892 # Footer Image
      - FILE_LINK=curse # direct-link to file or curseforge-link on project page or nolink.
      - DISCORD_CHANNEL_ID=000000000 # The ID of the channel you want the bot to post in
      - WEBHOOK_TOKEN=InsertHere # Your discord-server webhook
      - DESCRIPTION=New File(s) Detected For CurseForge Project(s) # This sets the text that appears as the message description in the update notification
      - CHANGELOG_FORMAT=md # yml or md or css. Only choose one syntax. Can be very usefull if project owner/author uses discord MarkDown formatting in their changelog.
    volumes:
      - ./host/path/to/config:/config # Where the bot-conf will be stored
```

Using CLI:

```bash
docker create \
  --name=cf2d \
  -e TZ=Europe/Berlin `# Timezone` \
  -e ROLE_ID=000000000 `# (Optional) The ID of the discord role mentioned when the bot makes a post` \
  -e PUID=1000 `# User ID` \
  -e PROJECT_ID=435552 `# The ID of your Curseforge project` \
  -e PGID=1000 `# Group ID` \
  -e FILE_LINK=curse `# direct-link to file or curseforge-link on project page or nolink.` \
  -e DISCORD_CHANNEL_ID=000000000 `# The ID of the channel you want the bot to post in` \
  -e FOOTER_URL=https://avatars.githubusercontent.com/u/53531892 `Footer Image` \
  -e WEBHOOK_TOKEN=InsertHere `# Your discord-server webhook` \
  -e DESCRIPTION=New File(s) Detected For CurseForge Project(s) `# This sets the text that appears as the message description in the update notification` \
  -e CHANGELOG_FORMAT=md `# yml or md or css. Only choose one syntax. Can be very usefull if project owner/author uses discord MarkDown formatting in their changelog.` \
  -v ./host/path/to/config:/config `# Where the bot-conf will be stored` \
  --restart unless-stopped \
  ijo42/curseforge2discord:latest
```

## Build yourself

### Image

Use the [Dockerfile](https://github.com/ijo42/CurseForge2Discord/blob/master/Dockerfile) to build the image yourself, in
case you want to make any changes to it

docker-compose.yml:

```docker-compose.yml
version: '2'
services:
  cf2d:
    container_name: cf2d
    build: ./cf2d
    restart: unless-stopped
    volumes:
      - ./path/to/config:/config
    environment:
      - TZ=Europe/Berlin # Timezone
      - ROLE_ID=000000000
      - PUID=1000 # User ID
      - FOOTER_URL=
      - PROJECT_ID=
      - PGID=1000 # Group ID
      - FILE_LINK=
      - DISCORD_CHANNEL_ID=
      - WEBHOOK_TOKEN=
      - DESCRIPTION=
      - CHANGELOG_FORMAT=
```

1. Clone the repository: `git clone -b ijo42 https://github.com/Griefed/docker-Curseforge-Bot.git ./cf2d`
1. Prepare `docker-compose.yml` file as seen above
1. `docker-compose up -d --build cf2d`
1. ???
1. Profit!

### Setup by GitHub Release's

1. Install JDK 15
2. Download [latest binary](https://github.com/ijo42/CurseForge2Discord/releases/latest)
3. (Optional) Redefine config path:

* By argument `--path /opt/curseforge2discord`
* By ENV VAR `set CONFIG_PATH=/opt/curseforge2discord`

4. Start: `java -jar build/libs/CurseForge2Discord.jar`

### Building and Setup. Gradlew

1. Install JDK 15, git
2. Clone repo: `git clone https://github.com/ijo42/CurseForge2Discord.git .`
3. Build: `gradlew build`
4. (Optional) Redefine config path:

* By argument `--path /opt/curseforge2discord`
* By ENV VAR `set CONFIG_PATH=/opt/curseforge2discord`

5. Start: `java -jar build/libs/CurseForge2Discord.jar`

***

## Adding more projects to track

If you have multiple projects in CurseForge which you want to track with this bot, you need to manually edit
the `bot.conf` file which is created after container creation. Here's an example for multiple project IDs and how it's
formatted:

```
ids = [
    "430517;;DISCORD_CHANNEL_ID;;WEBHOOK_TOKEN",
    "239197;;DISCORD_CHANNEL_ID;;WEBHOOK_TOKEN",
    "243121;;DISCORD_CHANNEL_ID;;WEBHOOK_TOKEN"
]
```

## User / Group Identifiers

When using volumes, permissions issues can arise between the host OS and the
container. [Linuxserver.io](https://www.linuxserver.io/) avoids this issue by allowing you to specify the user `PUID`
and group `PGID`.

Ensure any volume directories on the host are owned by the same user you specify and any permissions issues will vanish
like magic.

In this instance `PUID=1000` and `PGID=1000`, to find yours use `id user` as below:

```
  $ id username
    uid=1000(dockeruser) gid=1000(dockergroup) groups=1000(dockergroup)
```

## Configuration

Configuration | Explanation
------------ | -------------
[Restart policy](https://docs.docker.com/compose/compose-file/#restart) | "no", always, on-failure, unless-stopped
config volume | Contains config files and logs.
data volume | Contains your/the containers important data.
TZ | Timezone
PUID | for UserID
PGID | for GroupID
FOOTER_URL | URL to image in Footer of anounce
DISCORD_CHANNEL_ID | The ID of the channel you want the bot to post in (from webhook)
PROJECT_ID | The ID of your CurseForge project
ROLE_ID | (Optional) The ID of the discord role mentioned when the bot makes a post
FILE_LINK | `direct`-link to file or `curse`forge-link on project page or `nolink`.
DESCRIPTION | This sets the text that appears as the message description in the update notification
CHANGELOG_FORMAT | `yml` or `md` or `css`. Only choose one syntax. Can be very usefull if project owner/author uses discord MarkDown formatting in their changelog.
WEBHOOK_TOKEN | Your discord-server webhook token

## License

Inherited from origin [license](https://github.com/ijo42/CurseForge2Discord/blob/master/LICENSE).
