# Music Recommendation Engine

A Java command-line tool that takes raw user song ratings and turns them into statistics, rating predictions, recommendations and personalized playlists. It uses collaborative filtering and K-means clustering.

## My contribution

This started as a course project (CS 214). The course provided the starting scaffold: the project layout, the Gradle and CI configuration, and the base CLI entry point. **I implemented all five analysis features** (`-a`, `-u`, `-p`, `-r`, `-s`) and the shared data and math utilities they use (`UserDataUtils`, `CsvUtils`).

## Highlights

- **I wrote the recommendation algorithms myself, without ML libraries:** per-user z-score normalization, user-similarity collaborative filtering, K-means clustering and inverse-distance ranking.
- **The data runs through a multi-stage pipeline:** validate → clean → normalize → model → write CSV.
- **Bad input fails with a clear error message:** unknown or duplicate songs, ratings outside 1–5, too few songs for the requested K, and missing files.
- **The code grew over six iterations.** Each feature has its own runner class, and the math they share lives in one utility class.

## How it works

Input is a CSV of `song,user,rating` rows (ratings 1–5). A flag selects the analysis:

| Flag | Feature | Technique |
|---|---|---|
| `-a` | Data cleaning | Drops users who gave every song the same rating, then builds the full user × song matrix |
| `-u` | Song similarity | Normalizes each user's ratings (z-score), then computes the distance between every pair of songs |
| `-p` | Rating prediction | User-based collaborative filtering: fills each missing rating from the most similar user, then converts it back to a 1–5 rating |
| `-r` | Recommendations | K-means clustering of songs, using the user's picks as the starting centroids |
| `-s` | Playlist generation | K-means, then ranks songs in the same cluster as the user's picks by 1 / distance and keeps the top 20 |

With no flag, it outputs each song's rating count, mean and standard deviation.

## Quick start

```bash
gradle run -q --args="'database/files/rockpop.csv' 'playlist.csv' '-s' '2' 'Bohemian Rhapsody' 'Stairway to Heaven'"
```

Given a dataset of rock fans and pop fans, two liked rock songs produce a rock playlist:

```
November Rain
Hotel California
Sweet Child O' Mine
```

Sample datasets are in `database/files/`.

## Tech stack

Java · Gradle · Apache Commons CSV · PMD, JaCoCo and GitHub Actions (from the course setup)
