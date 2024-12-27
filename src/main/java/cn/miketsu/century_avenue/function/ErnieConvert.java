package cn.miketsu.century_avenue.function;

import cn.miketsu.century_avenue.record.ErnieAuthResp;
import cn.miketsu.century_avenue.record.ErnieReq;
import cn.miketsu.century_avenue.record.ErnieResp;
import cn.miketsu.century_avenue.service.completions.LlmService;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 百度系列大模型抽象类
 *
 * @author sihuangwlp
 * @date 2024/5/28
 * @since 1.0.0
 */
public abstract class ErnieConvert implements LlmService {

    /**
     * 获取鉴权access token
     *
     * @param authUrl   鉴权接口路径
     * @param apiKey    apiKey
     * @param secretKey secretKey
     * @return access token
     * @author sihuangwlp
     * @date 2024/5/28
     * @since 1.0.0
     */
    protected String getAccessToken(String authUrl, String apiKey, String secretKey) {
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

    /**
     * 转换请求参数
     *
     * @param chatCompletionRequest openai风格的请求参数
     * @return ernie风格请求参数
     * @author sihuangwlp
     * @date 2024/5/28
     * @since 1.0.0
     */
    protected ErnieReq convertReq(OpenAiApi.ChatCompletionRequest chatCompletionRequest) {
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

    /**
     * 转换响应参数
     *
     * @param ernieResp ernie风格响应参数
     * @return openai风格响应参数
     * @author sihuangwlp
     * @date 2024/5/28
     * @since 1.0.0
     */
    protected OpenAiApi.ChatCompletion convertResp(ErnieResp ernieResp) {
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
