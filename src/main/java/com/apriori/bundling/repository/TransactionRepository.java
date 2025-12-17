package com.apriori.bundling.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.apriori.bundling.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDatasetId(Long datasetId);
}
