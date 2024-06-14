package cn.miketsu.century_avenue.service.common;

import cn.miketsu.century_avenue.config.OpenAIConfig;
import cn.miketsu.century_avenue.record.OpenaiCfg;
import cn.miketsu.century_avenue.service.LlmService;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 调用所有符合OpenAI风格的大模型api
 *
 * @author sihuangwlp
 * @date 2024/6/12
 * @since 1.0.2
 */
@Service
public class OpenAIApiService implements LlmService {

    private final OpenAIConfig openAIConfig;

    @Autowired
    public OpenAIApiService(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
    }

    @Override
    public String model() {
        return "";
    }

    @Override
    public Boolean available() {
        return openAIConfig.getModels() != null && !openAIConfig.getModels().isEmpty();
    }

    @Override
    public Collection<String> subModels() {
        if (available()) {
            return openAIConfig.getModels().stream().map(OpenaiCfg::model).collect(Collectors.toSet());
        } else {
            return LlmService.super.subModels();
        }
    }

    @Override
    public Flux<OpenAiApi.ChatCompletionChunk> stream(OpenAiApi.ChatCompletionRequest chatRequest) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the steam property to true.");

        OpenaiCfg openaiCfg = getCfg(chatRequest.model());
        Assert.notNull(openaiCfg, String.format("Cannot find config for model \"%s\"!", chatRequest.model()));

        OpenAiApi openAiApi = new OpenAiApi(openaiCfg.baseUrl(), openaiCfg.apiKey());

        return openAiApi.chatCompletionStream(chatRequest);
    }

    @Override
    public Flux<OpenAiApi.ChatCompletion> call(OpenAiApi.ChatCompletionRequest chatRequest) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream() == null || !chatRequest.stream(), "Request must set the steam property to false.");

        OpenaiCfg openaiCfg = getCfg(chatRequest.model());
        Assert.notNull(openaiCfg, String.format("Cannot find config for model \"%s\"!", chatRequest.model()));

        OpenAiApi openAiApi = new OpenAiApi(openaiCfg.baseUrl(), openaiCfg.apiKey());

        ResponseEntity<OpenAiApi.ChatCompletion> responseEntity = openAiApi.chatCompletionEntity(chatRequest);

        return Flux.just(responseEntity.getBody());
    }

    private OpenaiCfg getCfg(String model) {
        return openAIConfig.getModels().stream().filter(cfg -> cfg.model().equals(model)).findFirst().orElse(null);
    }
}
