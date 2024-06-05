package cn.miketsu.century_avenue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author sihuangwlp
 * @date 2024/6/5
 * @since 1.0.1
 */
@Component
@ConfigurationProperties(prefix = "glm-4")
public class Glm4Config {

    private String apiKey;

    private List<String> subModels;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public List<String> getSubModels() {
        return subModels;
    }

    public void setSubModels(List<String> subModels) {
        this.subModels = subModels;
    }
}
