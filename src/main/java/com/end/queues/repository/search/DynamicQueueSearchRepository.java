package com.end.queues.repository.search;

import com.end.queues.domain.DynamicQueue;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the DynamicQueue entity.
 */
public interface DynamicQueueSearchRepository extends ElasticsearchRepository<DynamicQueue, Long> {
}
