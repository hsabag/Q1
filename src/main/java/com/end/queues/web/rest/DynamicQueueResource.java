package com.end.queues.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.end.queues.domain.DynamicQueue;

import com.end.queues.repository.DynamicQueueRepository;
import com.end.queues.repository.search.DynamicQueueSearchRepository;
import com.end.queues.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing DynamicQueue.
 */
@RestController
@RequestMapping("/api")
public class DynamicQueueResource {

    private final Logger log = LoggerFactory.getLogger(DynamicQueueResource.class);

    private static final String ENTITY_NAME = "dynamicQueue";
        
    private final DynamicQueueRepository dynamicQueueRepository;

    private final DynamicQueueSearchRepository dynamicQueueSearchRepository;

    public DynamicQueueResource(DynamicQueueRepository dynamicQueueRepository, DynamicQueueSearchRepository dynamicQueueSearchRepository) {
        this.dynamicQueueRepository = dynamicQueueRepository;
        this.dynamicQueueSearchRepository = dynamicQueueSearchRepository;
    }

    /**
     * POST  /dynamic-queues : Create a new dynamicQueue.
     *
     * @param dynamicQueue the dynamicQueue to create
     * @return the ResponseEntity with status 201 (Created) and with body the new dynamicQueue, or with status 400 (Bad Request) if the dynamicQueue has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/dynamic-queues")
    @Timed
    public ResponseEntity<DynamicQueue> createDynamicQueue(@RequestBody DynamicQueue dynamicQueue) throws URISyntaxException {
        log.debug("REST request to save DynamicQueue : {}", dynamicQueue);
        if (dynamicQueue.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new dynamicQueue cannot already have an ID")).body(null);
        }
        DynamicQueue result = dynamicQueueRepository.save(dynamicQueue);
        dynamicQueueSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/dynamic-queues/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /dynamic-queues : Updates an existing dynamicQueue.
     *
     * @param dynamicQueue the dynamicQueue to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated dynamicQueue,
     * or with status 400 (Bad Request) if the dynamicQueue is not valid,
     * or with status 500 (Internal Server Error) if the dynamicQueue couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/dynamic-queues")
    @Timed
    public ResponseEntity<DynamicQueue> updateDynamicQueue(@RequestBody DynamicQueue dynamicQueue) throws URISyntaxException {
        log.debug("REST request to update DynamicQueue : {}", dynamicQueue);
        if (dynamicQueue.getId() == null) {
            return createDynamicQueue(dynamicQueue);
        }
        DynamicQueue result = dynamicQueueRepository.save(dynamicQueue);
        dynamicQueueSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, dynamicQueue.getId().toString()))
            .body(result);
    }

    /**
     * GET  /dynamic-queues : get all the dynamicQueues.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of dynamicQueues in body
     */
    @GetMapping("/dynamic-queues")
    @Timed
    public List<DynamicQueue> getAllDynamicQueues() {
        log.debug("REST request to get all DynamicQueues");
        List<DynamicQueue> dynamicQueues = dynamicQueueRepository.findAll();
        return dynamicQueues;
    }

    /**
     * GET  /dynamic-queues/:id : get the "id" dynamicQueue.
     *
     * @param id the id of the dynamicQueue to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dynamicQueue, or with status 404 (Not Found)
     */
    @GetMapping("/dynamic-queues/{id}")
    @Timed
    public ResponseEntity<DynamicQueue> getDynamicQueue(@PathVariable Long id) {
        log.debug("REST request to get DynamicQueue : {}", id);
        DynamicQueue dynamicQueue = dynamicQueueRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dynamicQueue));
    }

    /**
     * DELETE  /dynamic-queues/:id : delete the "id" dynamicQueue.
     *
     * @param id the id of the dynamicQueue to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/dynamic-queues/{id}")
    @Timed
    public ResponseEntity<Void> deleteDynamicQueue(@PathVariable Long id) {
        log.debug("REST request to delete DynamicQueue : {}", id);
        dynamicQueueRepository.delete(id);
        dynamicQueueSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/dynamic-queues?query=:query : search for the dynamicQueue corresponding
     * to the query.
     *
     * @param query the query of the dynamicQueue search 
     * @return the result of the search
     */
    @GetMapping("/_search/dynamic-queues")
    @Timed
    public List<DynamicQueue> searchDynamicQueues(@RequestParam String query) {
        log.debug("REST request to search DynamicQueues for query {}", query);
        return StreamSupport
            .stream(dynamicQueueSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }


}
