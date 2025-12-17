package com.apriori.bundling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.apriori.bundling.model.Dataset;

public interface DatasetRepository extends JpaRepository<Dataset, Long> {

    Dataset findTopByOrderByIdDesc();
}

