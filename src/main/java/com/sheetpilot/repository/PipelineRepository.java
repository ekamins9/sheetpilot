package com.sheetpilot.repository;

import com.sheetpilot.model.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

    List<Pipeline> findByNameContainingIgnoreCase(String name);

    List<Pipeline> findByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Pipeline p LEFT JOIN FETCH p.steps WHERE p.id = :id")
    Optional<Pipeline> findByIdWithSteps(Long id);
}
