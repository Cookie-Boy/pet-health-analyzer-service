package ru.sibsutis.pet_health_analyzer.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "pet_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetResult {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String petId;

    @Column(nullable = false)
    private Integer heartRate;

    @Column(nullable = false)
    private Integer respiration;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private boolean isAnomalous;

    @Column(nullable = false)
    private int anomalyClass;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AnomalyType anomalyType;

    @Column
    private Double distanceFromHome;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> details;

    @Column
    private Long timestamp;
}