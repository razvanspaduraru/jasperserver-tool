package com.projects.test.jasperservertool.gateway.filters.jasper.request;

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

@Component
public class JasperGetReportByName implements GatewayFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Example: /jasperserver/getByName/salesReport
        String path = request.getURI().getPath();
        String[] segments = path.split("/");

        StringBuilder folderBuilder = new StringBuilder();
        for (int i = 3; i < segments.length - 1; i++) {
            if (!folderBuilder.isEmpty()) {
                folderBuilder.append("/");
            }
            folderBuilder.append(segments[i]);
        }

        String reportFolder = folderBuilder.toString();  // ex: report/subReport
        String reportName   = segments[segments.length - 1];

        if (path.contains("/jasperserver/getByName")) {

            MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
            queryParams.add("_flowId", "viewReportFlow");
            queryParams.add("_flowId", "viewReportFlow");
            queryParams.add("ParentFolderUri", "/" + reportFolder);
            queryParams.add("reportUnit", "/" + reportFolder + "/" + reportName);
            queryParams.add("standAlone", "true");
            queryParams.add("_report", "report");

            queryParams.putAll(request.getQueryParams());

            // Rewrite the path to flow.html with updated query params
            URI originalUri = exchange.getRequest().getURI();
            URI newUri = UriComponentsBuilder.fromUri(originalUri)
                    .replacePath("/jasperserver/flow.html")
                    .replaceQueryParams(queryParams)
                    .build(true)
                    .toUri();

            // Mutate request
            ServerHttpRequest mutatedRequest = request.mutate()
                    .uri(newUri)
                    .build();

            exchange = exchange.mutate().request(mutatedRequest).build();
        }

        return chain.filter(exchange);
    }
}

