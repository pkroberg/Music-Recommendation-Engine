import java.util.*;

/**
 * Shared utility methods for user-rating normalization and similarity computation.
 */
public class UserDataUtils {

    public static void removeNonCooperativeUsers(
            Map<String, Map<String, Double>> userMap,
            Map<String, Set<Double>> ratingDiversity) {

        Iterator<String> iterator = userMap.keySet().iterator();
        while (iterator.hasNext()) {
            String user = iterator.next();
            if (ratingDiversity.get(user).size() < 2) {
                iterator.remove();
            }
        }
    }

    public static Map<String, Map<String, Double>> normalizeByUser(
            Map<String, Map<String, Double>> userMap) {

        Map<String, Map<String, Double>> normalized = new HashMap<>();

        for (String user : userMap.keySet()) {
            Collection<Double> ratings = userMap.get(user).values();

            double sum = 0;
            for (double r : ratings) sum += r;
            double mean = sum / ratings.size();

            double variance = 0;
            for (double r : ratings) variance += (r - mean) * (r - mean);
            double std = Math.sqrt(variance / ratings.size());

            Map<String, Double> normRatings = new HashMap<>();
            for (String song : userMap.get(user).keySet()) {
                double r = userMap.get(user).get(song);
                normRatings.put(song, (r - mean) / std);
            }
            normalized.put(user, normRatings);
        }

        return normalized;
    }

    public static Map<String, Map<String, Double>> computeUserSimilarity(
            List<String> sortedUsers,
            Map<String, Map<String, Double>> normalized) {

        Map<String, Map<String, Double>> userSimilarity = new HashMap<>();
        for (String u : sortedUsers) userSimilarity.put(u, new HashMap<>());

        for (int i = 0; i < sortedUsers.size(); i++) {
            for (int j = i + 1; j < sortedUsers.size(); j++) {
                String u1 = sortedUsers.get(i);
                String u2 = sortedUsers.get(j);
                double dist = euclideanDistance(normalized.get(u1), normalized.get(u2));
                userSimilarity.get(u1).put(u2, dist);
                userSimilarity.get(u2).put(u1, dist);
            }
        }

        return userSimilarity;
    }

    public static Map<String, List<String>> rankUsersBySimilarity(
            List<String> sortedUsers,
            Map<String, Map<String, Double>> userSimilarity) {

        Map<String, List<String>> mostSimilar = new HashMap<>();

        for (String target : sortedUsers) {
            List<String> others = new ArrayList<>(sortedUsers);
            others.remove(target);
            Map<String, Double> simMap = userSimilarity.get(target);

            others.sort((a, b) -> {
                double da = simMap.getOrDefault(a, Double.NaN);
                double db = simMap.getOrDefault(b, Double.NaN);
                boolean nanA = Double.isNaN(da), nanB = Double.isNaN(db);
                if (nanA && nanB) return 0;
                if (nanA) return 1;
                if (nanB) return -1;
                return Double.compare(da, db);
            });

            mostSimilar.put(target, others);
        }

        return mostSimilar;
    }

    public static double euclideanDistance(Map<String, Double> v1, Map<String, Double> v2) {
        double sum = 0;
        int count = 0;

        for (String key : v1.keySet()) {
            if (v2.containsKey(key)) {
                double diff = v1.get(key) - v2.get(key);
                sum += diff * diff;
                count++;
            }
        }

        return count == 0 ? Double.NaN : Math.sqrt(sum);
    }

    public static double euclideanDistanceVec(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    public static void computeUserMeanAndStd(
            Map<String, Map<String, Double>> userMap,
            Map<String, Double> outMean,
            Map<String, Double> outStd) {

        for (String user : userMap.keySet()) {
            Collection<Double> ratings = userMap.get(user).values();
            double sum = 0;
            for (double r : ratings) sum += r;
            double mean = sum / ratings.size();

            double variance = 0;
            for (double r : ratings) variance += (r - mean) * (r - mean);
            double std = Math.sqrt(variance / ratings.size());

            outMean.put(user, mean);
            outStd.put(user, std);
        }
    }
}