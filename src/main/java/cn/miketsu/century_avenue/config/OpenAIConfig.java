package cn.miketsu.century_avenue.config;

import cn.miketsu.century_avenue.record.OpenaiCfg;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author sihuangwlp
 * @date 2024/6/12
 * @since 1.0.2
 */
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfig {
    
    private List<OpenaiCfg> models;

    public List<OpenaiCfg> getModels() {
        return models;
    }

    public void setModels(List<OpenaiCfg> models) {
        this.models = models;
    }
}
