package com.tech.connections.jasperservertool.gateway.filters.jasper;

import com.tech.connections.jasperservertool.gateway.filters.response.decorator.ModifyResponseServerHttpResponseDecorator;
import com.tech.connections.jasperservertool.gateway.filters.response.transformer.JasperHtmlTransformerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

@Component
@Slf4j
public class JasperReportGateway {

    @Value("${nexgen.jasper.admin-user}")
    private String jasperAdminUser;
    @Value("${nexgen.jasper.admin-password}")
    private String jasperAdminPassword;
    @Value("${nexgen.jasper.formio-user}")
    private String jasperFormIoUser;
    @Value("${nexgen.jasper.formio-password}")
    private String jasperFormIoPassword;

    private final Map<String, MessageBodyDecoder> messageBodyDecoders;
    private final Map<String, MessageBodyEncoder> messageBodyEncoders;
    private final JasperHtmlTransformerResponse jasperTransformResponse = new JasperHtmlTransformerResponse();

    public JasperReportGateway(Set<MessageBodyDecoder> messageBodyDecoders,
                               Set<MessageBodyEncoder> messageBodyEncoders) {

        this.messageBodyDecoders = messageBodyDecoders.stream()
                .collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
        this.messageBodyEncoders = messageBodyEncoders.stream()
                .collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
    }

    private ServerWebExchange addHeaderAuth(ServerWebExchange exchange, boolean isAdmin) {
        String uri = exchange.getRequest().getURI().toString();
        String user, password;
        if (isAdmin) {
            user = jasperAdminUser;
            password = jasperAdminPassword;
        } else {
            user = jasperFormIoUser;
            password = jasperFormIoPassword;
        }

        if (uri.contains("?")) {
            uri += "&j_username="+user+"&j_password="+password;
        } else {
            uri += "?j_username="+user+"&j_password="+password;
        }
        ServerHttpRequest request = exchange.getRequest().mutate()
                .uri(URI.create(uri))
                .build();
        return exchange.mutate().request(request).build();
    }

    public GatewayFilter apply() {

        return new OrderedGatewayFilter((exchangeIn, chain) -> {
            boolean isAdmin = false;
//            TODO as32
            ServerWebExchange exchange = addHeaderAuth(exchangeIn, isAdmin);
            ServerHttpResponse originalResponse = exchange.getResponse();
            try {
                Principal p = exchangeIn.getPrincipal().toFuture().get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error getting principal", e);
                throw new RuntimeException(e);
            }

            ServerHttpResponseDecorator decoratedResponse = new ModifyResponseServerHttpResponseDecorator(
                    originalResponse,
                    exchange,
                    messageBodyDecoders,
                    messageBodyEncoders,
                    jasperTransformResponse);
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        }, NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1);
    }
}
