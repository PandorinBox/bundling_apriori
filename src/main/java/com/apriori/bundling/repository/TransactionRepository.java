package com.apriori.bundling.repository;

import com.apriori.bundling.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDatasetId(Long datasetId);

    @Transactional
    void deleteByDatasetId(Long datasetId);
}

