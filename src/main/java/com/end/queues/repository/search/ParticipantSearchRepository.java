package com.end.queues.repository.search;

import com.end.queues.domain.Participant;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Participant entity.
 */
public interface ParticipantSearchRepository extends ElasticsearchRepository<Participant, Long> {
}
