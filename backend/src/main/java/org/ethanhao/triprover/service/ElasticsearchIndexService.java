package org.ethanhao.triprover.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ElasticsearchIndexService {

    private final ElasticsearchClient esClient;

    @Autowired
    public ElasticsearchIndexService(
            ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    @PostConstruct
    public void initializeIndices() {
        try {
            createUserIndexIfNotExists();
            createPlanIndexIfNotExists();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Elasticsearch indices", e);
        }
    }

    private void createUserIndexIfNotExists() throws IOException {
        boolean exists = esClient.indices().exists(
                ExistsRequest.of(builder -> builder.index("sys_user"))).value();

        if (!exists) {
            esClient.indices().create(builder -> builder
                    .index("sys_user")
                    .mappings(typeMapping -> typeMapping
                            .properties("id", prop -> prop.keyword(keywordProp -> keywordProp))
                            .properties("user_name", prop -> prop.text(textProp -> textProp))
                            .properties("nick_name", prop -> prop.text(textProp -> textProp))
                            .properties("email", prop -> prop.keyword(keywordProp -> keywordProp))
                            .properties("phone_number", prop -> prop.keyword(keywordProp -> keywordProp))
                            .properties("avatar", prop -> prop.keyword(keywordProp -> keywordProp))
                            .properties("type", prop -> prop.keyword(keywordProp -> keywordProp))
                            .properties("status", prop -> prop.keyword(keywordProp -> keywordProp))
                            .properties("del_flag", prop -> prop.keyword(keywordProp -> keywordProp)))
                    .settings(settings -> settings
                            .numberOfShards("1")
                            .numberOfReplicas("1")));
            log.info("Created users index in Elasticsearch");
        } else {
            log.info("Users index already exists in Elasticsearch, skipping creation");
        }
    }

    private void createPlanIndexIfNotExists() throws IOException {
        boolean exists = esClient.indices().exists(
                ExistsRequest.of(builder -> builder.index("plans"))).value();

        if (!exists) {
            esClient.indices().create(builder -> builder
                    .index("plans")
                    .mappings(typeMapping -> typeMapping
                            .properties("plan_id", prop -> prop.keyword(keywordProp -> keywordProp))
                            .properties("plan_name", prop -> prop.text(textProp -> textProp))
                            .properties("description", prop -> prop.text(textProp -> textProp))
                            .properties("is_public", prop -> prop.boolean_(boolProp -> boolProp)))
                    .settings(settings -> settings
                            .numberOfShards("1")
                            .numberOfReplicas("1")));
            log.info("Created plans index in Elasticsearch");
        } else {
            log.info("Plans index already exists in Elasticsearch, skipping creation");
        }
    }
}