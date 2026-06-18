package it.andrea.audio;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {

    public static void export(String outputPath, List<LowFreqAnalysisResult> results) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {

            writer.write("File,FreqDominanteHz,MediaBasseDb,MediaRiferimentoDb,EccessoBasseDb,SuggeritaFreqHz,SuggeritaQ,SuggeritaCutDb,LoudnessPrevistoDb\n");

            for (LowFreqAnalysisResult r : results) {
                writer.write(String.format(
                        "%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                        r.fileName(),
                        r.dominantFrequencyHz(),
                        r.avgLowBandDb(),
                        r.avgRefBandDb(),
                        r.lowExcessDb(),
                        r.suggestedFreqHz(),
                        r.suggestedQ(),
                        r.suggestedCutDb(),
                        r.predictedLoudnessDb()
                ));
            }
        }
    }
}
