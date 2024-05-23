package cn.miketsu.century_avenue.service.closed;

import cn.miketsu.century_avenue.record.SparkReq;
import cn.miketsu.century_avenue.record.SparkResp;
import cn.miketsu.century_avenue.service.LlmService;
import cn.miketsu.century_avenue.util.JacksonUtil;
import cn.miketsu.century_avenue.util.SparkAuthUtil;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 讯飞星火Spark3.5 Max
 *
 * @author sihuangwlp
 * @date 2024/5/22
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class Spark35MaxServiceImpl implements LlmService {

    public static final String WEB_SOCKET_STREAMING_COMPLETED = "WebSocket streaming completed";

    private final String path = "/v3.5/chat";

    @Value("${spark35-max.api-secret:}")
    private String apiSecret;

    @Value("${spark35-max.api-key:}")
    private String apiKey;

    @Value("${spark35-max.app_id:}")
    private String appId;

    private final WebSocketClient webSocketClient;

    @Autowired
    public Spark35MaxServiceImpl(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public String model() {
        return "spark35-max";
    }

    @Override
    public Boolean available() {
        return apiSecret != null
                && !apiSecret.isBlank()
                && apiKey != null
                && !apiKey.isBlank()
                && appId != null
                && !appId.isBlank();
    }

    @Override
    public Flux<OpenAiApi.ChatCompletionChunk> stream(OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the steam property to true.");

        SparkWebSocketHandler handler = new SparkWebSocketHandler(chatRequest);
        String authedUrl = SparkAuthUtil.getAuthedUrl(path, apiSecret, apiKey);
        this.webSocketClient
                .execute(URI.create(authedUrl), handler)
                .doOnError(e -> handler.getSink().tryEmitError(new RuntimeException("Websocket connection failed", e)))
                .subscribe();

        Map<String, String> temp = new HashMap<>();

        return handler.getSink().asFlux().map(content -> {
            //如果websocket请求结束，返回结束标识
            if (WEB_SOCKET_STREAMING_COMPLETED.equals(content)) {
                return new OpenAiApi.ChatCompletionChunk(
                        temp.get("sid"),
                        Collections.singletonList(new OpenAiApi.ChatCompletionChunk.ChunkChoice(
                                OpenAiApi.ChatCompletionFinishReason.STOP,
                                0,
                                new OpenAiApi.ChatCompletionMessage(null, null),
                                null)),
                        null,
                        null,
                        null,
                        null);
            }
            //未结束，进行报文格式转换
            SparkResp sparkResp = ModelOptionsUtils.jsonToObject(content, SparkResp.class);
            temp.put("sid", sparkResp.header().sid());
            return new OpenAiApi.ChatCompletionChunk(sparkResp.header().sid(),
                    Collections.singletonList(new OpenAiApi.ChatCompletionChunk.ChunkChoice(
                            null,
                            sparkResp.payload().choices().text().get(0).index(),
                            new OpenAiApi.ChatCompletionMessage(sparkResp.payload().choices().text().get(0).content(), OpenAiApi.ChatCompletionMessage.Role.valueOf(sparkResp.payload().choices().text().get(0).role().toUpperCase())),
                            null
                    )),
                    System.currentTimeMillis() / 1000,
                    this.model(),
                    null,
                    "chat.completion.chunk");
        });
    }

    @Override
    public Flux<OpenAiApi.ChatCompletion> call(OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream() == null || !chatRequest.stream(), "Request must set the steam property to false.");

        SparkWebSocketHandler handler = new SparkWebSocketHandler(chatRequest);
        String authedUrl = SparkAuthUtil.getAuthedUrl(path, apiSecret, apiKey);

        Map<String, String> temp = new HashMap<>();

        return Flux.create(sink -> {
            Disposable disposable = this.webSocketClient
                    .execute(URI.create(authedUrl), handler)
                    .doOnError(e -> sink.error(new RuntimeException("Websocket connection failed", e)))
                    .subscribe();

            handler.getSink().asFlux()
                    .filter(content -> !WEB_SOCKET_STREAMING_COMPLETED.equals(content))
                    .map(sparkResp -> {
                        SparkResp resp = ModelOptionsUtils.jsonToObject(sparkResp, SparkResp.class);
                        //保存sid
                        temp.putIfAbsent("sid", resp.header().sid());

                        //保存usage信息
                        SparkResp.UsageText usage = resp.payload().usage();
                        temp.put("completionTokens", String.valueOf(usage.completion_tokens()));
                        temp.put("promptTokens", String.valueOf(usage.prompt_tokens()));
                        temp.put("totalTokens", String.valueOf(usage.total_tokens()));

                        return resp.payload().choices().text().getFirst().content();
                    })
                    .reduce("", (acc, curr) -> acc + curr)
                    .subscribe(content -> {
                        sink.next(new OpenAiApi.ChatCompletion(
                                temp.get("sid"),
                                Collections.singletonList(new OpenAiApi.ChatCompletion.Choice(
                                                OpenAiApi.ChatCompletionFinishReason.STOP,
                                                0,
                                                new OpenAiApi.ChatCompletionMessage(content, OpenAiApi.ChatCompletionMessage.Role.ASSISTANT)
                                                , null
                                        )
                                ),
                                System.currentTimeMillis() / 1000,
                                this.model(),
                                null,
                                null,
                                new OpenAiApi.Usage(
                                        Integer.valueOf(temp.get("completionTokens")),
                                        Integer.valueOf(temp.get("promptTokens")),
                                        Integer.valueOf(temp.get("totalTokens"))
                                )
                        ));
                        sink.complete();
                    }, sink::error); // Propagate errors to the sink

            sink.onDispose(disposable); // 当Flux被取消订阅时关闭WebSocket连接
        });
    }

    public class SparkWebSocketHandler implements WebSocketHandler {

        private final Sinks.Many<String> sink = Sinks.many().replay().latest();

        private final OpenAiApi.ChatCompletionRequest chatRequest;

        public SparkWebSocketHandler(OpenAiApi.ChatCompletionRequest chatRequest) {
            this.chatRequest = chatRequest;
        }

        @Override
        public Mono<Void> handle(WebSocketSession session) {
            SparkReq.Header header = new SparkReq.Header(appId, chatRequest.user());
            SparkReq.ChatParameter chatParameter = new SparkReq.ChatParameter(new SparkReq.Chat("general", chatRequest.temperature(), chatRequest.maxTokens()));
            SparkReq.Payload payload = new SparkReq.Payload(new SparkReq.Message(chatRequest.messages().stream().map(message -> new SparkReq.MessageText(message.role().name().toLowerCase(), message.content())).collect(Collectors.toList())));
            SparkReq sparkReq = new SparkReq(header, chatParameter, payload);

            return session.send(Mono.just(session.textMessage(JacksonUtil.tryParse(sparkReq))))
                    .thenMany(session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnComplete(() -> sink.tryEmitNext(WEB_SOCKET_STREAMING_COMPLETED))
                            .doOnNext(sink::tryEmitNext)
                            .doOnComplete(sink::tryEmitComplete)
                    )
                    .then();
        }

        public Sinks.Many<String> getSink() {
            return sink;
        }
    }
}
