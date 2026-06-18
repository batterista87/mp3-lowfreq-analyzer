package it.andrea.audio;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class XlsxExporter {

    public static void export(String outputPath, List<LowFreqAnalysisResult> results) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Analisi Basse");

        // Header
        Row header = sheet.createRow(0);
        String[] columns = {
                "File",
                "Freq dominante (20–150 Hz)",
                "Media basse (dB)",
                "Media riferimento (200–800 Hz)",
                "Eccesso basse (dB)",
                "Suggerita freq (Hz)",
                "Suggerita Q",
                "Suggerita cut (dB)",
                "Loudness previsto dopo EQ (dB)"
        };

        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Dati
        int rowIndex = 1;
        for (LowFreqAnalysisResult r : results) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(r.fileName());
            row.createCell(1).setCellValue(r.dominantFrequencyHz());
            row.createCell(2).setCellValue(r.avgLowBandDb());
            row.createCell(3).setCellValue(r.avgRefBandDb());
            row.createCell(4).setCellValue(r.lowExcessDb());
            row.createCell(5).setCellValue(r.suggestedFreqHz());
            row.createCell(6).setCellValue(r.suggestedQ());
            row.createCell(7).setCellValue(r.suggestedCutDb());
            row.createCell(8).setCellValue(r.predictedLoudnessDb());
        }

        // Auto-size colonne
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Scrittura file
        try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
}
