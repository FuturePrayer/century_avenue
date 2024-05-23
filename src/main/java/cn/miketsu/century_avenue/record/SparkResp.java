package cn.miketsu.century_avenue.record;

import java.util.List;

/**
 * @author sihuangwlp
 * @date 2024/5/23
 */
public record SparkResp(Header header, Payload payload) {

    public record Header(int code, String message, String sid, int status) {
    }

    public record UsageText(int question_tokens, int prompt_tokens, int completion_tokens, int total_tokens) {
    }

    public record ChoiceText(String content, String role, int index) {
    }

    public record Choices(int status, int seq, List<ChoiceText> text) {
    }

    public record Payload(Choices choices, UsageText usage) {
    }
}
