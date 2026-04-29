package ru.sibsutis.pet_health_analyzer.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sibsutis.pet_health_analyzer.core.model.PetResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PetResultRepository extends JpaRepository<PetResult, UUID> {
    List<PetResult> findByPetIdAndTimestampGreaterThanEqualOrderByTimestampAsc(String petId, Long timestamp);
    Optional<PetResult> findTopByPetIdOrderByTimestampDesc(String petId);
}
