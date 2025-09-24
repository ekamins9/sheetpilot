package com.sheetpilot.repository;

import com.sheetpilot.model.PipelineStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PipelineStepRepository extends JpaRepository<PipelineStep, Long> {

    List<PipelineStep> findByPipelineIdOrderByStepOrderAsc(Long pipelineId);

    List<PipelineStep> findByTransformationType(String transformationType);
}
