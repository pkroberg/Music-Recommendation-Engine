import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * PA6
 */
public class PlaylistRunner {

    public static void run(List<CSVRecord> records, Path outputPath, int k, List<String> userSongs)
            throws Exception {

        if (k < 1) {
            throw new Exception("K must be at least 1");
        }
        if (userSongs.size() < k) {
            throw new Exception("fewer user-provided songs than K");
        }

        Set<String> userSongSet = new HashSet<>();
        for (String s : userSongs) {
            if (!userSongSet.add(s)) {
                throw new Exception("duplicate song titles in input");
            }
        }

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

        for (String sel : userSongs) {
            if (!allSongs.contains(sel)) {
                throw new Exception("user-provided song not found");
            }
        }

        Map<String, Double> userMeanNorm = new HashMap<>();
        Map<String, Double> userStdNorm  = new HashMap<>();
        UserDataUtils.computeUserMeanAndStd(userMap, userMeanNorm, userStdNorm);

        Map<String, Map<String, Double>> normalized = UserDataUtils.normalizeByUser(userMap);
        List<String> sortedUsers = new ArrayList<>(userMap.keySet());
        Collections.sort(sortedUsers);

        Map<String, Map<String, Double>> userSimilarity =
                UserDataUtils.computeUserSimilarity(sortedUsers, normalized);
        Map<String, List<String>> mostSimilar =
                UserDataUtils.rankUsersBySimilarity(sortedUsers, userSimilarity);

        Map<String, Double> userMeanActual = new HashMap<>();
        Map<String, Integer> userCountActual = new HashMap<>();
        for (String user : userMap.keySet()) {
            Collection<Double> ratings = userMap.get(user).values();
            double sum = 0;
            for (double r : ratings) sum += r;
            userMeanActual.put(user, sum / ratings.size());
            userCountActual.put(user, ratings.size());
        }

        Map<String, Double> songMeanActual = new HashMap<>();
        Map<String, Integer> songCountActual = new HashMap<>();
        for (String song : allSongs) {
            double sum = 0;
            int cnt = 0;
            for (Map<String, Double> uRatings : userMap.values()) {
                if (uRatings.containsKey(song)) { sum += uRatings.get(song); cnt++; }
            }
            songMeanActual.put(song, cnt > 0 ? sum / cnt : 0.0);
            songCountActual.put(song, cnt);
        }

        Map<String, Map<String, Double>> filled = new HashMap<>();
        for (String user : userMap.keySet()) {
            filled.put(user, new HashMap<>(userMap.get(user)));
        }

        for (String song : allSongs) {
            for (String target : sortedUsers) {
                if (filled.get(target).containsKey(song)) continue;

                Double borrowedNorm = null;
                for (String other : mostSimilar.get(target)) {
                    Double simScore = userSimilarity.get(target).get(other);
                    if (simScore == null || Double.isNaN(simScore)) continue;
                    Double normRating = normalized.get(other) != null
                            ? normalized.get(other).get(song) : null;
                    if (normRating != null) { borrowedNorm = normRating; break; }
                }

                if (borrowedNorm != null) {
                    double mean = userMeanNorm.get(target);
                    double std  = userStdNorm.get(target);
                    long rounded = Math.round(borrowedNorm * std + mean);
                    rounded = Math.max(1, Math.min(5, rounded));
                    filled.get(target).put(song, (double) rounded);
                }
            }
        }

        for (String song : allSongs) {
            for (String user : sortedUsers) {
                if (filled.get(user).containsKey(song)) continue;

                double songMean = songMeanActual.get(song);
                double uMean    = userMeanActual.get(user);
                int    nSong    = songCountActual.get(song);
                int    nUser    = userCountActual.get(user);

                double weighted = (songMean * nSong + uMean * nUser) / (double) (nSong + nUser);
                long rounded = Math.round(weighted);
                rounded = Math.max(1, Math.min(5, rounded));
                filled.get(user).put(song, (double) rounded);
            }
        }

        Set<String> songsToRemove = new HashSet<>();
        for (String song : allSongs) {
            Set<Double> distinct = new HashSet<>();
            for (String user : sortedUsers) {
                Double r = filled.get(user).get(song);
                if (r != null) distinct.add(r);
            }
            if (distinct.size() <= 1) songsToRemove.add(song);
        }
        allSongs.removeAll(songsToRemove);

        if (allSongs.size() < k + 1) {
            throw new Exception("pre-processing leaves fewer than K+1 songs");
        }

        for (String sel : userSongs) {
            if (!allSongs.contains(sel)) {
                throw new Exception("pre-processing leaves fewer than K+1 songs");
            }
        }

        List<String> sortedSongs = new ArrayList<>(allSongs);
        Collections.sort(sortedSongs);

        int numUsers = sortedUsers.size();

        Map<String, double[]> songVectors = new HashMap<>();
        for (String song : sortedSongs) {
            double sum = 0;
            int cnt = 0;
            for (String user : sortedUsers) {
                Double r = filled.get(user).get(song);
                if (r != null) { sum += r; cnt++; }
            }
            double mean = cnt > 0 ? sum / cnt : 0.0;

            double variance = 0;
            for (String user : sortedUsers) {
                Double r = filled.get(user).get(song);
                if (r != null) variance += (r - mean) * (r - mean);
            }
            double std = cnt > 0 ? Math.sqrt(variance / cnt) : 0.0;

            double[] vec = new double[numUsers];
            for (int ui = 0; ui < numUsers; ui++) {
                Double r = filled.get(sortedUsers.get(ui)).get(song);
                vec[ui] = (r != null && std != 0.0) ? (r - mean) / std : 0.0;
            }
            songVectors.put(song, vec);
        }

        double[][] centroids = new double[k][numUsers];
        for (int i = 0; i < k; i++) {
            centroids[i] = Arrays.copyOf(songVectors.get(userSongs.get(i)), numUsers);
        }

        List<List<String>> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());

        for (int iter = 0; iter < 10; iter++) {
            for (List<String> cluster : clusters) cluster.clear();

            for (String song : sortedSongs) {
                double[] vec = songVectors.get(song);
                int bestCluster = 0;
                double bestDist = Double.MAX_VALUE;
                for (int ci = 0; ci < k; ci++) {
                    double dist = UserDataUtils.euclideanDistanceVec(vec, centroids[ci]);
                    if (dist < bestDist) { bestDist = dist; bestCluster = ci; }
                }
                clusters.get(bestCluster).add(song);
            }

            for (int ci = 0; ci < k; ci++) {
                List<String> clusterSongs = clusters.get(ci);
                if (clusterSongs.isEmpty()) continue;
                double[] newCentroid = new double[numUsers];
                for (String song : clusterSongs) {
                    double[] vec = songVectors.get(song);
                    for (int ui = 0; ui < numUsers; ui++) newCentroid[ui] += vec[ui];
                }
                for (int ui = 0; ui < numUsers; ui++) newCentroid[ui] /= clusterSongs.size();
                centroids[ci] = newCentroid;
            }
        }

        Map<String, Integer> songCluster = new HashMap<>();
        for (int ci = 0; ci < k; ci++) {
            for (String song : clusters.get(ci)) songCluster.put(song, ci);
        }

        Map<String, Double> bestScore = new HashMap<>();

        for (String likedSong : userSongs) {
            int clusterIdx = songCluster.get(likedSong);
            double[] likedVec = songVectors.get(likedSong);

            for (String candidate : clusters.get(clusterIdx)) {
                if (userSongSet.contains(candidate)) continue;
                double dist = UserDataUtils.euclideanDistanceVec(songVectors.get(candidate), likedVec);
                double score = (dist == 0.0) ? Double.MAX_VALUE : 1.0 / dist;
                bestScore.merge(candidate, score, Math::max);
            }
        }

        List<Map.Entry<String, Double>> ranked = new ArrayList<>(bestScore.entrySet());
        ranked.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            int written = 0;
            for (Map.Entry<String, Double> entry : ranked) {
                if (written >= 20) break;
                writer.write(entry.getKey());
                writer.newLine();
                written++;
            }
        }
    }
}