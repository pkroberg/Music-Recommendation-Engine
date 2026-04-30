import org.apache.commons.csv.CSVRecord;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Cs214Project {

    public static void main(String[] args) {
        try {
            CsvUtils.correctArguments(args);

            Path inputPath  = Paths.get(args[0]);
            Path outputPath = Paths.get(args[1]);

            CsvUtils.ensurePaths(inputPath, outputPath);

            List<CSVRecord> records = CsvUtils.readAndValidateCsv(inputPath);

            String flag = args.length >= 3 ? args[2] : "";

            switch (flag) {
                case "-a":
                    UserAnalysisRunner.run(records, outputPath);
                    break;
                case "-u":
                    SongSimilarityRunner.run(records, outputPath);
                    break;
                case "-p":
                    UserPredictionsRunner.run(records, outputPath);
                    break;
                case "-r":
                    List<String> selections = new ArrayList<>();
                    for (int i = 3; i < args.length; i++) selections.add(args[i]);
                    RecommendationsRunner.run(records, outputPath, selections);
                    break;
                case "-s":
                    int k = Integer.parseInt(args[3]);
                    List<String> userSongs = new ArrayList<>();
                    for (int i = 4; i < args.length; i++) userSongs.add(args[i]);
                    PlaylistRunner.run(records, outputPath, k, userSongs);
                    break;
                default:
                    SongStatsRunner.run(records, outputPath);
                    break;
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}