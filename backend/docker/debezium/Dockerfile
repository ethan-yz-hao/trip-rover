ARG DEBEZIUM_VERSION=3.0.0.Final
FROM quay.io/debezium/connect:${DEBEZIUM_VERSION}
ENV KAFKA_CONNECT_ES_DIR=$KAFKA_CONNECT_PLUGINS_DIR/kafka-connect-elasticsearch

ARG KAFKA_ELASTICSEARCH_VERSION=14.0.3

USER root

# Install Maven
RUN microdnf install -y maven

# Create a pom.xml file
RUN cat > /tmp/pom.xml <<'EOF'
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>kafka-connect-deps</artifactId>
    <version>1.0-SNAPSHOT</version>
    <repositories>
        <repository>
            <id>confluent</id>
            <url>https://packages.confluent.io/maven/</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>io.confluent</groupId>
            <artifactId>kafka-connect-elasticsearch</artifactId>
            <version>14.0.3</version>
        </dependency>
    </dependencies>
</project>
EOF

RUN mvn dependency:copy-dependencies -f /tmp/pom.xml -DoutputDirectory=$KAFKA_CONNECT_ES_DIR

USER kafka
