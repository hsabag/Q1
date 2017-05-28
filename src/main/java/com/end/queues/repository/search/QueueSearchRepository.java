package com.end.queues.repository.search;

import com.end.queues.domain.Queue;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Queue entity.
 */
public interface QueueSearchRepository extends ElasticsearchRepository<Queue, Long> {
}
