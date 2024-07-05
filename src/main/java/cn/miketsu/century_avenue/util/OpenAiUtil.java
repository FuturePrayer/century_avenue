package cn.miketsu.century_avenue.util;

import cn.miketsu.century_avenue.record.EmbeddingsReq;
import cn.miketsu.century_avenue.record.EmbeddingsResp;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.ApiUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * 调用OpenAI API工具类
 *
 * @author sihuangwlp
 * @date 2024/6/18
 * @since 1.0.5
 */
public class OpenAiUtil {

    private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

    private final RestClient restClient;

    private final WebClient webClient;

    public OpenAiUtil(String baseUrl, String openAiToken) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(ApiUtils.getJsonContentHeaders(openAiToken))
                .defaultStatusHandler(RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER)
                .build();

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(ApiUtils.getJsonContentHeaders(openAiToken))
                .build();
    }

    public Flux<OpenAiApi.ChatCompletionChunk> chatCompletionStream(OpenAiApi.ChatCompletionRequest chatRequest) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the steam property to true.");

        return this.webClient.post()
                .uri("/chat/completions")
                .body(Mono.just(chatRequest), OpenAiApi.ChatCompletionRequest.class)
                .retrieve()
                .bodyToFlux(String.class)
                .takeUntil(SSE_DONE_PREDICATE)
                .filter(SSE_DONE_PREDICATE.negate())
                .map(content -> ModelOptionsUtils.jsonToObject(content, OpenAiApi.ChatCompletionChunk.class));
    }

    public ResponseEntity<OpenAiApi.ChatCompletion> chatCompletionEntity(OpenAiApi.ChatCompletionRequest chatRequest) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(!chatRequest.stream(), "Request must set the steam property to false.");

        return this.restClient.post()
                .uri("/chat/completions")
                .body(chatRequest)
                .retrieve()
                .toEntity(OpenAiApi.ChatCompletion.class);
    }

    public ResponseEntity<EmbeddingsResp> embeddings(EmbeddingsReq embeddingRequest) {

        Assert.notNull(embeddingRequest, "The request body can not be null.");

        Assert.notNull(embeddingRequest.input(), "The input can not be null.");

        return this.restClient.post()
                .uri("/embeddings")
                .body(embeddingRequest)
                .retrieve()
                .toEntity(EmbeddingsResp.class);
    }
}
