package cn.miketsu.century_avenue.service.completions.common;

import cn.miketsu.century_avenue.config.CenturyAvenueConfig;
import cn.miketsu.century_avenue.config.CenturyAvenueConfig.OpenAI.Model;
import cn.miketsu.century_avenue.service.completions.LlmService;
import cn.miketsu.century_avenue.util.ConstantUtil;
import cn.miketsu.century_avenue.util.OpenAiUtil;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 调用所有符合OpenAI风格的大模型api
 *
 * @author sihuangwlp
 * @date 2024/6/12
 * @since 1.0.2
 */
@Service
public class OpenAICompletionImpl implements LlmService {

    private final CenturyAvenueConfig centuryAvenueConfig;

    /**
     * 负载均衡（轮询）
     * 
     * @since 2.0.0-release
     */
    private final Map<String, Queue<Model>> modelsQueue;

    @Autowired
    public OpenAICompletionImpl(CenturyAvenueConfig centuryAvenueConfig) {
        this.centuryAvenueConfig = centuryAvenueConfig;
        this.modelsQueue = new LinkedHashMap<>();

        Map<String, List<Model>> map = centuryAvenueConfig.openai().models().stream().collect(Collectors.groupingBy(Model::model));
        for (Map.Entry<String, List<Model>> entry : map.entrySet()) {
            Queue<Model> queue = new LinkedList<>();
            for (Model model : entry.getValue()) {
                if (model.apiKey().contains(ConstantUtil.LOAD_BALANCING_SEPARATOR)) {
                    for (String apiKey : model.apiKey().split(ConstantUtil.LOAD_BALANCING_SEPARATOR)) {
                        queue.offer(new Model(apiKey, model.model(), model.baseUrl()));
                    }
                } else {
                    queue.offer(model);
                }
            }
            modelsQueue.put(entry.getKey(), queue);
        }
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
        Queue<Model> modelQueue = modelsQueue.get(model);
        Model poll = modelQueue.remove();
        modelQueue.offer(poll);
        return poll;
    }
}
