package cn.miketsu.century_avenue.service.embeddings.closed;

import cn.miketsu.century_avenue.config.CenturyAvenueConfig;
import cn.miketsu.century_avenue.config.CenturyAvenueConfig.CenturyAvenue.Embedding.Zhipu;
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
public class ZhipuEmbeddingImpl implements EmbeddingService {

    private final CenturyAvenueConfig centuryAvenueConfig;

    private Map<String, Zhipu> models;

    @Autowired
    public ZhipuEmbeddingImpl(CenturyAvenueConfig centuryAvenueConfig) {
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
        Zhipu openAI = models.get(embeddingsReq.model());
        OpenAiUtil openAiApi = new OpenAiUtil("https://open.bigmodel.cn/api/paas/v4", openAI.apiKey());
        ResponseEntity<EmbeddingsResp> embeddings = openAiApi.embeddings(embeddingsReq);
        return embeddings.getBody();
    }

    private void initCache() {
        if (models == null && centuryAvenueConfig.centuryAvenue().embedding().zhipu() != null) {
            models = centuryAvenueConfig.centuryAvenue().embedding().zhipu().stream()
                    .filter(o -> StringUtil.isNotBlank(o.apiKey()))
                    .collect(Collectors.toMap(CenturyAvenueConfig.CenturyAvenue.Embedding.Zhipu::model, o -> o));
        }
    }
}
