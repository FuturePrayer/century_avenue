package cn.miketsu.century_avenue.util;

/**
 * @author sihuangwlp
 * @date 2024/6/20
 */
public final class StringUtil {

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
