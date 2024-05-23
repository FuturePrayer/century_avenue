package cn.miketsu.century_avenue.record;

import java.util.List;

/**
 * @author sihuangwlp
 * @date 2024/5/23
 * @since 0.0.1-SNAPSHOT
 */
public record SparkReq(Header header, ChatParameter parameter, Payload payload) {

    public record Header(String app_id, String uid) {
    }

    public record ChatParameter(Chat chat) {
    }

    public record Chat(String domain, Float temperature, Integer max_tokens, Integer top_k, Integer chat_id) {
        public Chat(String domain, Float temperature, Integer max_tokens) {
            this(domain, temperature, max_tokens, null, null);
        }
    }

    public record Payload(Message message) {
    }

    public record Message(List<MessageText> text) {
    }

    public record MessageText(String role, String content) {
    }
}
