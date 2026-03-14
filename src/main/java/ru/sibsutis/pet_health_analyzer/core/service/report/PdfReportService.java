package ru.sibsutis.pet_health_analyzer.core.service.report;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfReportService {

    public byte[] generateReport(List<Double> values, List<String> dates, String petName) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Загрузка шрифта (например, стандартный)
                PDType0Font font = PDType0Font.load(document, getClass().getResourceAsStream("/fonts/arial.ttf"));

                // Заголовок
                contentStream.beginText();
                contentStream.setFont(font, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Отчёт по анализам для: " + petName);
                contentStream.endText();

                // Создание графика динамики
                JFreeChart chart = createChart(values, dates);
                BufferedImage chartImage = chart.createBufferedImage(500, 300);

                // Конвертация BufferedImage в PDImageXObject
                PDImageXObject pdImage = LosslessFactory.createFromImage(document, chartImage);
                contentStream.drawImage(pdImage, 50, 400, 500, 300);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private JFreeChart createChart(List<Double> values, List<String> dates) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < values.size(); i++) {
            dataset.addValue(values.get(i), "Показатель", dates.get(i));
        }
        return ChartFactory.createLineChart(
                "Динамика показателя",
                "Дата",
                "Значение",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    }
}