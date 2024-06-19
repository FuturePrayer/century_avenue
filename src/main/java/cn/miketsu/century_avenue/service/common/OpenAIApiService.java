package cn.miketsu.century_avenue.service.common;

import cn.miketsu.century_avenue.config.CenturyAvenueConfig;
import cn.miketsu.century_avenue.config.CenturyAvenueConfig.OpenAI.Model;
import cn.miketsu.century_avenue.service.LlmService;
import cn.miketsu.century_avenue.util.OpenAiUtil;
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

    private final CenturyAvenueConfig centuryAvenueConfig;

    @Autowired
    public OpenAIApiService(CenturyAvenueConfig centuryAvenueConfig) {
        this.centuryAvenueConfig = centuryAvenueConfig;
    }

    @Override
    public String model() {
        return "";
    }

    @Override
    public Boolean available() {
        return centuryAvenueConfig.openai() != null
                && centuryAvenueConfig.openai().models() != null
                && !centuryAvenueConfig.openai().models().isEmpty();
    }

    @Override
    public Collection<String> subModels() {
        if (available()) {
            return centuryAvenueConfig.openai().models().stream().map(Model::model).collect(Collectors.toSet());
        } else {
            return LlmService.super.subModels();
        }
    }

    @Override
    public Flux<OpenAiApi.ChatCompletionChunk> stream(OpenAiApi.ChatCompletionRequest chatRequest) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the stream property to true.");

        Model openaiCfg = getCfg(chatRequest.model());
        Assert.notNull(openaiCfg, String.format("Cannot find config for model \"%s\"!", chatRequest.model()));

        OpenAiUtil openAiApi = new OpenAiUtil(openaiCfg.baseUrl(), openaiCfg.apiKey());

        return openAiApi.chatCompletionStream(chatRequest);
    }

    @Override
    public Flux<OpenAiApi.ChatCompletion> call(OpenAiApi.ChatCompletionRequest chatRequest) {

        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream() == null || !chatRequest.stream(), "Request must set the stream property to false.");

        Model openaiCfg = getCfg(chatRequest.model());
        Assert.notNull(openaiCfg, String.format("Cannot find config for model \"%s\"!", chatRequest.model()));

        OpenAiUtil openAiApi = new OpenAiUtil(openaiCfg.baseUrl(), openaiCfg.apiKey());

        ResponseEntity<OpenAiApi.ChatCompletion> responseEntity = openAiApi.chatCompletionEntity(chatRequest);

        return Flux.just(responseEntity.getBody());
    }

    private Model getCfg(String model) {
        return centuryAvenueConfig.openai().models().stream().filter(cfg -> cfg.model().equals(model)).findFirst().orElse(null);
    }
}
