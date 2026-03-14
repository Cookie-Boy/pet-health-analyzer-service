package ru.sibsutis.pet_health_analyzer.core.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import ru.sibsutis.pet_health_analyzer.core.model.Pet;

import java.time.LocalDateTime;
import java.util.List;

public interface PetRepository extends MongoRepository<Pet, String> {

    List<Pet> findByBreed(String breed);

    @Query("{'labResults.testDate': {$gte: ?0, $lte: ?1}}")
    List<Pet> findPetsWithLabResultsInDateRange(LocalDateTime start, LocalDateTime end);

    @Query("{'labResults.aiProcessed': false}")
    List<Pet> findPetsWithUnprocessedAIResults();
}
