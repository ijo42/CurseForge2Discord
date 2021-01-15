FROM bellsoft/liberica-openjdk-alpine:11
LABEL maintainer="admin@ijo42.ru"

ENV CONFIG_PATH="/opt/curse2discord/configs"

WORKDIR /opt/curse2discord
COPY build/libs/CurseForge2Discord.jar /opt/curse2discord
VOLUME $CONFIG_PATH

ENTRYPOINT [ "java", "-jar", "CurseForge2Discord.jar" ]
