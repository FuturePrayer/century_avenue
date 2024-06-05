package cn.miketsu.century_avenue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * @author sihuangwlp
 * @date 2024/5/26
 * @since 0.0.4-SNAPSHOT
 */
@Component
@ConfigurationProperties(prefix = "century-avenue")
public class DockingConfig {

    private Map<String, String> modelMapping;

    public Map<String, String> getModelMapping() {
        return modelMapping == null ? Collections.emptyMap() : modelMapping;
    }

    @ConfigurationProperties(prefix = "century-avenue.model-mapping")
    public void setModelMapping(Map<String, String> modelMapping) {
        this.modelMapping = modelMapping;
    }
}
