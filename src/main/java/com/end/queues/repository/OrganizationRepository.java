package com.end.queues.repository;

import com.end.queues.domain.Organization;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Organization entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OrganizationRepository extends JpaRepository<Organization,Long> {

}
