package com.sheetpilot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "pipeline_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "transformation_type", nullable = false)
    private String transformationType;

    /**
     * JSON configuration for the transformation step
     * Structure varies based on transformationType
     * Example for filter: {"column": "age", "operator": ">", "value": 18}
     * Example for sort: {"column": "name", "direction": "ASC"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> config;
}
