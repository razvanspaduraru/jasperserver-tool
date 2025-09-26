package com.tech.connections.jasperservertool.gateway.filters.jasper.request;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;

@Component
public class JasperGetAdminFilter implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if ("/jasperserver/getAdmin".equals(path)) {

            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>(exchange.getRequest().getQueryParams());

            // Add or modify query parameters
            queryParams.put("_flowId", Collections.singletonList("searchFlow"));
            queryParams.put("lastMode", Collections.singletonList("true"));

            // Build a new URI with modified query parameters
            URI originalUri = exchange.getRequest().getURI();
            URI newUri = UriComponentsBuilder.fromUri(originalUri)
                    .replaceQueryParams(queryParams)
                    .replacePath("/jasperserver/flow.html") // rewrite path
                    .build(true)
                    .toUri();

            // Mutate the existing request without losing headers
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .uri(newUri)
                    .build();

            exchange = exchange.mutate().request(mutatedRequest).build();
        }

        return chain.filter(exchange);
    }
}