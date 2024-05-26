package cn.miketsu.century_avenue.record;

/**
 * 百度ERNIE大模型鉴权接口响应参数
 *
 * @author sihuangwlp
 * @date 2024/5/26
 * @since 0.0.4-SNAPSHOT
 */
public record ErnieAuthResp(String access_token,
                            Integer expires_in,
                            String error,
                            String error_description) {
}
