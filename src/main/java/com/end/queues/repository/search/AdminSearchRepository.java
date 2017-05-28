package com.end.queues.repository.search;

import com.end.queues.domain.Admin;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Admin entity.
 */
public interface AdminSearchRepository extends ElasticsearchRepository<Admin, Long> {
}
