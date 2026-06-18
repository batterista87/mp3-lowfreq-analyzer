package it.andrea.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.util.fft.FFT;

public class LowFreqAnalyzer {

    private static final float SAMPLE_RATE = 44100f;
    private static final int BUFFER_SIZE = 4096;
    private static final int OVERLAP = 2048;

    // Range basse aggiornato
    private static final double LOW_MIN_HZ = 20.0;
    private static final double LOW_MAX_HZ = 150.0;

    // Nuova banda di riferimento
    private static final double REF_MIN_HZ = 200.0;
    private static final double REF_MAX_HZ = 800.0;

    // Soglia per considerare le basse "troppo alte"
    private static final double LOW_EXCESS_THRESHOLD_DB = 2.0;

    public static LowFreqAnalysisResult analyzeMp3(File mp3File) throws Exception {

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromPipe(
                mp3File.getAbsolutePath(),
                (int) SAMPLE_RATE,
                BUFFER_SIZE,
                OVERLAP
        );

        FFT fft = new FFT(BUFFER_SIZE);
        float[] window = new float[BUFFER_SIZE];
        float[] magnitudes = new float[BUFFER_SIZE / 2];

        List<double[]> lowBandList = new ArrayList<>();
        List<double[]> refBandList = new ArrayList<>();

        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] buffer = audioEvent.getFloatBuffer();
                System.arraycopy(buffer, 0, window, 0, buffer.length);

                fft.forwardTransform(window);
                fft.modulus(window, magnitudes);

                double[] lowBand = extractBand(magnitudes, LOW_MIN_HZ, LOW_MAX_HZ);
                double[] refBand = extractBand(magnitudes, REF_MIN_HZ, REF_MAX_HZ);

                lowBandList.add(lowBand);
                refBandList.add(refBand);

                return true;
            }

            @Override
            public void processingFinished() {}
        });

        dispatcher.run();

        double[] avgLow = averageBands(lowBandList);
        double[] avgRef = averageBands(refBandList);

        double avgLowDb = averageDb(avgLow);
        double avgRefDb = averageDb(avgRef);
        double lowExcessDb = avgLowDb - avgRefDb;

        double dominantFreq = findDominantFrequency(avgLow, LOW_MIN_HZ, LOW_MAX_HZ);

        double suggestedFreq = dominantFreq;
        double suggestedQ = 2.0;
        double suggestedCutDb = 0.0;

        if (lowExcessDb > LOW_EXCESS_THRESHOLD_DB) {
            suggestedCutDb = -Math.min(lowExcessDb * 0.7, 6.0);
        }

        // Stima loudness post-EQ (approssimazione)
        double predictedLoudness = 89.0 + (suggestedCutDb * 0.30);

        return new LowFreqAnalysisResult(
                mp3File.getName(),
                dominantFreq,
                avgLowDb,
                avgRefDb,
                lowExcessDb,
                suggestedFreq,
                suggestedQ,
                suggestedCutDb,
                predictedLoudness
        );
    }

    private static double[] extractBand(float[] magnitudes, double minHz, double maxHz) {
        int minBin = freqToBin(minHz);
        int maxBin = freqToBin(maxHz);
        maxBin = Math.min(maxBin, magnitudes.length - 1);

        double[] band = new double[maxBin - minBin + 1];
        for (int i = minBin, j = 0; i <= maxBin; i++, j++) {
            double mag = Math.max(magnitudes[i], 1e-9);
            band[j] = 20.0 * Math.log10(mag);
        }
        return band;
    }

    private static int freqToBin(double freqHz) {
        return (int) Math.round(freqHz * BUFFER_SIZE / SAMPLE_RATE);
    }

    private static double[] averageBands(List<double[]> bands) {
        int len = bands.get(0).length;
        double[] avg = new double[len];

        for (double[] band : bands) {
            for (int i = 0; i < len; i++) {
                avg[i] += band[i];
            }
        }
        for (int i = 0; i < len; i++) {
            avg[i] /= bands.size();
        }
        return avg;
    }

    private static double averageDb(double[] band) {
        double sum = 0.0;
        for (double v : band) sum += v;
        return sum / band.length;
    }

    private static double findDominantFrequency(double[] avgBandDb, double minHz, double maxHz) {
        int minBin = freqToBin(minHz);
        int maxBin = freqToBin(maxHz);
        int len = maxBin - minBin + 1;

        double maxVal = Double.NEGATIVE_INFINITY;
        int maxIndex = 0;

        for (int i = 0; i < len; i++) {
            if (avgBandDb[i] > maxVal) {
                maxVal = avgBandDb[i];
                maxIndex = i;
            }
        }

        int bin = minBin + maxIndex;
        return bin * SAMPLE_RATE / BUFFER_SIZE;
    }
}
