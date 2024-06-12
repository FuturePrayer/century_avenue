package cn.miketsu.century_avenue.service.common;

import cn.miketsu.century_avenue.config.OpenAIConfig;
import cn.miketsu.century_avenue.record.OpenaiCfg;
import cn.miketsu.century_avenue.service.LlmService;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.lang.reflect.Field;
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

        OpenAiChatClient openAiChatClient = getOpenAiChatClient(chatRequest, openaiCfg);

        try {
            Class<OpenAiChatClient> clazz = OpenAiChatClient.class;

            Field field = clazz.getDeclaredField("openAiApi");

            field.setAccessible(true);

            OpenAiApi openAiApi = (OpenAiApi) field.get(openAiChatClient);

            return openAiApi.chatCompletionStream(chatRequest);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Flux<OpenAiApi.ChatCompletion> call(OpenAiApi.ChatCompletionRequest chatRequest) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream() == null || !chatRequest.stream(), "Request must set the steam property to false.");

        OpenaiCfg openaiCfg = getCfg(chatRequest.model());
        Assert.notNull(openaiCfg, String.format("Cannot find config for model \"%s\"!", chatRequest.model()));

        OpenAiChatClient openAiChatClient = getOpenAiChatClient(chatRequest, openaiCfg);

        try {
            Class<OpenAiChatClient> clazz = OpenAiChatClient.class;

            Field field = clazz.getDeclaredField("openAiApi");

            field.setAccessible(true);

            OpenAiApi openAiApi = (OpenAiApi) field.get(openAiChatClient);

            ResponseEntity<OpenAiApi.ChatCompletion> responseEntity = openAiApi.chatCompletionEntity(chatRequest);

            return Flux.just(responseEntity.getBody());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private OpenaiCfg getCfg(String model) {
        return openAIConfig.getModels().stream().filter(cfg -> cfg.model().equals(model)).findFirst().orElse(null);
    }

    private static OpenAiChatClient getOpenAiChatClient(OpenAiApi.ChatCompletionRequest chatRequest, OpenaiCfg openaiCfg) {
        return new OpenAiChatClient(
                new OpenAiApi(openaiCfg.baseUrl(), openaiCfg.apiKey()),
                OpenAiChatOptions.builder()
                        .withTools(chatRequest.tools())
                        .withFrequencyPenalty(chatRequest.frequencyPenalty())
                        .withModel(chatRequest.model())
                        .withLogitBias(chatRequest.logitBias())
                        .withMaxTokens(chatRequest.maxTokens())
                        .withN(chatRequest.n())
                        .withPresencePenalty(chatRequest.presencePenalty())
                        .withResponseFormat(chatRequest.responseFormat())
                        .withSeed(chatRequest.seed())
                        .withStop(chatRequest.stop())
                        .withTemperature(chatRequest.temperature())
                        .withTopP(chatRequest.topP())
                        .withToolChoice(chatRequest.toolChoice())
                        .withUser(chatRequest.user())
                        .build()
        );
    }
}
