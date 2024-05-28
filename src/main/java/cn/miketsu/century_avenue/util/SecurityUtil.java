package cn.miketsu.century_avenue.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author sihuangwlp
 * @date 2024/5/22
 * @since 0.0.1-SNAPSHOT
 */
public final class SecurityUtil {

    /**
     * Base64编码
     *
     * @param data 待编码内容
     * @return Base64编码的字符串
     */
    public static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 使用HMAC-SHA256算法获取字符串摘要。
     *
     * @param secretKey 密钥
     * @param data      要摘要的数据
     * @return Base64编码的摘要字符串
     */
    public static String hmacSha256(String secretKey, String data) {
        try {
            // 创建HMAC-SHA256算法的Mac实例
            Mac mac = Mac.getInstance("HmacSHA256");

            // 使用密钥初始化Mac
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            // 执行摘要操作
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 将摘要结果转换为Base64编码的字符串
            return base64Encode(digest);
        } catch (Exception e) {
            throw new RuntimeException("Error while generating HMAC-SHA256 hash", e);
        }
    }
}
