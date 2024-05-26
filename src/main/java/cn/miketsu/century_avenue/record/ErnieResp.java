package cn.miketsu.century_avenue.record;

/**
 * 百度ERNIE大模型响应参数
 *
 * @author sihuangwlp
 * @date 2024/5/26
 * @since 0.0.4-SNAPSHOT
 */
public record ErnieResp(
        String id,
        String object,
        Long created,
        Integer sentence_id,
        Boolean is_end,
        Boolean is_truncated,
        String result,
        Boolean need_clear_history,
        Integer ban_round,
        Usage usage,
        Integer error_code,
        String error_msg
) {

    public record Usage(Integer prompt_tokens, Integer completion_tokens, Integer total_tokens) {
    }
}
