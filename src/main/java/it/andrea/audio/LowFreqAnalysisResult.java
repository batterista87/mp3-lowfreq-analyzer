package it.andrea.audio;

public record LowFreqAnalysisResult(
        String fileName,
        double dominantFrequencyHz,
        double avgLowBandDb,
        double avgRefBandDb,
        double lowExcessDb,
        double suggestedFreqHz,
        double suggestedQ,
        double suggestedCutDb,
        double predictedLoudnessDb
) {}
