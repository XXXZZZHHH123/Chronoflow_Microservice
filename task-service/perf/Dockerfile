FROM maven:3.9.6-eclipse-temurin-17 AS deps
WORKDIR /workspace

COPY pom.xml .
COPY task-service/pom.xml task-service/pom.xml
COPY attendee-service/pom.xml attendee-service/pom.xml
COPY event-service/pom.xml event-service/pom.xml
COPY file-service/pom.xml file-service/pom.xml
COPY framework/pom.xml framework/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY common/pom.xml common/pom.xml
COPY shared-api/pom.xml shared-api/pom.xml
COPY wsgateway/pom.xml wsgateway/pom.xml

RUN mvn -B -f pom.xml -pl task-service -am dependency:go-offline -DskipTests

COPY . .

RUN mvn -B -pl task-service -am -DskipTests install

FROM maven:3.9.6-eclipse-temurin-17 AS runner
WORKDIR /workspace

COPY --from=deps /workspace /workspace
COPY --from=deps /root/.m2 /root/.m2
COPY task-service/perf/entrypoint.sh /entrypoint.sh

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN chmod +x /entrypoint.sh

ENV MAVEN_OPTS="-XX:+UseContainerSupport"

ENTRYPOINT ["/entrypoint.sh"]