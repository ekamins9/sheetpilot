package com.sheetpilot.service;

import com.sheetpilot.dto.*;
import com.sheetpilot.exception.ResourceNotFoundException;
import com.sheetpilot.model.Pipeline;
import com.sheetpilot.model.PipelineStep;
import com.sheetpilot.repository.PipelineRepository;
import com.sheetpilot.repository.PipelineStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for pipeline operations
 * Handles business logic for pipeline CRUD and step management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineService {

    private final PipelineRepository pipelineRepository;
    private final PipelineStepRepository pipelineStepRepository;

    /**
     * Create a new pipeline
     * @param request Pipeline creation request
     * @return Created pipeline response
     */
    @Transactional
    public PipelineResponse createPipeline(PipelineRequest request) {
        log.info("Creating new pipeline with name: {}", request.getName());

        Pipeline pipeline = Pipeline.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Pipeline saved = pipelineRepository.save(pipeline);
        log.info("Pipeline created successfully with id: {}", saved.getId());

        return mapToPipelineResponse(saved);
    }

    /**
     * Get all pipelines with step count (efficient listing)
     * @return List of pipelines with metadata
     */
    @Transactional(readOnly = true)
    public List<PipelineListResponse> getAllPipelines() {
        log.info("Fetching all pipelines");

        List<Pipeline> pipelines = pipelineRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return pipelines.stream()
                .map(this::mapToPipelineListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pipeline by ID with all steps
     * Uses optimized query with JOIN FETCH to avoid N+1 problem
     * @param id Pipeline ID
     * @return Pipeline with all steps
     */
    @Transactional(readOnly = true)
    public PipelineResponse getPipelineById(Long id) {
        log.info("Fetching pipeline with id: {}", id);

        Pipeline pipeline = pipelineRepository.findByIdWithSteps(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline", id));

        return mapToPipelineResponse(pipeline);
    }

    /**
     * Update pipeline metadata (name and description)
     * @param id Pipeline ID
     * @param request Update request
     * @return Updated pipeline
     */
    @Transactional
    public PipelineResponse updatePipeline(Long id, PipelineRequest request) {
        log.info("Updating pipeline with id: {}", id);

        Pipeline pipeline = pipelineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline", id));

        pipeline.setName(request.getName());
        pipeline.setDescription(request.getDescription());

        Pipeline updated = pipelineRepository.save(pipeline);
        log.info("Pipeline updated successfully with id: {}", updated.getId());

        return mapToPipelineResponse(updated);
    }

    /**
     * Delete pipeline (cascade deletes all steps)
     * @param id Pipeline ID
     */
    @Transactional
    public void deletePipeline(Long id) {
        log.info("Deleting pipeline with id: {}", id);

        if (!pipelineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pipeline", id);
        }

        pipelineRepository.deleteById(id);
        log.info("Pipeline deleted successfully with id: {}", id);
    }

    /**
     * Add a step to a pipeline
     * @param pipelineId Pipeline ID
     * @param request Step creation request
     * @return Complete pipeline with new step
     */
    @Transactional
    public PipelineResponse addStepToPipeline(Long pipelineId, PipelineStepRequest request) {
        log.info("Adding step to pipeline with id: {}", pipelineId);

        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline", pipelineId));

        PipelineStep step = PipelineStep.builder()
                .pipeline(pipeline)
                .stepOrder(request.getStepOrder())
                .transformationType(request.getTransformationType())
                .config(request.getConfig())
                .build();

        pipelineStepRepository.save(step);
        log.info("Step added successfully to pipeline id: {}", pipelineId);

        // Fetch pipeline with updated steps
        Pipeline updated = pipelineRepository.findByIdWithSteps(pipelineId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline", pipelineId));

        return mapToPipelineResponse(updated);
    }

    /**
     * Update a specific pipeline step
     * @param pipelineId Pipeline ID
     * @param stepId Step ID
     * @param request Step update request
     * @return Updated pipeline step
     */
    @Transactional
    public PipelineStepResponse updatePipelineStep(Long pipelineId, Long stepId, PipelineStepRequest request) {
        log.info("Updating step {} for pipeline {}", stepId, pipelineId);

        PipelineStep step = pipelineStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline step", stepId));

        // Verify step belongs to the specified pipeline (security check)
        if (!step.getPipeline().getId().equals(pipelineId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pipeline");
        }

        step.setStepOrder(request.getStepOrder());
        step.setTransformationType(request.getTransformationType());
        step.setConfig(request.getConfig());

        PipelineStep updated = pipelineStepRepository.save(step);
        log.info("Step updated successfully with id: {}", stepId);

        return mapToPipelineStepResponse(updated);
    }

    /**
     * Delete a pipeline step
     * @param pipelineId Pipeline ID
     * @param stepId Step ID
     */
    @Transactional
    public void deletePipelineStep(Long pipelineId, Long stepId) {
        log.info("Deleting step {} from pipeline {}", stepId, pipelineId);

        PipelineStep step = pipelineStepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Pipeline step", stepId));

        // Verify step belongs to the specified pipeline (security check)
        if (!step.getPipeline().getId().equals(pipelineId)) {
            throw new IllegalArgumentException("Step does not belong to the specified pipeline");
        }

        pipelineStepRepository.delete(step);
        log.info("Step deleted successfully with id: {}", stepId);
    }

    /**
     * Map Pipeline entity to PipelineResponse DTO
     */
    private PipelineResponse mapToPipelineResponse(Pipeline pipeline) {
        List<PipelineStepResponse> steps = pipeline.getSteps().stream()
                .map(this::mapToPipelineStepResponse)
                .collect(Collectors.toList());

        return PipelineResponse.builder()
                .id(pipeline.getId())
                .name(pipeline.getName())
                .description(pipeline.getDescription())
                .createdAt(pipeline.getCreatedAt())
                .updatedAt(pipeline.getUpdatedAt())
                .steps(steps)
                .build();
    }

    /**
     * Map Pipeline entity to PipelineListResponse DTO (efficient for listing)
     */
    private PipelineListResponse mapToPipelineListResponse(Pipeline pipeline) {
        return PipelineListResponse.builder()
                .id(pipeline.getId())
                .name(pipeline.getName())
                .description(pipeline.getDescription())
                .stepCount(pipeline.getSteps().size())
                .createdAt(pipeline.getCreatedAt())
                .updatedAt(pipeline.getUpdatedAt())
                .build();
    }

    /**
     * Map PipelineStep entity to PipelineStepResponse DTO
     */
    private PipelineStepResponse mapToPipelineStepResponse(PipelineStep step) {
        return PipelineStepResponse.builder()
                .id(step.getId())
                .stepOrder(step.getStepOrder())
                .transformationType(step.getTransformationType())
                .config(step.getConfig())
                .build();
    }
}
