package cn.miketsu.century_avenue.service.completions;

import org.springframework.ai.openai.api.OpenAiApi;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;

/**
 * @author sihuangwlp
 * @date 2024/5/22
 */
public interface LlmService {

    /**
     * 模型名称
     *
     * @return 模型名称
     * @author sihuangwlp
     * @date 2024/5/23
     * @since 0.0.1-SNAPSHOT
     */
    String model();

    /**
     * 获取子模型
     *
     * @return 子模型名称集合
     * @author sihuangwlp
     * @date 2024/6/5
     * @since 1.0.1
     */
    default Collection<String> subModels() {
        return Collections.emptyList();
    }

    /**
     * 本模型是否可用<br/>
     * 一般判断规则：对接必须参数不为空
     *
     * @return 是否可用
     * @author sihuangwlp
     * @date 2024/5/23
     * @since 0.0.1-SNAPSHOT
     */
    Boolean available();

    /**
     * 模型流式调用
     *
     * @param chatRequest openai风格的请求参数
     * @return openai风格的流式响应参数
     * @author sihuangwlp
     * @date 2024/5/23
     * @since 0.0.1-SNAPSHOT
     */
    Flux<OpenAiApi.ChatCompletionChunk> stream(OpenAiApi.ChatCompletionRequest chatRequest);

    /**
     * 模型调用
     *
     * @param chatRequest openai风格的请求参数
     * @return openai风格的非流式响应参数
     * @author sihuangwlp
     * @date 2024/5/23
     * @since 0.0.1-SNAPSHOT
     */
    Flux<OpenAiApi.ChatCompletion> call(OpenAiApi.ChatCompletionRequest chatRequest);
}
