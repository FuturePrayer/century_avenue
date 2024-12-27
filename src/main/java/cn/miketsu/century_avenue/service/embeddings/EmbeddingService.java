package cn.miketsu.century_avenue.service.embeddings;

import cn.miketsu.century_avenue.record.EmbeddingsReq;
import cn.miketsu.century_avenue.record.EmbeddingsResp;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;

/**
 * @author sihuangwlp
 * @date 2024/7/5
 */
public interface EmbeddingService {
    Boolean available();

    Collection<String> model();

    EmbeddingsResp embeddings(EmbeddingsReq embeddingsReq);
}
