package ru.sibsutis.pet_health_analyzer.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sibsutis.pet_health_analyzer.api.dto.LatestPetResultDto;
import ru.sibsutis.pet_health_analyzer.api.dto.PetResultDto;
import ru.sibsutis.pet_health_analyzer.api.dto.RecommendationDto;
import ru.sibsutis.pet_health_analyzer.core.model.Period;
import ru.sibsutis.pet_health_analyzer.core.service.PetAnalyzerService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class PetAnalyzerController {

    private final PetAnalyzerService petAnalyzerService;

    @GetMapping("/vitals/{petId}")
    public ResponseEntity<List<PetResultDto>> getVitalsHistory(
            @PathVariable String petId,
            @RequestParam(defaultValue = "day") Period period) {

        List<PetResultDto> history = petAnalyzerService.getVitalsHistory(petId, period);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/vitals/{petId}/latest")
    public ResponseEntity<LatestPetResultDto> getLatestVitals(@PathVariable String petId) {
        LatestPetResultDto latestVitals = petAnalyzerService.getLatestVitals(petId);
        return ResponseEntity.ok(latestVitals);
    }

    @GetMapping("/recommendations/{petId}")
    public ResponseEntity<List<RecommendationDto>> getRecommendations(@PathVariable String petId) {
        List<RecommendationDto> recommendations = petAnalyzerService.getRecommendations(petId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/analyze/{petId}")
    public ResponseEntity<RecommendationDto> analyzeAndGenerateRecommendations(
            @PathVariable String petId,
            @RequestParam(defaultValue = "week") Period period) {

        RecommendationDto recommendation = petAnalyzerService.generateRecommendations(petId, period);
        return ResponseEntity.ok(recommendation);
    }
}
