package com.end.queues.repository;

import com.end.queues.domain.DynamicQueue;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the DynamicQueue entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DynamicQueueRepository extends JpaRepository<DynamicQueue,Long> {

}
