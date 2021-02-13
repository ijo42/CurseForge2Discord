FROM gradle:jdk15 AS builder

WORKDIR /tmp/curseforge2discord

COPY src/ build.gradle LICENSE /

RUN \
  gradle


FROM lsiobase/alpine:3.11 as release

LABEL maintainer="ijo42 <admin@ijo42.ru>"

ENV  LANG=en_US.UTF-8 \
     LANGUAGE=en_US:en
#	 LC_ALL=en_US.UTF-8

ARG LIBERICA_IMAGE_VARIANT=base

ARG LIBERICA_JVM_DIR=/usr/lib/jvm
ARG LIBERICA_ROOT=${LIBERICA_JVM_DIR}/jdk-15
ARG LIBERICA_VERSION=15.0.2
ARG LIBERICA_BUILD=10
ARG LIBERICA_VARIANT=jdk
ARG LIBERICA_RELEASE_TAG=""
ARG LIBERICA_ARCH=x64

ARG OPT_MODULES="java.base,java.logging"
ARG OPT_PKGS=""
SHELL ["/bin/ash", "-eo", "pipefail", "-c"]

RUN \
  for pkg in $OPT_PKGS ; do apk --no-cache add $pkg ; done && \
  mkdir -p /tmp/java && \
  LIBERICA_BUILD_STR=${LIBERICA_BUILD:+"+${LIBERICA_BUILD}"} && \
  PKG="bellsoft-${LIBERICA_VARIANT}${LIBERICA_VERSION}${LIBERICA_BUILD_STR}-linux-${LIBERICA_ARCH}-musl.tar.gz" && \
  PKG_URL="https://download.bell-sw.com/java/${LIBERICA_VERSION}${LIBERICA_BUILD_STR}/${PKG}" && \
  echo "Download ${PKG_URL}" && \
  wget "${PKG_URL}" -O /tmp/java/jdk.tar.gz && \
  SHA1=$(wget -q "https://download.bell-sw.com/sha1sum/java/${LIBERICA_VERSION}${LIBERICA_BUILD_STR}" -O - | grep ${PKG} | cut -f1 -d' ') && \
  echo "${SHA1} */tmp/java/jdk.tar.gz" | sha1sum -c - && \
  tar xzf /tmp/java/jdk.tar.gz -C /tmp/java && \
  UNPACKED_ROOT="/tmp/java/${LIBERICA_VARIANT}-${LIBERICA_VERSION}" && \
  case $LIBERICA_IMAGE_VARIANT in \
  base) apk add --no-cache binutils && mkdir -pv "${LIBERICA_JVM_DIR}" && "${UNPACKED_ROOT}/bin/jlink" --add-modules ${OPT_MODULES} --no-header-files --no-man-pages --strip-debug --module-path \
    "${UNPACKED_ROOT}"/jmods --vm=server --output "${LIBERICA_ROOT}" && apk del binutils ;; \
  standard) apk --no-cache add binutils &&  mkdir -pv "${LIBERICA_ROOT}" && find /tmp/java/${LIBERICA_VARIANT}* -maxdepth 1 -mindepth 1 -exec mv -v "{}" "${LIBERICA_ROOT}/" \; ;; \
  *) mkdir -pv "${LIBERICA_ROOT}" && find /tmp/java/${LIBERICA_VARIANT}* -maxdepth 1 -mindepth 1 -exec mv -v "{}" "${LIBERICA_ROOT}/" \; ;; \
  esac && \
  ln -s $LIBERICA_ROOT /usr/lib/jvm/jdk && \
  rm -rf /tmp/java && rm -rf /tmp/hsperfdata_root

ENV JAVA_HOME=${LIBERICA_ROOT} \
	PATH=${LIBERICA_ROOT}/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

RUN \
  mkdir -p \
    /app/curseforge2discord

COPY --from=builder /tmp/curseforge2discord/build/libs/curseforge2discord.jar /app/curseforge2discord/CurseForge2Discord.jar

COPY root/ /

VOLUME /config
