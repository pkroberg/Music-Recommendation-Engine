import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * PA1
 */
public class SongStatsRunner {

    public static void run(List<CSVRecord> records, Path outputPath) throws Exception {

        Map<String, SongStats> songMap = new HashMap<>();

        for (CSVRecord record : records) {
            String song = record.get(0).trim();
            double rating = Double.parseDouble(record.get(2).trim());

            songMap.putIfAbsent(song, new SongStats());
            songMap.get(song).add(rating);
        }

        List<String> songs = new ArrayList<>(songMap.keySet());
        Collections.sort(songs);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("song,number of ratings,mean,standard deviation");
            writer.newLine();

            for (String song : songs) {
                SongStats stats = songMap.get(song);
                writer.write(song + "," + stats.getCount() + "," + stats.getMean() + "," + stats.getStandDev());
                writer.newLine();
            }
        }
    }
}