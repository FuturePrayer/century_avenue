package cn.miketsu.century_avenue.service.closed;

import cn.miketsu.century_avenue.service.LlmService;
import cn.miketsu.century_avenue.util.HttpUtil;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiStreamFunctionCallingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * 智谱清言GLM4
 *
 * @author sihuangwlp
 * @date 2024/5/22
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class GLM4ServiceImpl implements LlmService {

    @Value("${glm-4.api-key:}")
    private String apiKey;

    private static final String BASE_URL = "https://open.bigmodel.cn/api/paas";

    private final WebClient webClient;

    private final RestClient restClient;

    private static final Predicate<String> SSE_DONE_PREDICATE = "[DONE]"::equals;

    private final OpenAiStreamFunctionCallingHelper chunkMerger = new OpenAiStreamFunctionCallingHelper();

    public GLM4ServiceImpl() {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .build();
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    @Override
    public String model() {
        return "glm-4";
    }

    @Override
    public Boolean available() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public Flux<OpenAiApi.ChatCompletionChunk> stream(OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the steam property to true.");

        AtomicBoolean isInsideTool = new AtomicBoolean(false);

        return this.webClient.post()
                .uri("/v4/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .body(Mono.just(chatRequest), OpenAiApi.ChatCompletionRequest.class)
                .retrieve()
                .bodyToFlux(String.class)
                // 在收到“[DONE]”后取消flux流。
                .takeUntil(SSE_DONE_PREDICATE)
                // 过滤掉“[DONE]”消息。
                .filter(SSE_DONE_PREDICATE.negate())
                .map(content -> ModelOptionsUtils.jsonToObject(content, OpenAiApi.ChatCompletionChunk.class))
                // Detect是块是流函数调用的一部分。
                .map(chunk -> {
                    if (this.chunkMerger.isStreamingToolFunctionCall(chunk)) {
                        isInsideTool.set(true);
                    }
                    return chunk;
                })
                // 将属于同一函数调用的所有块分组。
                // Flux<ChatCompletionChunk> -> Flux<Flux<ChatCompletionChunk>>
                .windowUntil(chunk -> {
                    if (isInsideTool.get() && this.chunkMerger.isStreamingToolFunctionCallFinish(chunk)) {
                        isInsideTool.set(false);
                        return true;
                    }
                    return !isInsideTool.get();
                })
                // 将窗口块合并为单个块。
                // 将内部Flux＜ChatCompletionChunk＞窗口减少为单个Mono＜ChatComplexChunk＞，
                // Flux<Flux<ChatCompletionChunk>> -> Flux<Mono<ChatCompletionChunk>>
                .concatMapIterable(window -> {
                    Mono<OpenAiApi.ChatCompletionChunk> monoChunk = window.reduce(
                            new OpenAiApi.ChatCompletionChunk(null, null, null, null, null, null),
                            this.chunkMerger::merge);
                    return List.of(monoChunk);
                })
                // Flux<Mono<ChatCompletionChunk>> -> Flux<ChatCompletionChunk>
                .flatMap(mono -> mono);
    }

    @Override
    public Flux<OpenAiApi.ChatCompletion> call(OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream() == null || !chatRequest.stream(), "Request must set the steam property to false.");

        return Flux.just(
                HttpUtil.post()
                        .url("/v4/chat/completions")
                        .header("Authorization", "Bearer " + apiKey)
                        .body(chatRequest)
                        .resp(OpenAiApi.ChatCompletion.class)
        );
    }
}
