package com.sheetpilot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "transformation_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransformationJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spreadsheet_id", nullable = false)
    private Spreadsheet spreadsheet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    /**
     * JSON structure storing job result metadata
     * Example: {
     *   "recordsProcessed": 1000,
     *   "recordsFiltered": 50,
     *   "outputFileUrl": "s3://bucket/output.xlsx",
     *   "errors": []
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> result;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = JobStatus.PENDING;
        }
    }

    public enum JobStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
