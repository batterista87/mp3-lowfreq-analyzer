package it.andrea.audio;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println("Uso: java -jar analyzer.jar <cartella_mp3>");
            return;
        }

        Path folder = Paths.get(args[0]);
        if (!Files.isDirectory(folder)) {
            System.out.println("Cartella non valida.");
            return;
        }

        List<LowFreqAnalysisResult> results = new ArrayList<>();

        Files.list(folder)
                .filter(p -> p.toString().toLowerCase().endsWith(".mp3"))
                .forEach(p -> {
                    try {
                        LowFreqAnalysisResult r = LowFreqAnalyzer.analyzeMp3(p.toFile());
                        results.add(r);
                        print(r);
                    } catch (Exception e) {
                        System.err.println("Errore: " + e.getMessage());
                    }
                });

        // Salva XLSX nella cartella
        String xlsxPath = folder.resolve("analisi_basse.xlsx").toString();
        XlsxExporter.export(xlsxPath, results);

        System.out.println("\nXLSX generato: " + xlsxPath);
    }

    private static void print(LowFreqAnalysisResult r) {
        System.out.println("File: " + r.fileName());
        System.out.printf("  Freq dominante (20–150 Hz): %.1f Hz%n", r.dominantFrequencyHz());
        System.out.printf("  Media basse: %.2f dB%n", r.avgLowBandDb());
        System.out.printf("  Media riferimento (200–800 Hz): %.2f dB%n", r.avgRefBandDb());
        System.out.printf("  Eccesso basse: %.2f dB%n", r.lowExcessDb());

        if (r.suggestedCutDb() < 0) {
            System.out.println("  Suggerimento EQ:");
            System.out.printf("    Bell @ %.1f Hz, Q=%.2f, Gain=%.1f dB%n",
                    r.suggestedFreqHz(), r.suggestedQ(), r.suggestedCutDb());
        } else {
            System.out.println("  Nessun taglio necessario.");
        }

        System.out.printf("  Loudness previsto dopo EQ: %.2f dB%n", r.predictedLoudnessDb());
        System.out.println();
    }
}
