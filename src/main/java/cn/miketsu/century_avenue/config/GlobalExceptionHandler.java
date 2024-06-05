package cn.miketsu.century_avenue.config;

import cn.miketsu.century_avenue.util.JacksonUtil;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * 全局异常处理
 *
 * @author sihuangwlp
 * @date 2024/5/26
 * @since 0.0.4-SNAPSHOT
 */
@Order(-2)
@RestControllerAdvice
public class GlobalExceptionHandler implements WebExceptionHandler {

    private static final Logger log = Loggers.getLogger(GlobalExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof RuntimeException) {
            return handleRuntimeException((RuntimeException) ex, exchange);
        } else {
            return handleDefaultException(ex, exchange);
        }
    }

    private Mono<Void> handleRuntimeException(RuntimeException ex, ServerWebExchange exchange) {
        log.error("运行时异常！", ex);
        exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(JacksonUtil.tryParse(new ErrorResponse(new ErrorResponse.Error(-1, ex.getMessage()))).getBytes())));
    }

    private Mono<Void> handleDefaultException(Throwable ex, ServerWebExchange exchange) {
        log.error("程序出现异常！", ex);
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap("An unexpected error occurred".getBytes())));
    }

    public record ErrorResponse(Error error) {

        public record Error(Integer code, String message) {
        }
    }
}
