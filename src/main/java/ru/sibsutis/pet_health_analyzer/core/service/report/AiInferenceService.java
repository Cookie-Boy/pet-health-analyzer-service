package ru.sibsutis.pet_health_analyzer.core.service.report;

import org.springframework.stereotype.Service;
import ru.sibsutis.pet_health_analyzer.core.model.AiAnalysisResult;
import ru.sibsutis.pet_health_analyzer.core.model.LabResult;
import ru.sibsutis.pet_health_analyzer.core.model.VitalData;

import java.util.List;

@Service
public class AiInferenceService {
    // Предположим, модель сохранена в resources/model.pkl или другом формате
    // Для демонстрации используем заглушку

    public List<AiAnalysisResult> analyze(LabResult labResult, VitalData vitalData) {
        // Загрузка модели, выполнение предсказаний
        // Сравнение с нормами для породы (таблица breed_norms)
        // Возврат списка AiAnalysisResult
        return null;
    }
}