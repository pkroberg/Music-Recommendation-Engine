import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * PA2
 */
public class UserAnalysisRunner {

    public static void run(List<CSVRecord> records, Path outputPath) throws Exception {

        Map<String, Map<String, Double>> userMap = new HashMap<>();
        Map<String, Set<Double>> ratingDiversity = new HashMap<>();

        for (CSVRecord record : records) {
            String song = record.get(0).trim();
            String user = record.get(1).trim();
            double rating = Double.parseDouble(record.get(2).trim());

            userMap.putIfAbsent(user, new HashMap<>());
            ratingDiversity.putIfAbsent(user, new HashSet<>());

            userMap.get(user).put(song, rating);
            ratingDiversity.get(user).add(rating);
        }

        UserDataUtils.removeNonCooperativeUsers(userMap, ratingDiversity);

        Set<String> songSet = new HashSet<>();
        for (Map<String, Double> songs : userMap.values()) songSet.addAll(songs.keySet());

        List<String> sortedUsers = new ArrayList<>(userMap.keySet());
        List<String> sortedSongs = new ArrayList<>(songSet);
        Collections.sort(sortedUsers);
        Collections.sort(sortedSongs);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("username,song,rating");
            writer.newLine();

            for (String user : sortedUsers) {
                for (String song : sortedSongs) {
                    Double rating = userMap.get(user).get(song);
                    if (rating == null) rating = Double.NaN;
                    writer.write(user + "," + song + "," + rating);
                    writer.newLine();
                }
            }
        }
    }
}