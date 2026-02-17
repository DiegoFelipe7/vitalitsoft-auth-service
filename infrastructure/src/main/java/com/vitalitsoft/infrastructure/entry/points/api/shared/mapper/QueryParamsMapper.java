package com.vitalitsoft.infrastructure.entry.points.api.shared.mapper;

import co.com.nexus.model.shared.pagination.QueryParams;
import org.springframework.web.reactive.function.server.ServerRequest;

public final class QueryParamsMapper {
    private QueryParamsMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static QueryParams mapToQueryParams(ServerRequest request) {
        return QueryParams.builder()
                .page(request.queryParam("page").map(Integer::parseInt).orElse(0))
                .size(request.queryParam("size").map(Integer::parseInt).orElse(10))
                .sortField(request.queryParam("sort").orElse("createdAt"))
                .build();

    }
}
