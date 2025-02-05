package cn.miketsu.century_avenue.service.completions.closed;

import cn.miketsu.century_avenue.config.CenturyAvenueConfig;
import cn.miketsu.century_avenue.record.SparkReq;
import cn.miketsu.century_avenue.record.SparkResp;
import cn.miketsu.century_avenue.service.completions.LlmService;
import cn.miketsu.century_avenue.util.JacksonUtil;
import cn.miketsu.century_avenue.util.SparkAuthUtil;
import cn.miketsu.century_avenue.util.StringUtil;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

/**
 * 讯飞星火Spark4 Ultra
 *
 * @author sihuangwlp
 * @date 2024/6/27
 * @since 2.0.0
 */
@Service
public class Spark4UltraServiceImpl implements LlmService {

    public static final String WEB_SOCKET_STREAMING_COMPLETED = "WebSocket streaming completed";

    private final String path = "/v4.0/chat";

    @Autowired
    private CenturyAvenueConfig centuryAvenueConfig;

    private final WebSocketClient webSocketClient;

    @Autowired
    public Spark4UltraServiceImpl(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public String model() {
        return "spark4-ultra";
    }

    @Override
    public Boolean available() {
        return centuryAvenueConfig.spark4Ultra() != null
                && StringUtil.isNotBlank(centuryAvenueConfig.spark4Ultra().appId())
                && StringUtil.isNotBlank(centuryAvenueConfig.spark4Ultra().apiSecret())
                && StringUtil.isNotBlank(centuryAvenueConfig.spark4Ultra().apiKey());
    }

    @Override
    public Flux<OpenAiApi.ChatCompletionChunk> stream(OpenAiApi.ChatCompletionRequest chatRequest) {
        Assert.notNull(chatRequest, "The request body can not be null.");
        Assert.isTrue(chatRequest.stream(), "Request must set the steam property to true.");

        SparkWebSocketHandler handler = new SparkWebSocketHandler(chatRequest);
        String authedUrl = SparkAuthUtil.getAuthedUrl(path, centuryAvenueConfig.spark4Ultra().apiSecret(), centuryAvenueConfig.spark4Ultra().apiKey());
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
        String authedUrl = SparkAuthUtil.getAuthedUrl(path, centuryAvenueConfig.spark4Ultra().apiSecret(), centuryAvenueConfig.spark4Ultra().apiKey());

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
                        if (usage != null) {
                            temp.put("completionTokens", String.valueOf(usage.completion_tokens()));
                            temp.put("promptTokens", String.valueOf(usage.prompt_tokens()));
                            temp.put("totalTokens", String.valueOf(usage.total_tokens()));
                        }

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
                                temp.containsKey("completionTokens") && temp.containsKey("promptTokens") && temp.containsKey("totalTokens")
                                        ? new OpenAiApi.Usage(
                                        Integer.valueOf(temp.get("completionTokens")),
                                        Integer.valueOf(temp.get("promptTokens")),
                                        Integer.valueOf(temp.get("totalTokens"))
                                )
                                        : null
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
            SparkReq.Header header = new SparkReq.Header(centuryAvenueConfig.spark4Ultra().appId(), chatRequest.user());
            SparkReq.ChatParameter chatParameter = new SparkReq.ChatParameter(new SparkReq.Chat("4.0Ultra", chatRequest.temperature(), chatRequest.maxTokens()));
            SparkReq.Payload payload = new SparkReq.Payload(new SparkReq.Message(chatRequest.messages().stream().map(message -> new SparkReq.MessageText(message.role().name().toLowerCase(), message.content())).collect(Collectors.toList())));
            SparkReq sparkReq = new SparkReq(header, chatParameter, payload);

            return session.send(Mono.just(session.textMessage(JacksonUtil.tryParse(sparkReq))))
                    .thenMany(session.receive()
                            .map(WebSocketMessage::getPayloadAsText)
                            .doOnComplete(() -> sink.tryEmitNext(WEB_SOCKET_STREAMING_COMPLETED))
                            .doOnEach(System.out::println)
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
