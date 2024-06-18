package cn.miketsu.century_avenue.controller;

import cn.miketsu.century_avenue.config.DockingConfig;
import cn.miketsu.century_avenue.service.LlmService;
import cn.miketsu.century_avenue.util.JacksonUtil;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author sihuangwlp
 * @date 2024/5/22
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/v1")
public class OpenAIController {

    private static final Logger log = Loggers.getLogger(OpenAIController.class);

    @Autowired
    private List<LlmService> llmServices;

    @Autowired
    private DockingConfig dockingConfig;

    /**
     * 聊天
     *
     * @param chatCompletionRequest openai风格的请求参数
     * @return openai风格的响应参数
     * @author sihuangwlp
     * @date 2024/5/22
     * @since 0.0.1-SNAPSHOT
     */
    @PostMapping("/chat/completions")
    public ResponseEntity<Flux<String>> completions(@RequestBody OpenAiApi.ChatCompletionRequest chatCompletionRequest) {
        log.info("聊天补全接口，传入报文：{}", JacksonUtil.tryParse(chatCompletionRequest));
        //模型名称映射
        String model;
        if (dockingConfig.getModelMapping() != null && dockingConfig.getModelMapping().containsKey(chatCompletionRequest.model())) {
            model = dockingConfig.getModelMapping().get(chatCompletionRequest.model());
        } else {
            model = chatCompletionRequest.model();
        }

        Optional<LlmService> first = llmServices.stream()
                .filter(llmService -> llmService.available() && (llmService.model().equals(model) || llmService.subModels().contains(model)))
                .findFirst();

        chatCompletionRequest = new OpenAiApi.ChatCompletionRequest(
                chatCompletionRequest.messages(),
                model,
                chatCompletionRequest.frequencyPenalty(),
                chatCompletionRequest.logitBias(),
                chatCompletionRequest.logprobs(),
                chatCompletionRequest.topLogprobs(),
                chatCompletionRequest.maxTokens(),
                chatCompletionRequest.n(),
                chatCompletionRequest.presencePenalty(),
                chatCompletionRequest.responseFormat(),
                chatCompletionRequest.seed(),
                chatCompletionRequest.stop(),
                chatCompletionRequest.stream() != null && chatCompletionRequest.stream(),
                chatCompletionRequest.temperature(),
                chatCompletionRequest.topP(),
                chatCompletionRequest.tools(),
                chatCompletionRequest.toolChoice(),
                chatCompletionRequest.user()
        );
        log.debug("聊天补全接口，重新组装后报文：{}", JacksonUtil.tryParse(chatCompletionRequest));

        if (first.isEmpty()) {
            throw new RuntimeException(String.format("model %s not found", model));
        } else if (chatCompletionRequest.stream() != null && chatCompletionRequest.stream()) {
            //流式返回
            Flux<String> flux = first.get().stream(chatCompletionRequest).map(JacksonUtil::tryParse);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_EVENT_STREAM);
            return new ResponseEntity<>(flux.concatWith(Flux.just("[DONE]").log(log, Level.FINE, false)), headers, HttpStatus.OK);
        } else {
            //非流式返回
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(first.get().call(chatCompletionRequest).map(JacksonUtil::tryParse).log(log, Level.FINE, false), headers, HttpStatus.OK);
        }
    }

    /**
     * 可用的模型列表
     *
     * @return openai风格的模型列表响应参数
     * @author sihuangwlp
     * @date 2024/5/22
     * @since 0.0.1-SNAPSHOT
     */
    @GetMapping(value = "/models", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> models() {
        String created = String.valueOf(System.currentTimeMillis() / 1000);
        //统计可用的模型列表
        Set<String> modelList = llmServices.stream()
                .filter(LlmService::available)
                .map(LlmService::model)
                .collect(Collectors.toSet());
        //子模型
        modelList.addAll(llmServices.stream()
                .filter(LlmService::available)
                .map(LlmService::subModels)
                .flatMap(Collection::stream).collect(Collectors.toSet()));
        //添加上他们的别名
        modelList.addAll(modelList.stream()
                .map(this::getAliasOfModel)
                .filter(s -> s != null && !s.isEmpty())
                .flatMap(List::stream).collect(Collectors.toSet()));
        return Mono.just(JacksonUtil.tryParse(
                                new HashMap<String, Object>() {{
                                    put("object", "list");
                                    put("data",
                                            modelList.stream()
                                                    .filter(llm -> llm != null && !llm.isBlank())
                                                    .map(llm -> new HashMap<String, String>() {{
                                                        put("id", llm);
                                                        put("object", "model");
                                                        put("created", created);
                                                        put("owned_by", "system");
                                                    }})
                                                    .sorted(Comparator.comparing(o -> o.get("id"), Comparator.naturalOrder()))
                                                    .toList()
                                    );
                                }}
                        )
                )
                .log(log, Level.FINE, false);
    }

    /**
     * 获取指定模型的别名
     *
     * @param model 指定的模型名称
     * @return 模型别名列表
     * @author sihuangwlp
     * @since 0.0.4-SNAPSHOT
     */
    private List<String> getAliasOfModel(String model) {
        if (dockingConfig.getModelMapping() == null) {
            return Collections.emptyList();
        }
        return dockingConfig.getModelMapping().entrySet().stream()
                .filter(entry -> entry.getValue().equals(model))
                .map(Map.Entry::getKey)
                .toList();
    }
}
