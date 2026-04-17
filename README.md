# PA6 - Cluster proximity playlist

## Motivation

In PA5, you implemented K-means clustering to group similar songs based on normalized user ratings (after filling in missing ratings and then re-normalizing). Now, we'll use these clusters to generate personalized recommendations for new users of your streaming platform! When a user tells us they like specific songs, we can quickly find other songs they might enjoy by leveraging song proximity within clusters. The goal is to make a playlist of up to 20 songs that our user might like.

_Notice: This assignment has shorter instructions than usual, which can be a common scenario in real-world software development. While the assignment is a quite simple extension of PA5, your challenge is to come up with your own implementation tasks to generate the corresponding output as seen in the input/output examples._

## Input Format

Your program must accept 5 or more command-line arguments, with K being the number of user-provided songs as initial centroids. 

`gradle run -q --args="'<input_file>' '<output_file>' '-s' 'K' '<liked_song_name1>' '<liked_song_name2>' ..."`

This means for our clustering process, the initialization uses the first `K` user-provided songs as initial centroids. The rest of the clustering process is the same as in PA5 (10 iterations, Euclidean distance).

## Playlist Generation
After clustering, find the (up to) 20 songs that are closest to all our user-provided songs (not the `K` first user-provided songs). Follow the provided pseudo-code algorithm. This algorithm represents the idea of the program conceptually, but depending on your previous code structure you may have to change the details of some steps entirely.


```
# For ALL user-provided songs (including those beyond first K)
For each user_liked_song s in user_provided_songs:
	For each song t in the same cluster as s:
	    If (t != s)
			Let d = 1.0 / euclidean_distance(t, s)
			Keep best (max) d for each candidate song
Create list of all candidate songs (excluding user-provided songs) with their best distances
Sort list by d, write the (up to) top 20 songs to output file 
```

## Error Handling

- All errors from PA5 still apply
- If argument -s is present for playlist generation throw an error in these cases:
	- Fewer than 5 arguments present
	- K < 1
	- Fewer user-provided songs than K
	- User-provided song not found
	- Duplicate song titles in input
	- Pre-processing leaves fewer than K+1 songs

# Example Input/Output

Note: All features from previous PAs are still expected to work.

---
command
`gradle run -q --args="'database/files/rock.csv' 'playlist.csv' '-s' '1' 'Bohemian Rhapsody'"`

rock.csv
```
Bohemian Rhapsody,user1,5
Bohemian Rhapsody,user2,4
Bohemian Rhapsody,user3,5
Stairway to Heaven,user1,5
Stairway to Heaven,user2,4
Stairway to Heaven,user3,5
Hotel California,user1,5
Hotel California,user2,4
Hotel California,user3,3
November Rain,user1,1
November Rain,user2,5
November Rain,user3,1
```

playlist.csv
```
Stairway to Heaven
Hotel California
November Rain
```

---
`gradle run -q --args="'database/files/rockpop.csv' 'rock_playlist.csv' '-s' '2' 'Bohemian Rhapsody' 'Stairway to Heaven'"`

rockpop.csv

```
Bohemian Rhapsody,rock_fan1,5
Bohemian Rhapsody,rock_fan2,4
Bohemian Rhapsody,rock_fan3,5
Bohemian Rhapsody,pop_fan1,2
Bohemian Rhapsody,pop_fan2,1
Stairway to Heaven,rock_fan1,4
Stairway to Heaven,rock_fan2,5
Stairway to Heaven,rock_fan3,4
Stairway to Heaven,pop_fan1,1
Stairway to Heaven,pop_fan2,2
Sweet Child O' Mine,rock_fan1,5
Sweet Child O' Mine,rock_fan2,4
Sweet Child O' Mine,rock_fan3,3
Sweet Child O' Mine,pop_fan1,1
Sweet Child O' Mine,pop_fan2,1
November Rain,rock_fan1,4
November Rain,rock_fan2,5
November Rain,rock_fan3,5
November Rain,pop_fan1,2
November Rain,pop_fan2,3
Hotel California,rock_fan1,3
Hotel California,rock_fan2,4
Hotel California,rock_fan3,2
Hotel California,pop_fan1,1
Hotel California,pop_fan2,1
Bad Romance,pop_fan1,5
Bad Romance,pop_fan2,4
Bad Romance,rock_fan1,1
Bad Romance,rock_fan2,2
Poker Face,pop_fan1,4
Poker Face,pop_fan2,5
Poker Face,rock_fan1,2
Poker Face,rock_fan2,1
Shallow,pop_fan1,3
Shallow,pop_fan2,4
Shallow,rock_fan1,1
Shallow,rock_fan2,1
```

rock_playlist.csv

```
November Rain
Hotel California
Sweet Child O' Mine
```

---
`gradle run -q --args="'database/files/rockpop.csv' 'mixed_playlist.csv' '-s' '2' 'Bohemian Rhapsody' 'Bad Romance'"`

rockpop.csv _(see above)_

mixed_playlist.csv

```
Shallow
Sweet Child O' Mine
Poker Face
Stairway to Heaven
November Rain
Hotel California
```
