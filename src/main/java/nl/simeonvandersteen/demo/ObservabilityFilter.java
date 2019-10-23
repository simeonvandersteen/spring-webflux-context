package nl.simeonvandersteen.demo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ObservabilityFilter implements WebFilter {

    public static final String CORRELATION_ID = "CorrelationId";
    public static final String REQUEST_ID = "RequestId";

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();

        Map<String, String> contextMap = contextMapFromRequest(request);

        addContextToResponse(serverWebExchange, contextMap);

        return webFilterChain
                .filter(serverWebExchange)
                .subscriberContext(context -> context.putAll(Context.of(contextMap)));
    }

    private void addContextToResponse(ServerWebExchange serverWebExchange, Map<String, String> contextMap) {
        serverWebExchange
                .getResponse()
                .beforeCommit(() -> Mono.<Void>empty()
                        .doOnTerminate(() -> {
                            HttpHeaders headers = serverWebExchange.getResponse().getHeaders();
                            contextMap.forEach(headers::add);
                        }));
    }

    private Map<String, String> contextMapFromRequest(ServerHttpRequest request) {
        return Map.of(
                REQUEST_ID, UUID.randomUUID().toString(),
                CORRELATION_ID, request.getHeaders()
                        .getOrDefault(CORRELATION_ID, List.of(UUID.randomUUID().toString())).get(0));
    }
}
