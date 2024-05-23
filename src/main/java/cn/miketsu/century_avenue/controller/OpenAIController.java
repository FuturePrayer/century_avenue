package cn.miketsu.century_avenue.controller;

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
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author sihuangwlp
 * @date 2024/5/22
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/v1")
public class OpenAIController {

    @Autowired
    private List<LlmService> llmServices;

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
        System.out.println(JacksonUtil.tryParse(chatCompletionRequest));
        Optional<LlmService> first = llmServices.stream()
                .filter(llmService -> llmService.available() && llmService.model().equals(chatCompletionRequest.model()))
                .findFirst();
        if (first.isEmpty()) {
            throw new RuntimeException("model not found");
        } else if (chatCompletionRequest.stream() != null && chatCompletionRequest.stream()) {
            //流式返回
            Sinks.Many<String> sink = Sinks.many().replay().latest();
            Flux<String> flux = first.get().stream(chatCompletionRequest).map(JacksonUtil::tryParse);
            flux.doOnComplete(() -> {
                sink.tryEmitNext("[DONE]");
                sink.tryEmitComplete();
            }).subscribe(sink::tryEmitNext);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_EVENT_STREAM);
            return new ResponseEntity<>(sink.asFlux(), headers, HttpStatus.OK);
        } else {
            //非流式返回
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(first.get().call(chatCompletionRequest).map(JacksonUtil::tryParse), headers, HttpStatus.OK);
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
        return Mono.just(JacksonUtil.tryParse(
                        new HashMap<String, Object>() {{
                            put("object", "list");
                            put("data",
                                    llmServices.stream()
                                            .filter(LlmService::available)
                                            .map(llm -> new HashMap<String, String>() {{
                                                put("id", llm.model());
                                                put("object", "model");
                                                put("created", created);
                                                put("owned_by", "system");
                                            }})
                                            .toList()
                            );
                        }}
                )
        );
    }
}
