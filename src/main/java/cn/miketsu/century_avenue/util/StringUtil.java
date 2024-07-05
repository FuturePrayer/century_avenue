package cn.miketsu.century_avenue.util;

/**
 * @author sihuangwlp
 * @date 2024/6/20
 * @since 2.0.0
 */
public final class StringUtil {

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
    
    public static String defaultIfBlank(String str,String defaultStr){
        return isBlank(str) ? defaultStr : str;
    }
}
