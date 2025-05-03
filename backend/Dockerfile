FROM openjdk:11-jdk-slim

MAINTAINER bison

ARG JAR_FILE

ADD ${JAR_FILE} /server.jar

EXPOSE 18888

ENV JAVA_OPTS=""

CMD ["bash", "-c", "java $JAVA_OPTS -XX:+ExitOnOutOfMemoryError -XX:+UnlockExperimentalVMOptions -XX:+UseContainerSupport -Duser.timezone=UTC -jar server.jar"]