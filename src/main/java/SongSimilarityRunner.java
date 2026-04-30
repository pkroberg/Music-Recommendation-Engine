import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * PA3
 */
public class SongSimilarityRunner {

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

        if (userMap.size() < 2) {
            throw new Exception("at least two cooperative users are required for song similarity");
        }

        Map<String, Map<String, Double>> normalized = UserDataUtils.normalizeByUser(userMap);

        Map<String, Map<String, Double>> songMap = new HashMap<>();
        for (String user : normalized.keySet()) {
            for (String song : normalized.get(user).keySet()) {
                songMap.putIfAbsent(song, new HashMap<>());
                songMap.get(song).put(user, normalized.get(user).get(song));
            }
        }

        List<String> songs = new ArrayList<>(songMap.keySet());
        Collections.sort(songs);

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("name1,name2,similarity");
            writer.newLine();

            for (int i = 0; i < songs.size(); i++) {
                for (int j = i + 1; j < songs.size(); j++) {
                    String song1 = songs.get(i);
                    String song2 = songs.get(j);

                    Map<String, Double> ratings1 = songMap.get(song1);
                    Map<String, Double> ratings2 = songMap.get(song2);

                    double sum = 0;
                    int count = 0;
                    for (String user : ratings1.keySet()) {
                        if (ratings2.containsKey(user)) {
                            double diff = ratings1.get(user) - ratings2.get(user);
                            sum += diff * diff;
                            count++;
                        }
                    }

                    double distance = count == 0 ? Double.NaN : Math.sqrt(sum);
                    writer.write(song1 + "," + song2 + "," + distance);
                    writer.newLine();
                }
            }
        }
    }
}