package ru.sibsutis.pet_health_analyzer.core.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LabResult {
    private String id;

    private LabTest labTest;
    private LocalDateTime testDate;
    private String resultValue;
    private String pdfReportPath;
    private Boolean aiProcessed;
    private List<AiAnalysisResult> aiAnalysisResults;
}
