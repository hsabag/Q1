package com.end.queues.web.rest;

import com.end.queues.Q1App;

import com.end.queues.domain.Queue;
import com.end.queues.repository.QueueRepository;
import com.end.queues.repository.search.QueueSearchRepository;
import com.end.queues.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the QueueResource REST controller.
 *
 * @see QueueResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Q1App.class)
public class QueueResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_DESC = "AAAAAAAAAA";
    private static final String UPDATED_DESC = "BBBBBBBBBB";

    private static final Integer DEFAULT_MAX_CAPACITY = 1;
    private static final Integer UPDATED_MAX_CAPACITY = 2;

    private static final String DEFAULT_SITE = "AAAAAAAAAA";
    private static final String UPDATED_SITE = "BBBBBBBBBB";

    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private QueueSearchRepository queueSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restQueueMockMvc;

    private Queue queue;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        QueueResource queueResource = new QueueResource(queueRepository, queueSearchRepository);
        this.restQueueMockMvc = MockMvcBuilders.standaloneSetup(queueResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Queue createEntity(EntityManager em) {
        Queue queue = new Queue()
            .name(DEFAULT_NAME)
            .desc(DEFAULT_DESC)
            .maxCapacity(DEFAULT_MAX_CAPACITY)
            .site(DEFAULT_SITE);
        return queue;
    }

    @Before
    public void initTest() {
        queueSearchRepository.deleteAll();
        queue = createEntity(em);
    }

    @Test
    @Transactional
    public void createQueue() throws Exception {
        int databaseSizeBeforeCreate = queueRepository.findAll().size();

        // Create the Queue
        restQueueMockMvc.perform(post("/api/queues")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(queue)))
            .andExpect(status().isCreated());

        // Validate the Queue in the database
        List<Queue> queueList = queueRepository.findAll();
        assertThat(queueList).hasSize(databaseSizeBeforeCreate + 1);
        Queue testQueue = queueList.get(queueList.size() - 1);
        assertThat(testQueue.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testQueue.getDesc()).isEqualTo(DEFAULT_DESC);
        assertThat(testQueue.getMaxCapacity()).isEqualTo(DEFAULT_MAX_CAPACITY);
        assertThat(testQueue.getSite()).isEqualTo(DEFAULT_SITE);

        // Validate the Queue in Elasticsearch
        Queue queueEs = queueSearchRepository.findOne(testQueue.getId());
        assertThat(queueEs).isEqualToComparingFieldByField(testQueue);
    }

    @Test
    @Transactional
    public void createQueueWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = queueRepository.findAll().size();

        // Create the Queue with an existing ID
        queue.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restQueueMockMvc.perform(post("/api/queues")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(queue)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Queue> queueList = queueRepository.findAll();
        assertThat(queueList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllQueues() throws Exception {
        // Initialize the database
        queueRepository.saveAndFlush(queue);

        // Get all the queueList
        restQueueMockMvc.perform(get("/api/queues?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(queue.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].desc").value(hasItem(DEFAULT_DESC.toString())))
            .andExpect(jsonPath("$.[*].maxCapacity").value(hasItem(DEFAULT_MAX_CAPACITY)))
            .andExpect(jsonPath("$.[*].site").value(hasItem(DEFAULT_SITE.toString())));
    }

    @Test
    @Transactional
    public void getQueue() throws Exception {
        // Initialize the database
        queueRepository.saveAndFlush(queue);

        // Get the queue
        restQueueMockMvc.perform(get("/api/queues/{id}", queue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(queue.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.desc").value(DEFAULT_DESC.toString()))
            .andExpect(jsonPath("$.maxCapacity").value(DEFAULT_MAX_CAPACITY))
            .andExpect(jsonPath("$.site").value(DEFAULT_SITE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingQueue() throws Exception {
        // Get the queue
        restQueueMockMvc.perform(get("/api/queues/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateQueue() throws Exception {
        // Initialize the database
        queueRepository.saveAndFlush(queue);
        queueSearchRepository.save(queue);
        int databaseSizeBeforeUpdate = queueRepository.findAll().size();

        // Update the queue
        Queue updatedQueue = queueRepository.findOne(queue.getId());
        updatedQueue
            .name(UPDATED_NAME)
            .desc(UPDATED_DESC)
            .maxCapacity(UPDATED_MAX_CAPACITY)
            .site(UPDATED_SITE);

        restQueueMockMvc.perform(put("/api/queues")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedQueue)))
            .andExpect(status().isOk());

        // Validate the Queue in the database
        List<Queue> queueList = queueRepository.findAll();
        assertThat(queueList).hasSize(databaseSizeBeforeUpdate);
        Queue testQueue = queueList.get(queueList.size() - 1);
        assertThat(testQueue.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testQueue.getDesc()).isEqualTo(UPDATED_DESC);
        assertThat(testQueue.getMaxCapacity()).isEqualTo(UPDATED_MAX_CAPACITY);
        assertThat(testQueue.getSite()).isEqualTo(UPDATED_SITE);

        // Validate the Queue in Elasticsearch
        Queue queueEs = queueSearchRepository.findOne(testQueue.getId());
        assertThat(queueEs).isEqualToComparingFieldByField(testQueue);
    }

    @Test
    @Transactional
    public void updateNonExistingQueue() throws Exception {
        int databaseSizeBeforeUpdate = queueRepository.findAll().size();

        // Create the Queue

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restQueueMockMvc.perform(put("/api/queues")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(queue)))
            .andExpect(status().isCreated());

        // Validate the Queue in the database
        List<Queue> queueList = queueRepository.findAll();
        assertThat(queueList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteQueue() throws Exception {
        // Initialize the database
        queueRepository.saveAndFlush(queue);
        queueSearchRepository.save(queue);
        int databaseSizeBeforeDelete = queueRepository.findAll().size();

        // Get the queue
        restQueueMockMvc.perform(delete("/api/queues/{id}", queue.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean queueExistsInEs = queueSearchRepository.exists(queue.getId());
        assertThat(queueExistsInEs).isFalse();

        // Validate the database is empty
        List<Queue> queueList = queueRepository.findAll();
        assertThat(queueList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchQueue() throws Exception {
        // Initialize the database
        queueRepository.saveAndFlush(queue);
        queueSearchRepository.save(queue);

        // Search the queue
        restQueueMockMvc.perform(get("/api/_search/queues?query=id:" + queue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(queue.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].desc").value(hasItem(DEFAULT_DESC.toString())))
            .andExpect(jsonPath("$.[*].maxCapacity").value(hasItem(DEFAULT_MAX_CAPACITY)))
            .andExpect(jsonPath("$.[*].site").value(hasItem(DEFAULT_SITE.toString())));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Queue.class);
        Queue queue1 = new Queue();
        queue1.setId(1L);
        Queue queue2 = new Queue();
        queue2.setId(queue1.getId());
        assertThat(queue1).isEqualTo(queue2);
        queue2.setId(2L);
        assertThat(queue1).isNotEqualTo(queue2);
        queue1.setId(null);
        assertThat(queue1).isNotEqualTo(queue2);
    }
}
