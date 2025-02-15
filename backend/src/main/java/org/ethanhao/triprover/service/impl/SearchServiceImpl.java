package org.ethanhao.triprover.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.ethanhao.triprover.domain.index.UserIndex;
import org.ethanhao.triprover.domain.index.PlanIndex;
import org.ethanhao.triprover.dto.user.UserIndexResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanIndexResponseDTO;
import org.ethanhao.triprover.mapper.UserIndexMapper;
import org.ethanhao.triprover.mapper.PlanIndexMapper;
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
    private final PlanIndexMapper planIndexMapper;

    @Autowired
    public SearchServiceImpl(ElasticsearchClient esClient, UserIndexMapper userIndexMapper, PlanIndexMapper planIndexMapper) {
        this.esClient = esClient;
        this.userIndexMapper = userIndexMapper;
        this.planIndexMapper = planIndexMapper;
    }

    @Override
    public List<UserIndexResponseDTO> searchUsers(String query) {
        try {
            SearchResponse<UserIndex> response = esClient.search(s -> s
                .index("sys_user")
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

    @Override
    public List<PlanIndexResponseDTO> searchPlans(String query) {
        try {
            SearchResponse<PlanIndex> response = esClient.search(s -> s
                .index("plans")
                .query(q -> q
                    .bool(b -> b
                        .filter(f -> f.term(t -> t.field("is_public").value(true)))
                        .must(m -> m
                            .multiMatch(mm -> mm
                                .fields("plan_name", "description")
                                .query(query)
                                .fuzziness("AUTO")
                            )
                        )
                    )
                )
                .size(10),
                PlanIndex.class
            );

            return response.hits().hits().stream()
                .map(Hit::source)
                .map(planIndexMapper::toResponseDTO)
                .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("Failed to search plans", e);
        }
    }
} 