package cn.miketsu.century_avenue.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 讯飞星火大模型鉴权工具类
 *
 * @author sihuangwlp
 * @date 2024/5/22
 * @since 0.0.1-SNAPSHOT
 */
public class SparkAuthUtil {

    private static final String tmp_template = """
            host: spark-api.xf-yun.com
            date: %s
            GET %s HTTP/1.1""";

    private static final String authorization_origin_template = "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"";

    private static final String base_url_template = "wss://spark-api.xf-yun.com%s?%s";

    private static final String authorization_template = "authorization=%s&date=%s&host=spark-api.xf-yun.com";

    private static final SimpleDateFormat sdf;

    static {
        // 创建SimpleDateFormat对象，并指定日期格式
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

        // 设置时区为GMT
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * 获取经过鉴权处理后的url
     *
     * @param path      要请求的模型的路径，如：/v1.1/chat
     * @param apiSecret APISecret参数
     * @param apiKey    APIKey参数
     * @return 经过鉴权处理后的url
     */
    public static String getAuthedUrl(String path, String apiSecret, String apiKey) {
        String date = sdf.format(new Date());
        String tmp = String.format(tmp_template, date, path);
        String signature = SecurityUtil.hmacSha256(apiSecret, tmp);
        String authorization_origin = String.format(authorization_origin_template, apiKey, signature);
        return String.format(base_url_template, path, String.format(authorization_template, URLEncoder.encode(SecurityUtil.base64Encode(authorization_origin.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8), URLEncoder.encode(date, StandardCharsets.UTF_8)));
    }
}
