package org.ethanhao.triprover.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        log.info("Initializing Elasticsearch client for {}:{}", host, port);
        
        // Create the credentials provider
        BasicCredentialsProvider credsProv = new BasicCredentialsProvider();
        credsProv.setCredentials(
            AuthScope.ANY, 
            new UsernamePasswordCredentials(username, password)
        );

        // Create the low-level client
        RestClient restClient = RestClient.builder(
            new HttpHost(host, port, "http"))
            .setHttpClientConfigCallback(httpClientBuilder -> 
                httpClientBuilder.setDefaultCredentialsProvider(credsProv)
            )
            .build();

        // Create the transport and API client
        ElasticsearchTransport transport = new RestClientTransport(
            restClient, 
            new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
} 