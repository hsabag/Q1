package com.end.queues.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DynamicQueue.
 */
@Entity
@Table(name = "dynamic_queue")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "dynamicqueue")
public class DynamicQueue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "num_participants")
    private Long numParticipants;

    @Column(name = "rate")
    private Integer rate;

    @OneToMany(mappedBy = "dynamicQueue")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Participant> participants = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumParticipants() {
        return numParticipants;
    }

    public DynamicQueue numParticipants(Long numParticipants) {
        this.numParticipants = numParticipants;
        return this;
    }

    public void setNumParticipants(Long numParticipants) {
        this.numParticipants = numParticipants;
    }

    public Integer getRate() {
        return rate;
    }

    public DynamicQueue rate(Integer rate) {
        this.rate = rate;
        return this;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public DynamicQueue participants(Set<Participant> participants) {
        this.participants = participants;
        return this;
    }

    public DynamicQueue addParticipants(Participant participant) {
        this.participants.add(participant);
        participant.setDynamicQueue(this);
        return this;
    }

    public DynamicQueue removeParticipants(Participant participant) {
        this.participants.remove(participant);
        participant.setDynamicQueue(null);
        return this;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DynamicQueue dynamicQueue = (DynamicQueue) o;
        if (dynamicQueue.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), dynamicQueue.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DynamicQueue{" +
            "id=" + getId() +
            ", numParticipants='" + getNumParticipants() + "'" +
            ", rate='" + getRate() + "'" +
            "}";
    }
}
