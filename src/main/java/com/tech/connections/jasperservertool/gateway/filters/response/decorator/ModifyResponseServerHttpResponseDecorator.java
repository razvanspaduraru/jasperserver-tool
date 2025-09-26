package com.tech.connections.jasperservertool.gateway.filters.response.decorator;

import com.tech.connections.jasperservertool.gateway.filters.response.transformer.TransformResponse;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

public class ModifyResponseServerHttpResponseDecorator extends ServerHttpResponseDecorator {
    private final ServerWebExchange exchange;
    private final Map<String, MessageBodyDecoder> messageBodyDecoders;
    private final Map<String, MessageBodyEncoder> messageBodyEncoders;
    private final TransformResponse transformResponse;
    public ModifyResponseServerHttpResponseDecorator(ServerHttpResponse delegate,
                                                     ServerWebExchange exchange,
                                                     Map<String, MessageBodyDecoder> messageBodyDecoders,
                                                     Map<String, MessageBodyEncoder> messageBodyEncoders, TransformResponse transformResponse) {
        super(delegate);
        this.exchange = exchange;
        this.messageBodyDecoders = messageBodyDecoders;
        this.messageBodyEncoders = messageBodyEncoders;
        this.transformResponse = transformResponse;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

        Class inClass = String.class;
        Class outClass = String.class;

        String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
        HttpHeaders httpHeaders = new HttpHeaders();

        if (originalResponseContentType != null && originalResponseContentType.contains("text/html")) {
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, originalResponseContentType);

            ClientResponse clientResponse = prepareClientResponse(body, httpHeaders);

            // TODO: flux or mono
            Mono modifiedBody = extractBody(exchange, clientResponse, inClass)
                    .flatMap(originalBody -> {
                        String contentType = exchange.getResponse().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                        if (contentType != null && contentType.contains("text/html")) {
                            // Safe to transform HTML pages
                            return Mono.just(transformResponse.applyTransform((String) originalBody));
                        } else {
                            // Leave JSON or other responses untouched
                            return Mono.just(originalBody);
                        }
                    })
                    .switchIfEmpty(Mono.empty());

            BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
            CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange,
                    exchange.getResponse().getHeaders());
            return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
                Mono<DataBuffer> messageBody = writeBody(getDelegate(), outputMessage, outClass);
                HttpHeaders headers = getDelegate().getHeaders();
                if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)
                        || headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                    messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
                }
                // TODO: fail if isStreamingMediaType?
                return getDelegate().writeWith(messageBody);
            }));
        } else {
            return super.writeWith(body);
        }
    }


    private ClientResponse prepareClientResponse(Publisher<? extends DataBuffer> body, HttpHeaders httpHeaders) {
        ClientResponse.Builder builder;
        builder = ClientResponse.create(exchange.getResponse().getStatusCode(), HandlerStrategies.withDefaults().messageReaders());
        return builder.headers(headers -> headers.putAll(httpHeaders)).body(Flux.from(body)).build();
    }

    private <T> Mono<T> extractBody(ServerWebExchange exchange, ClientResponse clientResponse, Class<T> inClass) {
        // if inClass is byte[] then just return body, otherwise check if
        // decoding required
        if (byte[].class.isAssignableFrom(inClass)) {
            return clientResponse.bodyToMono(inClass);
        }

        List<String> encodingHeaders = exchange.getResponse().getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
        for (String encoding : encodingHeaders) {
            MessageBodyDecoder decoder = messageBodyDecoders.get(encoding);
            if (decoder != null) {
                return clientResponse.bodyToMono(byte[].class).publishOn(Schedulers.parallel()).map(decoder::decode)
                        .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                        .map(buffer -> prepareClientResponse(Mono.just(buffer),
                                exchange.getResponse().getHeaders()))
                        .flatMap(response -> response.bodyToMono(inClass));
            }
        }

        return clientResponse.bodyToMono(inClass);
    }

    private Mono<DataBuffer> writeBody(ServerHttpResponse httpResponse, CachedBodyOutputMessage message,
                                       Class<?> outClass) {
        Mono<DataBuffer> response = DataBufferUtils.join(message.getBody());
        if (byte[].class.isAssignableFrom(outClass)) {
            return response;
        }

        List<String> encodingHeaders = httpResponse.getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
        for (String encoding : encodingHeaders) {
            MessageBodyEncoder encoder = messageBodyEncoders.get(encoding);
            if (encoder != null) {
                DataBufferFactory dataBufferFactory = httpResponse.bufferFactory();
                response = response.publishOn(Schedulers.parallel()).map(buffer -> {
                    byte[] encodedResponse = encoder.encode(buffer);
                    DataBufferUtils.release(buffer);
                    return encodedResponse;
                }).map(dataBufferFactory::wrap);
                break;
            }
        }

        return response;
    }
}
