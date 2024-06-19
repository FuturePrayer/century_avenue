package cn.miketsu.century_avenue.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;

import java.util.List;
import java.util.Map;

/**
 * @author sihuangwlp
 * @date 2024/6/19
 * @since 2.0.0
 */
@ConfigurationProperties
@ConfigurationPropertiesBinding
public record CenturyAvenueConfig(@JsonProperty("api-key") String apiKey,
                                  OpenAI openai,
                                  @JsonProperty("glm-4") GLM4 glm4,
                                  @JsonProperty("spark-lite") SparkLite sparkLite,
                                  @JsonProperty("spark-pro") SparkPro sparkPro,
                                  @JsonProperty("spark35-max") Spark35Max spark35Max,
                                  @JsonProperty("qwen-long") QwenLong qwenLong,
                                  @JsonProperty("ernie-tiny-8k") ErnieTiny8k ernieTiny8k,
                                  @JsonProperty("ernie-lite-8k") ErnieLite8k ernieLite8k,
                                  @JsonProperty("ernie-speed") ErnieSpeed ernieSpeed,
                                  @JsonProperty("ernie-speed-128k") ErnieSpeed128k ernieSpeed128k,
                                  @JsonProperty("ernie-3.5-8k-preview") Ernie358kPreview ernie358kPreview,
                                  @JsonProperty("ernie-4.0-8k-preview") Ernie408kPreview ernie408kPreview,
                                  @JsonProperty("century-avenue") CenturyAvenue centuryAvenue) {


    public record OpenAI(List<Model> models) {

        public record Model(String apiKey,
                            String model,
                            String baseUrl) {
        }
    }

    public record GLM4(@JsonProperty("api-key") String apiKey,
                       List<String> subModels) {
    }

    public record SparkLite(@JsonProperty("app_id") String appId,
                            @JsonProperty("api-secret") String apiSecret,
                            @JsonProperty("api-key") String apiKey) {
    }

    public record SparkPro(@JsonProperty("app_id") String appId,
                           @JsonProperty("api-secret") String apiSecret,
                           @JsonProperty("api-key") String apiKey) {
    }

    public record Spark35Max(@JsonProperty("app_id") String appId,
                             @JsonProperty("api-secret") String apiSecret,
                             @JsonProperty("api-key") String apiKey) {
    }

    public record QwenLong(@JsonProperty("api-key") String apiKey) {
    }

    public record ErnieTiny8k(@JsonProperty("api-key") String apiKey,
                              @JsonProperty("secret-key") String secretKey) {

    }

    public record ErnieLite8k(@JsonProperty("api-key") String apiKey,
                              @JsonProperty("secret-key") String secretKey) {
    }

    public record ErnieSpeed(@JsonProperty("api-key") String apiKey,
                             @JsonProperty("secret-key") String secretKey) {
    }

    public record ErnieSpeed128k(@JsonProperty("api-key") String apiKey,
                                 @JsonProperty("secret-key") String secretKey) {
    }

    public record Ernie358kPreview(@JsonProperty("api-key") String apiKey,
                                   @JsonProperty("secret-key") String secretKey) {
    }

    public record Ernie408kPreview(@JsonProperty("api-key") String apiKey,
                                   @JsonProperty("secret-key") String secretKey) {
    }

    public record CenturyAvenue(@JsonProperty("model-mapping") Map<String, String> modelMapping) {
    }

}
