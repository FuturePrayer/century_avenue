package cn.miketsu.century_avenue.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.json.JsonParseException;

import java.util.concurrent.Callable;

/**
 * @author sihuangwlp
 * @date 2024/5/22
 */
public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String tryParse(Object object) {
        return tryParse(object, JacksonException.class);
    }

    public static String tryParse(Object object, Class<? extends Exception> check) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            if (check.isInstance(e)) {
                throw new JsonParseException(e);
            }
            throw new IllegalStateException(e);
        }
    }
}
