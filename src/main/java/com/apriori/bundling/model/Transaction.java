package com.apriori.bundling.model;

import jakarta.persistence.*;

@Entity
@Table(name = "transaction_data")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id")
    private Long datasetId;

    @Column(length = 255)
    private String items;

    // ===== GETTER & SETTER =====
    public Long getId() {
        return id;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public String getItems() {
        return items;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public void setItems(String items) {
        this.items = items;
    }
}
