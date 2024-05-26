package cn.miketsu.century_avenue.service.closed;

import cn.miketsu.century_avenue.record.ErnieAuthResp;
import cn.miketsu.century_avenue.record.ErnieReq;
import cn.miketsu.century_avenue.record.ErnieResp;
import cn.miketsu.century_avenue.service.LlmService;
import cn.miketsu.century_avenue.util.JacksonUtil;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.*;

/**
 * ERNIE-Tiny-8K
 *
 * @author sihuangwlp
 * @date 2024/5/26
 * @since 0.0.4-SNAPSHOT
 */
@Service
public class ErnieTinyServiceImpl implements LlmService {

    @Value("${ernie-tiny-8k.api-key:}")
    private String apiKey;

    @Value("${ernie-tiny-8k.secret-key:}")
    private String secretKey;

    private static final String authUrl = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s";

    private static final String baseUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-tiny-8k?access_token=%s";

    @Override
    public String model() {
        return "ernie-tiny-8k";
    }

    @Override
    public Boolean available() {
        return apiKey != null
                && !apiKey.isBlank()
                && secretKey != null
                && !secretKey.isBlank();
    }

    @Override
    public Flux<OpenAiApi.ChatCompletionChunk> stream(OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the steam property to true.");

        Sinks.Many<OpenAiApi.ChatCompletionChunk> sink = Sinks.many().replay().latest();

        Map<String, Object> map = new HashMap<>();

        WebClient.builder()
                .build()
                .post()
                .uri(String.format(baseUrl, getAccessToken()))
                .header("Content-Type", "application/json")
                .body(Mono.just(convert(chatRequest)), ErnieReq.class)
                .retrieve()
                .bodyToFlux(String.class)
                .map(content -> ModelOptionsUtils.jsonToObject(content, ErnieResp.class))
                .map(ernieResp -> new OpenAiApi.ChatCompletionChunk(ernieResp.id(),
                        Collections.singletonList(new OpenAiApi.ChatCompletionChunk.ChunkChoice(null, ernieResp.sentence_id(), new OpenAiApi.ChatCompletionMessage(ernieResp.result(), OpenAiApi.ChatCompletionMessage.Role.ASSISTANT), null)),
                        ernieResp.created(),
                        this.model(),
                        null,
                        ernieResp.object()))
                .doOnNext(chunk -> {
                    map.put("id", chunk.id());
                    map.put("index", chunk.choices().getFirst().index());
                })
                .doOnComplete(() -> sink.tryEmitNext(new OpenAiApi.ChatCompletionChunk(
                        (String) map.get("sid"),
                        Collections.singletonList(new OpenAiApi.ChatCompletionChunk.ChunkChoice(
                                OpenAiApi.ChatCompletionFinishReason.STOP,
                                ((Integer) map.getOrDefault("index", -1)) + 1,
                                new OpenAiApi.ChatCompletionMessage(null, null),
                                null)),
                        null,
                        null,
                        null,
                        null)))
                .subscribe(sink::tryEmitNext);
        return sink.asFlux();
    }

    @Override
    public Flux<OpenAiApi.ChatCompletion> call(OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream() == null || !chatRequest.stream(), "Request must set the steam property to false.");

        return Flux.just(convert(Objects.requireNonNull(RestClient.builder()
                .build()
                .post()
                .uri(String.format(baseUrl, getAccessToken()))
                .header("Content-Type", "application/json")
                .body(JacksonUtil.tryParse(convert(chatRequest)))
                .retrieve()
                .body(ErnieResp.class))));
    }

    private String getAccessToken() {
        RestClient restClient = RestClient.builder().baseUrl(String.format(authUrl, apiKey, secretKey)).build();
        ErnieAuthResp authResp = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ErnieAuthResp.class);
        if (authResp == null) {
            throw new RuntimeException("ERNIE Tiny auth failed.");
        }
        return authResp.access_token();
    }

    private ErnieReq convert(OpenAiApi.ChatCompletionRequest chatCompletionRequest) {
        List<ErnieReq.Message> list = chatCompletionRequest.messages().stream()
                .filter(message -> OpenAiApi.ChatCompletionMessage.Role.USER.name().equalsIgnoreCase(message.role().name()) || OpenAiApi.ChatCompletionMessage.Role.ASSISTANT.name().equalsIgnoreCase(message.role().name()))
                .map(message -> new ErnieReq.Message(message.role().name().toLowerCase(), message.content()))
                .toList();

        Optional<OpenAiApi.ChatCompletionMessage> system = chatCompletionRequest.messages().stream()
                .filter(message -> OpenAiApi.ChatCompletionMessage.Role.SYSTEM.name().equalsIgnoreCase(message.role().name()))
                .findFirst();

        return new ErnieReq(list,
                chatCompletionRequest.stream(),
                chatCompletionRequest.temperature(),
                chatCompletionRequest.topP(),
                chatCompletionRequest.frequencyPenalty(),
                system.map(OpenAiApi.ChatCompletionMessage::content).orElse(null),
                null,
                chatCompletionRequest.maxTokens(),
                chatCompletionRequest.user()
        );
    }

    private OpenAiApi.ChatCompletion convert(ErnieResp ernieResp) {
        if (ernieResp.error_code() != null) {
            throw new RuntimeException(ernieResp.error_msg());
        }
        return new OpenAiApi.ChatCompletion(ernieResp.id(),
                Collections.singletonList(new OpenAiApi.ChatCompletion.Choice(OpenAiApi.ChatCompletionFinishReason.STOP, ernieResp.sentence_id(), new OpenAiApi.ChatCompletionMessage(ernieResp.result(), OpenAiApi.ChatCompletionMessage.Role.ASSISTANT), null)),
                ernieResp.created(),
                this.model(),
                null,
                ernieResp.object(),
                ernieResp.usage() == null ? null : new OpenAiApi.Usage(ernieResp.usage().completion_tokens(), ernieResp.usage().prompt_tokens(), ernieResp.usage().total_tokens())
        );
    }
}
