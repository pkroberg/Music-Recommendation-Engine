import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility methods for argument validation, path checking, and CSV parsing.
 */
public class CsvUtils {
    public static void correctArguments(String[] args) throws Exception {
        if (args.length < 2) {
            throw new Exception("must enter at least 2 arguments");
        }

        if (!args[0].endsWith(".csv") || !args[1].endsWith(".csv")) {
            throw new Exception("input and output paths must have `.csv` extension");
        }

        if (args.length >= 3) {
            String flag = args[2];
            if (!flag.equals("-a") && !flag.equals("-u") && !flag.equals("-p")
                    && !flag.equals("-r") && !flag.equals("-s")) {
                throw new Exception("third argument must be -a, -u, -p, -r, or -s");
            }

            if (flag.equals("-s")) {
                if (args.length < 5) {
                    throw new Exception("must enter at least 5 arguments for playlist generation");
                }
                try {
                    int k = Integer.parseInt(args[3]);
                    if (k < 1) {
                        throw new Exception("K must be at least 1");
                    }
                } catch (NumberFormatException e) {
                    throw new Exception("K must be a valid integer");
                }
            }
        }
    }

    public static void ensurePaths(Path inputPath, Path outputPath) throws Exception {
        if (!Files.exists(inputPath)) {
            throw new Exception("input file DNE");
        }

        Path parent = outputPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            throw new Exception("output directory DNE");
        }
    }

    public static List<CSVRecord> readAndValidateCsv(Path inputPath) throws Exception {
        List<CSVRecord> records;

        try (Reader reader = Files.newBufferedReader(inputPath);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            records = parser.getRecords();
        }

        if (records.isEmpty()) {
            throw new Exception("file is empty");
        }

        for (CSVRecord record : records) {
            String ratingStr = record.get(2).trim();
            int rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5) {
                throw new Exception("rating must be between 1 and 5");
            }
        }

        return records;
    }
}