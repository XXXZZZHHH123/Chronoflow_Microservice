# 依赖预热：构建时把依赖与模块装进本地仓库，运行时就不会缺
FROM maven:3.9.6-eclipse-temurin-17 AS deps
WORKDIR /workspace

# 先拷贝各模块 POM，做 go-offline（避免一次性 COPY . 带来缓存失效）
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

# 仅凭 POM 预拉依赖（对 task-service 及其依赖链）
RUN mvn -B -f task-service/pom.xml -am dependency:go-offline

# 再拷贝源码（最大化利用上一步缓存）
COPY . .

# 安装 task-service 及其依赖模块到镜像的 ~/.m2
RUN mvn -B -f task-service/pom.xml -am -DskipTests install

# 运行层：带好了源码与本地仓库
FROM maven:3.9.6-eclipse-temurin-17 AS runner
WORKDIR /workspace

COPY --from=deps /workspace /workspace
COPY --from=deps /root/.m2 /root/.m2
COPY task-service/perf/entrypoint.sh /entrypoint.sh

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
RUN chmod +x /entrypoint.sh

ENV MAVEN_OPTS="-XX:+UseContainerSupport"

ENTRYPOINT ["/entrypoint.sh"]