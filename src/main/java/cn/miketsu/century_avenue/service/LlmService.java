package cn.miketsu.century_avenue.service;

import org.springframework.ai.openai.api.OpenAiApi;
import reactor.core.publisher.Flux;

/**
 * @author sihuangwlp
 * @date 2024/5/22
 */
public interface LlmService {
    String model();

    Flux<OpenAiApi.ChatCompletionChunk> stream(OpenAiApi.ChatCompletionRequest chatRequest);

    Flux<OpenAiApi.ChatCompletion> call(OpenAiApi.ChatCompletionRequest chatRequest);
}
