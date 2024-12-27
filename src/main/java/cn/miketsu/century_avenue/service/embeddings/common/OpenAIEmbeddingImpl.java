package cn.miketsu.century_avenue.service.embeddings.common;

import cn.miketsu.century_avenue.config.CenturyAvenueConfig;
import cn.miketsu.century_avenue.config.CenturyAvenueConfig.CenturyAvenue.Embedding.OpenAI;
import cn.miketsu.century_avenue.record.EmbeddingsReq;
import cn.miketsu.century_avenue.record.EmbeddingsResp;
import cn.miketsu.century_avenue.service.embeddings.EmbeddingService;
import cn.miketsu.century_avenue.util.OpenAiUtil;
import cn.miketsu.century_avenue.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author sihuangwlp
 * @date 2024/7/5
 */
@Service
public class OpenAIEmbeddingImpl implements EmbeddingService {

    private final CenturyAvenueConfig centuryAvenueConfig;

    private final String defaultBaseUrl = "https://api.openai.com";

    private Map<String, OpenAI> models;

    @Autowired
    public OpenAIEmbeddingImpl(CenturyAvenueConfig centuryAvenueConfig) {
        this.centuryAvenueConfig = centuryAvenueConfig;
    }

    @Override
    public Boolean available() {
        initCache();
        return models != null && !models.isEmpty();
    }

    @Override
    public Collection<String> model() {
        initCache();
        return models.keySet();
    }

    @Override
    public EmbeddingsResp embeddings(EmbeddingsReq embeddingsReq) {
        initCache();
        if (!models.containsKey(embeddingsReq.model())) {
            throw new RuntimeException("model not found");
        }
        OpenAI openAI = models.get(embeddingsReq.model());
        OpenAiUtil openAiApi = new OpenAiUtil(openAI.baseUrl(), openAI.apiKey());
        ResponseEntity<EmbeddingsResp> embeddings = openAiApi.embeddings(embeddingsReq);
        return embeddings.getBody();
    }

    private void initCache() {
        if (this.models == null && centuryAvenueConfig.centuryAvenue().embedding().openai() != null) {
            models = centuryAvenueConfig.centuryAvenue().embedding().openai().stream()
                    .filter(o -> StringUtil.isNotBlank(o.apiKey()) && StringUtil.isNotBlank(o.model()))
                    .map(o -> new OpenAI(o.model(), o.apiKey(), StringUtil.defaultIfBlank(o.baseUrl(), defaultBaseUrl)))
                    .collect(Collectors.toMap(OpenAI::model, o -> o));
        }
    }

}
