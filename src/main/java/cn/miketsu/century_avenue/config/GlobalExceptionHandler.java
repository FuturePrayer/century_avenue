package cn.miketsu.century_avenue.config;

import cn.miketsu.century_avenue.util.JacksonUtil;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * 全局异常处理
 *
 * @author sihuangwlp
 * @date 2024/5/26
 * @since 0.0.4-SNAPSHOT
 */
@Component
@Order(-2)
public class GlobalExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof RuntimeException) {
            return handleRuntimeException((RuntimeException) ex, exchange);
        } else {
            return handleDefaultException(ex, exchange);
        }
    }

    private Mono<Void> handleRuntimeException(RuntimeException ex, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(JacksonUtil.tryParse(new ErrorResponse(new ErrorResponse.Error(-1, ex.getMessage()))).getBytes())));
    }

    private Mono<Void> handleDefaultException(Throwable ex, ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("An unexpected error occurred".getBytes())));
    }

    public record ErrorResponse(Error error) {

        public record Error(Integer code, String message) {
        }
    }
}
