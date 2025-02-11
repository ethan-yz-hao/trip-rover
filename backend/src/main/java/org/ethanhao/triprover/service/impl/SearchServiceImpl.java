package org.ethanhao.triprover.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.ethanhao.triprover.domain.index.UserIndex;
import org.ethanhao.triprover.dto.user.UserIndexResponseDTO;
import org.ethanhao.triprover.mapper.UserIndexMapper;
import org.ethanhao.triprover.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

@Service
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient esClient;
    private final UserIndexMapper userIndexMapper;

    @Autowired
    public SearchServiceImpl(ElasticsearchClient esClient, UserIndexMapper userIndexMapper) {
        this.esClient = esClient;
        this.userIndexMapper = userIndexMapper;
    }

    @Override
    public List<UserIndexResponseDTO> searchUsers(String query) {
        try {
            SearchResponse<UserIndex> response = esClient.search(s -> s
                .index("users")
                .query(q -> q
                    .bool(b -> b
                        .filter(f -> f.term(t -> t.field("type").value(0)))
                        .filter(f -> f.term(t -> t.field("status").value(0)))
                        .filter(f -> f.term(t -> t.field("del_flag").value(0)))
                        .must(m -> m
                            .multiMatch(mm -> mm
                                .fields("user_name", "nick_name")

                                .query(query)
                                .fuzziness("AUTO")
                            )

                        )
                    )
                )
                .size(10),
                UserIndex.class
            );

            return response.hits().hits().stream()
                .map(Hit::source)
                .map(userIndexMapper::toResponseDTO)
                .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Failed to search users", e);
        }
    }
} 