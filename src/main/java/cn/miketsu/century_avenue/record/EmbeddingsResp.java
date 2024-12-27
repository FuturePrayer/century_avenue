package cn.miketsu.century_avenue.record;

import java.util.List;

/**
 * @author sihuangwlp
 * @date 2024/7/5
 * @since 2.0.0
 */
public record EmbeddingsResp(
        List<Datum> data,
        String model,
        String object,
        Usage usage
) {
    public record Datum(
            List<Double> embedding,
            Long index,
            String object
    ) {
    }

    public record Usage(
            Long promptTokens,
            Long totalTokens
    ) {
    }
}
