package cn.miketsu.century_avenue.service.completions.closed;

import cn.miketsu.century_avenue.config.CenturyAvenueConfig;
import cn.miketsu.century_avenue.function.ErnieConvert;
import cn.miketsu.century_avenue.record.ErnieReq;
import cn.miketsu.century_avenue.record.ErnieResp;
import cn.miketsu.century_avenue.util.HttpUtil;
import cn.miketsu.century_avenue.util.JacksonUtil;
import cn.miketsu.century_avenue.util.StringUtil;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.*;

/**
 * ERNIE-Speed-8K
 *
 * @author sihuangwlp
 * @date 2024/5/26
 * @since 0.0.4-SNAPSHOT
 */
@Service
public class ErnieSpeedServiceImpl extends ErnieConvert {

    @Autowired
    private CenturyAvenueConfig centuryAvenueConfig;

    private static final String authUrl = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=%s&client_secret=%s";

    private static final String baseUrl = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie_speed?access_token=%s";

    @Override
    public String model() {
        return "ernie-speed";
    }

    @Override
    public Boolean available() {
        return centuryAvenueConfig.ernieSpeed() != null
                && StringUtil.isNotBlank(centuryAvenueConfig.ernieSpeed().apiKey())
                && StringUtil.isNotBlank(centuryAvenueConfig.ernieSpeed().secretKey());
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
                .uri(String.format(baseUrl, getAccessToken(authUrl, centuryAvenueConfig.ernieSpeed().apiKey(), centuryAvenueConfig.ernieSpeed().secretKey())))
                .header("Content-Type", "application/json")
                .body(Mono.just(convertReq(chatRequest)), ErnieReq.class)
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

        return Flux.just(
                HttpUtil.post()
                        .url(String.format(baseUrl, getAccessToken(authUrl, centuryAvenueConfig.ernieSpeed().apiKey(), centuryAvenueConfig.ernieSpeed().secretKey())))
                        .header("Content-Type", "application/json")
                        .body(JacksonUtil.tryParse(convertReq(chatRequest)))
                        .resp(ErnieResp.class, this::convertResp)
        );
    }
}
