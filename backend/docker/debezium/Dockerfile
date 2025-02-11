ARG DEBEZIUM_VERSION=3.0.0.Final
FROM quay.io/debezium/connect:${DEBEZIUM_VERSION}
ENV KAFKA_CONNECT_ES_DIR=$KAFKA_CONNECT_PLUGINS_DIR/kafka-connect-elasticsearch

ARG KAFKA_ELASTICSEARCH_VERSION=14.0.3

USER root

# Install Maven
RUN microdnf install -y maven

# Copy pom.xml
COPY pom.xml /tmp/pom.xml

# Download dependencies using Maven
RUN mvn dependency:copy-dependencies -f /tmp/pom.xml -DoutputDirectory=$KAFKA_CONNECT_ES_DIR \
    && microdnf clean all \
    && rm -rf /tmp/pom.xml ~/.m2

USER kafka
