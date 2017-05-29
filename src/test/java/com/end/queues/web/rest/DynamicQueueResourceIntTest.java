package com.end.queues.web.rest;

import com.end.queues.Q1App;

import com.end.queues.domain.DynamicQueue;
import com.end.queues.repository.DynamicQueueRepository;
import com.end.queues.repository.search.DynamicQueueSearchRepository;
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
 * Test class for the DynamicQueueResource REST controller.
 *
 * @see DynamicQueueResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Q1App.class)
public class DynamicQueueResourceIntTest {

    private static final Long DEFAULT_NUM_PARTICIPANTS = 1L;
    private static final Long UPDATED_NUM_PARTICIPANTS = 2L;

    private static final Integer DEFAULT_RATE = 1;
    private static final Integer UPDATED_RATE = 2;

    @Autowired
    private DynamicQueueRepository dynamicQueueRepository;

    @Autowired
    private DynamicQueueSearchRepository dynamicQueueSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restDynamicQueueMockMvc;

    private DynamicQueue dynamicQueue;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        DynamicQueueResource dynamicQueueResource = new DynamicQueueResource(dynamicQueueRepository, dynamicQueueSearchRepository);
        this.restDynamicQueueMockMvc = MockMvcBuilders.standaloneSetup(dynamicQueueResource)
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
    public static DynamicQueue createEntity(EntityManager em) {
        DynamicQueue dynamicQueue = new DynamicQueue()
            .numParticipants(DEFAULT_NUM_PARTICIPANTS)
            .rate(DEFAULT_RATE);
        return dynamicQueue;
    }

    @Before
    public void initTest() {
        dynamicQueueSearchRepository.deleteAll();
        dynamicQueue = createEntity(em);
    }

    @Test
    @Transactional
    public void createDynamicQueue() throws Exception {
        int databaseSizeBeforeCreate = dynamicQueueRepository.findAll().size();

        // Create the DynamicQueue
        restDynamicQueueMockMvc.perform(post("/api/dynamic-queues")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(dynamicQueue)))
            .andExpect(status().isCreated());

        // Validate the DynamicQueue in the database
        List<DynamicQueue> dynamicQueueList = dynamicQueueRepository.findAll();
        assertThat(dynamicQueueList).hasSize(databaseSizeBeforeCreate + 1);
        DynamicQueue testDynamicQueue = dynamicQueueList.get(dynamicQueueList.size() - 1);
        assertThat(testDynamicQueue.getNumParticipants()).isEqualTo(DEFAULT_NUM_PARTICIPANTS);
        assertThat(testDynamicQueue.getRate()).isEqualTo(DEFAULT_RATE);

        // Validate the DynamicQueue in Elasticsearch
        DynamicQueue dynamicQueueEs = dynamicQueueSearchRepository.findOne(testDynamicQueue.getId());
        assertThat(dynamicQueueEs).isEqualToComparingFieldByField(testDynamicQueue);
    }

    @Test
    @Transactional
    public void createDynamicQueueWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = dynamicQueueRepository.findAll().size();

        // Create the DynamicQueue with an existing ID
        dynamicQueue.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restDynamicQueueMockMvc.perform(post("/api/dynamic-queues")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(dynamicQueue)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<DynamicQueue> dynamicQueueList = dynamicQueueRepository.findAll();
        assertThat(dynamicQueueList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllDynamicQueues() throws Exception {
        // Initialize the database
        dynamicQueueRepository.saveAndFlush(dynamicQueue);

        // Get all the dynamicQueueList
        restDynamicQueueMockMvc.perform(get("/api/dynamic-queues?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dynamicQueue.getId().intValue())))
            .andExpect(jsonPath("$.[*].numParticipants").value(hasItem(DEFAULT_NUM_PARTICIPANTS.intValue())))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(DEFAULT_RATE)));
    }

    @Test
    @Transactional
    public void getDynamicQueue() throws Exception {
        // Initialize the database
        dynamicQueueRepository.saveAndFlush(dynamicQueue);

        // Get the dynamicQueue
        restDynamicQueueMockMvc.perform(get("/api/dynamic-queues/{id}", dynamicQueue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(dynamicQueue.getId().intValue()))
            .andExpect(jsonPath("$.numParticipants").value(DEFAULT_NUM_PARTICIPANTS.intValue()))
            .andExpect(jsonPath("$.rate").value(DEFAULT_RATE));
    }

    @Test
    @Transactional
    public void getNonExistingDynamicQueue() throws Exception {
        // Get the dynamicQueue
        restDynamicQueueMockMvc.perform(get("/api/dynamic-queues/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateDynamicQueue() throws Exception {
        // Initialize the database
        dynamicQueueRepository.saveAndFlush(dynamicQueue);
        dynamicQueueSearchRepository.save(dynamicQueue);
        int databaseSizeBeforeUpdate = dynamicQueueRepository.findAll().size();

        // Update the dynamicQueue
        DynamicQueue updatedDynamicQueue = dynamicQueueRepository.findOne(dynamicQueue.getId());
        updatedDynamicQueue
            .numParticipants(UPDATED_NUM_PARTICIPANTS)
            .rate(UPDATED_RATE);

        restDynamicQueueMockMvc.perform(put("/api/dynamic-queues")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedDynamicQueue)))
            .andExpect(status().isOk());

        // Validate the DynamicQueue in the database
        List<DynamicQueue> dynamicQueueList = dynamicQueueRepository.findAll();
        assertThat(dynamicQueueList).hasSize(databaseSizeBeforeUpdate);
        DynamicQueue testDynamicQueue = dynamicQueueList.get(dynamicQueueList.size() - 1);
        assertThat(testDynamicQueue.getNumParticipants()).isEqualTo(UPDATED_NUM_PARTICIPANTS);
        assertThat(testDynamicQueue.getRate()).isEqualTo(UPDATED_RATE);

        // Validate the DynamicQueue in Elasticsearch
        DynamicQueue dynamicQueueEs = dynamicQueueSearchRepository.findOne(testDynamicQueue.getId());
        assertThat(dynamicQueueEs).isEqualToComparingFieldByField(testDynamicQueue);
    }

    @Test
    @Transactional
    public void updateNonExistingDynamicQueue() throws Exception {
        int databaseSizeBeforeUpdate = dynamicQueueRepository.findAll().size();

        // Create the DynamicQueue

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restDynamicQueueMockMvc.perform(put("/api/dynamic-queues")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(dynamicQueue)))
            .andExpect(status().isCreated());

        // Validate the DynamicQueue in the database
        List<DynamicQueue> dynamicQueueList = dynamicQueueRepository.findAll();
        assertThat(dynamicQueueList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteDynamicQueue() throws Exception {
        // Initialize the database
        dynamicQueueRepository.saveAndFlush(dynamicQueue);
        dynamicQueueSearchRepository.save(dynamicQueue);
        int databaseSizeBeforeDelete = dynamicQueueRepository.findAll().size();

        // Get the dynamicQueue
        restDynamicQueueMockMvc.perform(delete("/api/dynamic-queues/{id}", dynamicQueue.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate Elasticsearch is empty
        boolean dynamicQueueExistsInEs = dynamicQueueSearchRepository.exists(dynamicQueue.getId());
        assertThat(dynamicQueueExistsInEs).isFalse();

        // Validate the database is empty
        List<DynamicQueue> dynamicQueueList = dynamicQueueRepository.findAll();
        assertThat(dynamicQueueList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void searchDynamicQueue() throws Exception {
        // Initialize the database
        dynamicQueueRepository.saveAndFlush(dynamicQueue);
        dynamicQueueSearchRepository.save(dynamicQueue);

        // Search the dynamicQueue
        restDynamicQueueMockMvc.perform(get("/api/_search/dynamic-queues?query=id:" + dynamicQueue.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dynamicQueue.getId().intValue())))
            .andExpect(jsonPath("$.[*].numParticipants").value(hasItem(DEFAULT_NUM_PARTICIPANTS.intValue())))
            .andExpect(jsonPath("$.[*].rate").value(hasItem(DEFAULT_RATE)));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(DynamicQueue.class);
        DynamicQueue dynamicQueue1 = new DynamicQueue();
        dynamicQueue1.setId(1L);
        DynamicQueue dynamicQueue2 = new DynamicQueue();
        dynamicQueue2.setId(dynamicQueue1.getId());
        assertThat(dynamicQueue1).isEqualTo(dynamicQueue2);
        dynamicQueue2.setId(2L);
        assertThat(dynamicQueue1).isNotEqualTo(dynamicQueue2);
        dynamicQueue1.setId(null);
        assertThat(dynamicQueue1).isNotEqualTo(dynamicQueue2);
    }
}
