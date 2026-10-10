package com.hshc.backend.collecteddata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectedDataRepository extends JpaRepository<CollectedData, Long> {
    List<CollectedData> findAllByOrderBySubmittedAtDesc();
}
