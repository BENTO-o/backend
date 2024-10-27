# syntax=docker/dockerfile:1.4
FROM --platform=$BUILDPLATFORM gradle:8.10.1-jdk17 AS builder
WORKDIR /workdir/server
COPY build.gradle settings.gradle /workdir/server/
COPY gradle /workdir/server/gradle
COPY gradlew /workdir/server/
RUN chmod +x ./gradlew
RUN ./gradlew dependencies
COPY src /workdir/server/src
RUN ./gradlew build

FROM builder as dev-envs
RUN <<EOF
apt-get update
apt-get install -y git
EOF
RUN <<EOF
useradd -s /bin/bash -m vscode
groupadd docker
usermod -aG docker vscode
EOF
# install Docker tools (cli, buildx, compose)
COPY --from=gloursdocker/docker / /
CMD ["./gradlew", "bootRun"]

FROM eclipse-temurin:17-jre-focal
EXPOSE 8080
VOLUME /tmp
COPY --from=builder /workdir/server/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
