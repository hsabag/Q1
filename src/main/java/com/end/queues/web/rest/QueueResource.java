package com.end.queues.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.end.queues.domain.Queue;

import com.end.queues.repository.QueueRepository;
import com.end.queues.repository.search.QueueSearchRepository;
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
 * REST controller for managing Queue.
 */
@RestController
@RequestMapping("/api")
public class QueueResource {

    private final Logger log = LoggerFactory.getLogger(QueueResource.class);

    private static final String ENTITY_NAME = "queue";
        
    private final QueueRepository queueRepository;

    private final QueueSearchRepository queueSearchRepository;

    public QueueResource(QueueRepository queueRepository, QueueSearchRepository queueSearchRepository) {
        this.queueRepository = queueRepository;
        this.queueSearchRepository = queueSearchRepository;
    }

    /**
     * POST  /queues : Create a new queue.
     *
     * @param queue the queue to create
     * @return the ResponseEntity with status 201 (Created) and with body the new queue, or with status 400 (Bad Request) if the queue has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/queues")
    @Timed
    public ResponseEntity<Queue> createQueue(@RequestBody Queue queue) throws URISyntaxException {
        log.debug("REST request to save Queue : {}", queue);
        if (queue.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new queue cannot already have an ID")).body(null);
        }
        Queue result = queueRepository.save(queue);
        queueSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/queues/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /queues : Updates an existing queue.
     *
     * @param queue the queue to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated queue,
     * or with status 400 (Bad Request) if the queue is not valid,
     * or with status 500 (Internal Server Error) if the queue couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/queues")
    @Timed
    public ResponseEntity<Queue> updateQueue(@RequestBody Queue queue) throws URISyntaxException {
        log.debug("REST request to update Queue : {}", queue);
        if (queue.getId() == null) {
            return createQueue(queue);
        }
        Queue result = queueRepository.save(queue);
        queueSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, queue.getId().toString()))
            .body(result);
    }

    /**
     * GET  /queues : get all the queues.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of queues in body
     */
    @GetMapping("/queues")
    @Timed
    public List<Queue> getAllQueues() {
        log.debug("REST request to get all Queues");
        List<Queue> queues = queueRepository.findAll();
        return queues;
    }

    /**
     * GET  /queues/:id : get the "id" queue.
     *
     * @param id the id of the queue to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the queue, or with status 404 (Not Found)
     */
    @GetMapping("/queues/{id}")
    @Timed
    public ResponseEntity<Queue> getQueue(@PathVariable Long id) {
        log.debug("REST request to get Queue : {}", id);
        Queue queue = queueRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(queue));
    }

    /**
     * DELETE  /queues/:id : delete the "id" queue.
     *
     * @param id the id of the queue to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/queues/{id}")
    @Timed
    public ResponseEntity<Void> deleteQueue(@PathVariable Long id) {
        log.debug("REST request to delete Queue : {}", id);
        queueRepository.delete(id);
        queueSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/queues?query=:query : search for the queue corresponding
     * to the query.
     *
     * @param query the query of the queue search 
     * @return the result of the search
     */
    @GetMapping("/_search/queues")
    @Timed
    public List<Queue> searchQueues(@RequestParam String query) {
        log.debug("REST request to search Queues for query {}", query);
        return StreamSupport
            .stream(queueSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }


}
