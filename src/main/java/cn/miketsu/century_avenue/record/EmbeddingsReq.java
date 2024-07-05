package cn.miketsu.century_avenue.record;

/**
 * @author sihuangwlp
 * @date 2024/7/5
 * @since 2.0.0
 */
public record EmbeddingsReq(
        String input,
        String model
) {
    public static EmbeddingsReq changeModel(EmbeddingsReq embeddingsReq, String model) {
        return new EmbeddingsReq(embeddingsReq.input(), model);
    }
}
