import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * PA4
 */
public class UserPredictionsRunner {

    public static void run(List<CSVRecord> records, Path outputPath) throws Exception {

        Map<String, Map<String, Double>> userMap = new HashMap<>();
        Map<String, Set<Double>> ratingDiversity = new HashMap<>();

        for (CSVRecord record : records) {
            String song   = record.get(0).trim();
            String user   = record.get(1).trim();
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

        Set<String> allSongs = new HashSet<>();
        for (Map<String, Double> songs : userMap.values()) allSongs.addAll(songs.keySet());

        boolean hasMissing = false;
        outer:
        for (String user : userMap.keySet()) {
            for (String song : allSongs) {
                if (!userMap.get(user).containsKey(song)) {
                    hasMissing = true;
                    break outer;
                }
            }
        }
        if (!hasMissing) {
            throw new Exception("no predictions to be made");
        }

        Map<String, Double> userMean = new HashMap<>();
        Map<String, Double> userStd  = new HashMap<>();
        UserDataUtils.computeUserMeanAndStd(userMap, userMean, userStd);

        Map<String, Map<String, Double>> normalized = UserDataUtils.normalizeByUser(userMap);

        List<String> sortedUsers = new ArrayList<>(userMap.keySet());
        Collections.sort(sortedUsers);

        Map<String, Map<String, Double>> userSimilarity =
                UserDataUtils.computeUserSimilarity(sortedUsers, normalized);
        Map<String, List<String>> mostSimilar =
                UserDataUtils.rankUsersBySimilarity(sortedUsers, userSimilarity);

        List<String> sortedSongs = new ArrayList<>(allSongs);
        Collections.sort(sortedSongs);

        List<String[]> predictions = new ArrayList<>();

        for (String song : sortedSongs) {
            for (String target : sortedUsers) {
                if (userMap.get(target).containsKey(song)) continue;

                Double borrowedNorm = null;
                for (String other : mostSimilar.get(target)) {
                    Double simScore = userSimilarity.get(target).get(other);
                    if (simScore == null || Double.isNaN(simScore)) continue;
                    Double normRating = normalized.get(other) != null
                            ? normalized.get(other).get(song) : null;
                    if (normRating != null) {
                        borrowedNorm = normRating;
                        break;
                    }
                }

                String predictedStr;
                if (borrowedNorm == null) {
                    predictedStr = "NaN";
                } else {
                    double mean = userMean.get(target);
                    double std  = userStd.get(target);
                    long rounded = Math.round(borrowedNorm * std + mean);
                    rounded = Math.max(1, Math.min(5, rounded));
                    predictedStr = String.valueOf(rounded);
                }

                predictions.add(new String[]{song, target, predictedStr});
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("song,user,predicted rating");
            writer.newLine();
            for (String[] row : predictions) {
                writer.write(row[0] + "," + row[1] + "," + row[2]);
                writer.newLine();
            }
        }
    }
}