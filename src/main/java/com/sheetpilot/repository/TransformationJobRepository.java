package com.sheetpilot.repository;

import com.sheetpilot.model.TransformationJob;
import com.sheetpilot.model.TransformationJob.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransformationJobRepository extends JpaRepository<TransformationJob, Long> {

    List<TransformationJob> findByStatus(JobStatus status);

    List<TransformationJob> findByPipelineId(Long pipelineId);

    List<TransformationJob> findBySpreadsheetId(Long spreadsheetId);

    List<TransformationJob> findByCreatedAtBetween(Instant start, Instant end);

    List<TransformationJob> findByOrderByCreatedAtDesc();

    @Query("SELECT j FROM TransformationJob j WHERE j.status IN :statuses ORDER BY j.createdAt DESC")
    List<TransformationJob> findByStatusInOrderByCreatedAtDesc(List<JobStatus> statuses);

    long countByStatus(JobStatus status);
}
