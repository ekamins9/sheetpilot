package com.sheetpilot.controller;

import com.sheetpilot.dto.*;
import com.sheetpilot.service.PipelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for pipeline operations
 * Base path: /api/pipelines
 *
 * Endpoints:
 * - POST /api/pipelines - Create new pipeline
 * - GET /api/pipelines - List all pipelines with step count
 * - GET /api/pipelines/{id} - Get pipeline with all steps
 * - PUT /api/pipelines/{id} - Update pipeline metadata
 * - DELETE /api/pipelines/{id} - Delete pipeline (cascade delete steps)
 * - POST /api/pipelines/{id}/steps - Add step to pipeline
 * - PUT /api/pipelines/{pipelineId}/steps/{stepId} - Update step
 * - DELETE /api/pipelines/{pipelineId}/steps/{stepId} - Remove step
 */
@RestController
@RequestMapping("/api/pipelines")
@RequiredArgsConstructor
@Slf4j
public class PipelineController {

    private final PipelineService pipelineService;

    /**
     * Create a new pipeline
     * POST /api/pipelines
     *
     * @param request Pipeline creation request with name and description
     * @return 201 Created with pipeline details
     */
    @PostMapping
    public ResponseEntity<PipelineResponse> createPipeline(@Valid @RequestBody PipelineRequest request) {
        log.info("POST /api/pipelines - Creating pipeline: {}", request.getName());
        PipelineResponse response = pipelineService.createPipeline(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all pipelines with step count
     * GET /api/pipelines
     *
     * @return 200 OK with list of pipelines
     */
    @GetMapping
    public ResponseEntity<List<PipelineListResponse>> getAllPipelines() {
        log.info("GET /api/pipelines - Fetching all pipelines");
        List<PipelineListResponse> pipelines = pipelineService.getAllPipelines();
        return ResponseEntity.ok(pipelines);
    }

    /**
     * Get pipeline by ID with all steps
     * GET /api/pipelines/{id}
     *
     * @param id Pipeline ID
     * @return 200 OK with pipeline details and steps
     */
    @GetMapping("/{id}")
    public ResponseEntity<PipelineResponse> getPipelineById(@PathVariable Long id) {
        log.info("GET /api/pipelines/{} - Fetching pipeline", id);
        PipelineResponse response = pipelineService.getPipelineById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update pipeline metadata (name and description)
     * PUT /api/pipelines/{id}
     *
     * @param id Pipeline ID
     * @param request Update request with new name and/or description
     * @return 200 OK with updated pipeline
     */
    @PutMapping("/{id}")
    public ResponseEntity<PipelineResponse> updatePipeline(
            @PathVariable Long id,
            @Valid @RequestBody PipelineRequest request) {
        log.info("PUT /api/pipelines/{} - Updating pipeline", id);
        PipelineResponse response = pipelineService.updatePipeline(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete pipeline (cascade deletes all steps)
     * DELETE /api/pipelines/{id}
     *
     * @param id Pipeline ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePipeline(@PathVariable Long id) {
        log.info("DELETE /api/pipelines/{} - Deleting pipeline", id);
        pipelineService.deletePipeline(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a step to a pipeline
     * POST /api/pipelines/{id}/steps
     *
     * @param id Pipeline ID
     * @param request Step creation request
     * @return 201 Created with complete pipeline including new step
     */
    @PostMapping("/{id}/steps")
    public ResponseEntity<PipelineResponse> addStepToPipeline(
            @PathVariable Long id,
            @Valid @RequestBody PipelineStepRequest request) {
        log.info("POST /api/pipelines/{}/steps - Adding step to pipeline", id);
        PipelineResponse response = pipelineService.addStepToPipeline(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a specific pipeline step
     * PUT /api/pipelines/{pipelineId}/steps/{stepId}
     *
     * @param pipelineId Pipeline ID
     * @param stepId Step ID
     * @param request Step update request
     * @return 200 OK with updated step
     */
    @PutMapping("/{pipelineId}/steps/{stepId}")
    public ResponseEntity<PipelineStepResponse> updatePipelineStep(
            @PathVariable Long pipelineId,
            @PathVariable Long stepId,
            @Valid @RequestBody PipelineStepRequest request) {
        log.info("PUT /api/pipelines/{}/steps/{} - Updating pipeline step", pipelineId, stepId);
        PipelineStepResponse response = pipelineService.updatePipelineStep(pipelineId, stepId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a pipeline step
     * DELETE /api/pipelines/{pipelineId}/steps/{stepId}
     *
     * @param pipelineId Pipeline ID
     * @param stepId Step ID
     * @return 204 No Content on success
     */
    @DeleteMapping("/{pipelineId}/steps/{stepId}")
    public ResponseEntity<Void> deletePipelineStep(
            @PathVariable Long pipelineId,
            @PathVariable Long stepId) {
        log.info("DELETE /api/pipelines/{}/steps/{} - Deleting pipeline step", pipelineId, stepId);
        pipelineService.deletePipelineStep(pipelineId, stepId);
        return ResponseEntity.noContent().build();
    }
}
