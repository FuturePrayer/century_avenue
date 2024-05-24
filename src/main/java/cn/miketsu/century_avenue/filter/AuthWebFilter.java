package cn.miketsu.century_avenue.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * API KEY鉴权
 *
 * @author sihuangwlp
 * @date 2024/5/24
 * @since 0.0.3-SNAPSHOT
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class AuthWebFilter implements WebFilter {

    @Value("${api-key:}")
    private String apiKey;

    private static final String REQUIRED_HEADER = "Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 获取ServerHttpRequest
        ServerHttpRequest request = exchange.getRequest();

        // 检查是否存在指定的header
        if (!apiKey.isBlank() && (
                !request.getHeaders().containsKey(REQUIRED_HEADER)
                        || request.getHeaders().get(REQUIRED_HEADER) == null
                        || request.getHeaders().get(REQUIRED_HEADER).isEmpty())) {
            // 如果不存在，设置403 Forbidden状态码
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("Forbidden: Missing required header '".concat(REQUIRED_HEADER).concat("'").getBytes())));
        } else if (!apiKey.isBlank()
                && !Objects.equals(request.getHeaders().get(REQUIRED_HEADER).get(0), "Bearer " + apiKey)) {
            // 如果错误，设置403 Forbidden状态码
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("Forbidden: Required header '".concat(REQUIRED_HEADER).concat("' is wrong").getBytes())));
        }

        // 如果存在指定的header，继续处理请求
        return chain.filter(exchange);
    }
}
