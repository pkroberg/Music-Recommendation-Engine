/**
 * Tracks count, mean, and standard deviation for a song's ratings
 * using Welford's online algorithm.
 */
public class SongStats {

    private int count;
    private double avg;
    private double sumSquares;

    public SongStats() {
        count = 0;
        avg = 0.0;
        sumSquares = 0.0;
    }

    public void add(double rating) {
        count++;
        double diff = rating - avg;
        avg = avg + diff / count;
        double diff2 = rating - avg;
        sumSquares = sumSquares + diff * diff2;
    }

    public int getCount() {
        return count;
    }

    public double getMean() {
        return avg;
    }

    public double getStandDev() {
        if (count == 0) return 0.0;
        return Math.sqrt(sumSquares / count);
    }
}