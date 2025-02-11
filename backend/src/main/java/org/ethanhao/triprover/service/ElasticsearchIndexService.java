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
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Elasticsearch indices", e);
        }
    }

    private void createUserIndexIfNotExists() throws IOException {
        boolean exists = esClient.indices().exists(
            ExistsRequest.of(builder -> builder.index("users"))
        ).value();

        if (!exists) {
            esClient.indices().create(builder -> builder
                .index("users")
                .mappings(typeMapping -> typeMapping
                    .properties("id", prop -> prop.keyword(keywordProp -> keywordProp))
                    .properties("user_name", prop -> prop.text(textProp -> textProp))
                    .properties("nick_name", prop -> prop.text(textProp -> textProp))
                    .properties("type", prop -> prop.keyword(keywordProp -> keywordProp))
                    .properties("status", prop -> prop.keyword(keywordProp -> keywordProp))
                    .properties("del_flag", prop -> prop.keyword(keywordProp -> keywordProp)))
                .settings(settings -> settings
                    .numberOfShards("1")
                    .numberOfReplicas("1"))

            );
            log.info("Created Elasticsearch index");
        } else {
            log.info("Elasticsearch index already exists, skipping creation");
        }
    }
} 