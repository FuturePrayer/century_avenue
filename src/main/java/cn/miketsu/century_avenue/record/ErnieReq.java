package cn.miketsu.century_avenue.record;

import java.util.List;

/**
 * 百度ERNIE大模型请求参数
 *
 * @author sihuangwlp
 * @date 2024/5/26
 * @since 0.0.4-SNAPSHOT
 */
public record ErnieReq(
        List<Message> messages,
        Boolean stream,
        Float temperature,
        Float top_p,
        Float penalty_score,
        String system,
        List<String> stop,
        Integer max_output_tokens,
        String user_id
) {

    public record Message(String role, String content) {
    }
}
