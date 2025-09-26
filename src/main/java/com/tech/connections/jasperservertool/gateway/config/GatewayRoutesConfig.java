package com.tech.connections.jasperservertool.gateway.config;

import com.tech.connections.jasperservertool.gateway.filters.jasper.JasperReportGateway;
import com.tech.connections.jasperservertool.gateway.filters.jasper.request.JasperGetReportByName;
import com.tech.connections.jasperservertool.gateway.filters.jasper.request.JasperGetAdminFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GatewayRoutesConfig {

    @Value("${nexgen.jasper.uri}")
    private String jasper_uri;


    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           JasperReportGateway jasperReportGateway,
                                           JasperGetAdminFilter jasperGetAdminFilter,
                                           JasperGetReportByName jasperGetReportByName) {
        log.info("Custom Route Locator");
        return builder.routes()
                .route("jasperModule", r -> r.path("/jasperserver/getAdmin/**")
                        .filters(f -> f
                                .filter(jasperGetAdminFilter)
                                .filter(jasperReportGateway.apply())
                        )
                        .uri(jasper_uri)
                )
                .route("jasperGetReport", r -> r.path("/jasperserver/getByName/**")
                        .filters(f -> f
                                .filter(jasperGetReportByName)
                                .filter(jasperReportGateway.apply())
                        )
                        .uri(jasper_uri)
                )
                .route("jasperModule", r -> r.path("/jasperserver/**")
                        .filters(f -> f
                                .filter(jasperGetAdminFilter)
                                .filter(jasperReportGateway.apply())
                        )
                        .uri(jasper_uri)
                )
                .build();
    }
}
