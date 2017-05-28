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
 * A Queue.
 */
@Entity
@Table(name = "queue")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "queue")
public class Queue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "jhi_desc")
    private String desc;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "site")
    private String site;

    @OneToOne
    @JoinColumn(unique = true)
    private DynamicQueue dynamicInfo;

    @OneToMany(mappedBy = "queue")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Location> locations = new HashSet<>();

    @ManyToOne
    private Admin manager;

    @ManyToOne
    private Organization organization;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Queue name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public Queue desc(String desc) {
        this.desc = desc;
        return this;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public Queue maxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
        return this;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getSite() {
        return site;
    }

    public Queue site(String site) {
        this.site = site;
        return this;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public DynamicQueue getDynamicInfo() {
        return dynamicInfo;
    }

    public Queue dynamicInfo(DynamicQueue dynamicQueue) {
        this.dynamicInfo = dynamicQueue;
        return this;
    }

    public void setDynamicInfo(DynamicQueue dynamicQueue) {
        this.dynamicInfo = dynamicQueue;
    }

    public Set<Location> getLocations() {
        return locations;
    }

    public Queue locations(Set<Location> locations) {
        this.locations = locations;
        return this;
    }

    public Queue addLocation(Location location) {
        this.locations.add(location);
        location.setQueue(this);
        return this;
    }

    public Queue removeLocation(Location location) {
        this.locations.remove(location);
        location.setQueue(null);
        return this;
    }

    public void setLocations(Set<Location> locations) {
        this.locations = locations;
    }

    public Admin getManager() {
        return manager;
    }

    public Queue manager(Admin admin) {
        this.manager = admin;
        return this;
    }

    public void setManager(Admin admin) {
        this.manager = admin;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Queue organization(Organization organization) {
        this.organization = organization;
        return this;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Queue queue = (Queue) o;
        if (queue.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), queue.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Queue{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", desc='" + getDesc() + "'" +
            ", maxCapacity='" + getMaxCapacity() + "'" +
            ", site='" + getSite() + "'" +
            "}";
    }
}
