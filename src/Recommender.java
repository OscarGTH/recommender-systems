import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
// Importing utility classes
import java.util.*;

import javax.swing.plaf.synth.SynthSplitPaneUI;

import java.text.DecimalFormat;

public class Recommender {
    final static String genreSource = "ml-100k/u.genre";
    final static String dataSource = "ml-100k/u.data";
    final static String movieSource = "ml-100k/u.item";
    static User[] users;
    static Movie[] movies;
    static Helper helper;
    private static final DecimalFormat df = new DecimalFormat("0.0");

    public static void main(String[] args) throws Exception {

        System.out.println("Initializing movies...");
        initMovies(movieSource);
        System.out.println("Initializing users...");
        initUsers(dataSource);
        System.out.println("Initializing similarities...");
        initSimilarities();
        helper = new Helper();

        ArrayList<User> userGroup = new ArrayList<>();
        userGroup.add(users[23]);
        userGroup.add(users[256]);
        userGroup.add(users[377]);
        Group group = new Group(userGroup);
        System.out.println("Calculating recommendations for a group of users...\n");
        ArrayList<Integer> groupRecommendations = recommendGroup(group);
        runQuestioner(groupRecommendations, group);

    }

    public static void runQuestioner(ArrayList<Integer> recommendations, Group group) {
        System.out.println(
                "\nChoose your Why-not question and input the type number: (1) Atomic - (2) Genre - (3) Rank of movie");
        // Initializing scanner
        Scanner reader = new Scanner(System.in);
        // Reading type selection
        String type = reader.nextLine();
        switch (type) {
            case "1":
                System.out.println("Why wasn't some movie recommended?\n Input movie identifier: ");
                explainAtomic(reader.nextInt(), recommendations, group);
                break;
            case "2":
                System.out.println("Why wasn't some genre recommended?\n Input genre name: ");
                explainGroup(reader.nextLine(), recommendations, group);
                break;
            case "3":
                System.out.print("Why wasn't some movie in rank?\n Input movie identifier: ");
                Integer input3 = reader.nextInt();
                System.out.println("Input rank: ");
                Integer input4 = reader.nextInt();
                explainPositionAbsenteeism(input3, input4, recommendations, group);
                break;
            default:
                System.out.println("Invalid selection. Quitting program.");
                break;
        }
        reader.close();
    }

    public static void explainAtomic(Integer movieId, ArrayList<Integer> recommendations, Group group) {
        // Validating information
        if (movieId > 0 && movieId <= movies.length) {
            ArrayList<ArrayList<Float>> resultSet = new ArrayList<>();

            for (User user : group.getUsers()) {
                resultSet.add(determineAtomic(user.getPeerTuples(users, movieId, 100)));
            }
            // Turn individual peer statistics into group statistics.
            ArrayList<Float> explData = new ArrayList<>();
            explData = structureAtomic(resultSet);

            // Explanation: Recommended movies contain the queried movie.
            if (recommendations.contains(movieId)) {
                System.out.println("Queried movie is included in recommendations already.");
            }

            // Explanation : User asked for few items (Movie was found from ranks 21 to 40.)
            else if (group.getTop40Movies().contains(movieId)) {
                System.out.println("Movie was found from top 40 recommendations.");
            }

            // Explanation : No peers of the group's members have rated the item.
            else if (explData.get(3) == 0) {
                System.out.println("None of the similar users of the group's members have rated this item.");
            }

            // Explanation : Too few ratings from peers of the group.
            else if (explData.get(3) < 10) {
                System.out.println("Too few similar users of the group's members have rated this item.");
            }

            // Explanation : Dislike ratio of peers is too high (Majority of dislikes)
            else if (explData.get(2) > explData.get(3) / 2) {
                System.out.println("More than half of the group's peers have disliked this item.");
            }

            // Explanation : Peers of the group's users haven't like the item
            else if (explData.get(0) < 4) {
                System.out.println("Similar users of group's members haven't liked this item.");
            }

            // Statistics for debugging.
            System.out.println("Average of peer ratings was " + Float.toString(explData.get(0)));
            System.out.println("Dislike count was " + Float.toString(explData.get(2)));
            System.out.println("Like count was " + Float.toString(explData.get(1)));
            System.out.println("Total peer count was " + Float.toString(explData.get(3)));

        } else {
            // Explanation : Movie does not exist.
            System.out.println("The movie id does not exist in the database.");
        }
    }

    // Input: List of lists that contain peer statistics for a single user.
    // Output: List of values that have been combined for the group.
    public static ArrayList<Float> structureAtomic(ArrayList<ArrayList<Float>> resultSet) {
        Float totalCount = 0.0f;
        Float totalDislikes = 0.0f;
        Float totalLikes = 0.0f;
        Float average = 0.0f;
        // Going through each user's statistics
        for (ArrayList<Float> stats : resultSet) {
            average += stats.get(0);
            totalLikes += stats.get(1);
            totalDislikes += stats.get(2);
            totalCount += stats.get(3);
        }

        ArrayList<Float> structuredData = new ArrayList<>();
        structuredData.add(average / resultSet.size());
        structuredData.add(totalLikes);
        structuredData.add(totalDislikes);
        structuredData.add(totalCount);
        // Returning combined statistics for the group
        return structuredData;
    }

    public static void explainGroup(String genre, ArrayList<Integer> recommendations, Group group) {
        // Validating information
        if (genre.length() > 0) {
            System.out.println("GENRE CHOSEN ");
        }

    }

    public static ArrayList<Float> determineAtomic(ArrayList<ArrayList<Float>> tuples) {

        // Creating new tuple for helper data
        // Contains average of peer ratings [0], peer like count [1],
        // peer dislike count [2] and
        // total count [3].
        ArrayList<Float> explanationTuple = new ArrayList<>();
        // Initialize helper variables
        Float totalScore = 0.0f;
        Integer likeCount = 0;
        Integer dislikeCount = 0;
        Float avg = totalScore / tuples.size();
        // Check that there are more than 0 tuples
        if (tuples.size() > 0) {
            // Iterate tuples
            for (ArrayList<Float> tuple : tuples) {
                Float score = tuple.get(1);
                // Sum up score (Could we use similarity as weight?)
                totalScore += tuple.get(1);

                // Calculate dislike to like ratio using 4 as the threshold
                if (score >= 4) {
                    likeCount++;
                } else {
                    dislikeCount++;
                }
            }
            avg = totalScore / tuples.size();
        }
        explanationTuple.add(avg);
        explanationTuple.add((float) likeCount);
        explanationTuple.add((float) dislikeCount);
        explanationTuple.add((float) tuples.size());
        return explanationTuple;
    }

    public static void explainPositionAbsenteeism(Integer movieId, Integer rank, ArrayList<Integer> recommendations,
            Group group) {
        // Validating information
        if (movieId > 0 && movieId <= movies.length && rank > 0 && rank <= movies.length) {
            System.out.println("Position absenteeism CHOSEN ");
        }
    }

    public static void initSimilarities() {
        int i;
        int x;
        // Iterate through each user
        for (i = 1; i < users.length; i++) {
            // Process similarities for each user
            for (x = 1; x < users.length; x++) {
                // Make sure indexes are not same.
                if (i != x) {
                    calculateSimilarity(users[i], users[x]);
                }
            }
        }
    }

    public static void initUsers(String dataSource) {
        // User data is saved to index which matches to userId
        users = new User[944];
        ArrayList<Integer> tempUserIds = new ArrayList<Integer>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataSource));

            while (reader.ready()) {
                String line = reader.readLine();
                String[] lineData = line.split("\\s+");
                Integer userId = Integer.valueOf(lineData[0]);
                Integer movieId = Integer.valueOf(lineData[1]);
                Integer rating = Integer.valueOf(lineData[2]);

                if (tempUserIds.contains(userId)) {
                    users[userId].setMovieAsRated(movieId);
                    users[userId].setRating(movieId, rating);
                } else {
                    User user = new User(userId);
                    user.setRating(movieId, rating);
                    user.setMovieAsRated(movieId);
                    tempUserIds.add(userId);
                    users[userId] = user;
                }
                // Setting the rating for the movie
                movies[movieId].setRating(rating);
            }

            if (reader != null) {
                reader.close();
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        } catch (IOException ioExc) {
            System.out.println(ioExc);
        }

    }

    public static void initMovies(String movieSource) {
        // Movie data is saved to index which matches to movie id
        movies = new Movie[1683];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(movieSource));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] lineData = line.split("\\|");
                Integer movieId = Integer.valueOf(lineData[0]);
                String name = lineData[1];

                // Creating new movie object
                Movie movie = new Movie(movieId);
                movie.setName(name);
                movies[movieId] = movie;

            }
            if (reader != null) {
                reader.close();
            }

        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        } catch (IOException ioExc) {
            System.out.println(ioExc);
        }

    }

    // prints data of n first users
    public static void printUsers(int n) {

        for (int i = 1; i < n; i++) {
            System.out.print("User id: ");
            System.out.println(users[i].getUserId());
            System.out.print("Ratings : ");
            System.out.println(users[i].getRatingData());
            System.out.println("Total ratings: " + users[i].getRatedMovies().size());
        }
    }

    public static void getRatingStatsForMovie(int movieId) {
        Movie movie = movies[movieId];
        System.out.println("\nRating statistics for movie: " + movie.getName());
        System.out.println("The movie has " + Integer.toString(movie.getRatingCount()) + " ratings.");
        System.out.println("Sum: " + Float.toString(movie.getRatingSum()));
        System.out.println("Square Sum: " + Float.toString(movie.getRatingSquareSum()));
        System.out.println("Mean: " + Float.toString(movie.getRatingMean()));
    }

    // Prints line count of the dataset
    public static void getLineCount(String dataSource) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataSource));
            long lineCount = 0;

            while ((reader.readLine()) != null) {
                lineCount++;
            }
            reader.close();
            System.out.println("Line count is " + lineCount);
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        } catch (IOException ioExc) {
            System.out.println(ioExc);
        }
    }

    // Prints first 5 rows of the dataset
    public static void readMovieData(String dataSource) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataSource));
            int i = 0;
            System.out.println("user id | item id | rating | timestamp");
            while (reader.ready() && i < 5) {
                System.out.println(reader.readLine());
                i++;
            }

            if (reader != null) {
                reader.close();
            }
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        } catch (IOException ioExc) {
            System.out.println(ioExc);
        }
    }

    /**
     * Predicts movie rating of a given movie for a given user.
     * 
     * @param user1   User object
     * @param movieId Movie ID that the predicted rating is calculated for.
     * @return Predicted rating for a movie.
     */
    public static float predictRating(User user1, Integer movieId) {

        ArrayList<Integer> neighbors = user1.getKSimilarUsers(200);
        // Top part of prediction formula
        float top = 0.0f;
        // Bottom part of prediction formula
        float bot = 0.0f;
        // Prediction value
        float prediction = 0.0f;

        // Iterating through neighbors
        for (Integer neighbor : neighbors) {
            User neighborObject = users[neighbor];
            // Check if neighbor has rated the movie
            if (neighborObject.hasRatedMovie(movieId)) {
                // Get similarity of neighbor
                float similarity = user1.getSimilarity(neighbor);
                // Neighbor's rating of the movie
                Integer rating = neighborObject.getRatingForMovie(movieId);
                top += similarity * rating;
                bot += similarity;
            }
        }
        if (top != 0 && bot != 0) {
            prediction = top / bot;
            user1.setPrediction(movieId, prediction);
        }
        return prediction;
    }

    /**
     * Calculate cosine similarity between two items (movies).
     * 
     * 
     * @param item1ratings List of ratings for item 1 (item which user has not
     *                     rated).
     * @param item2ratings List of ratings for item 2 (item which user has rated).
     * @return Cosine similarity value between two items (movies).
     */
    public static float cosineSimilarity(ArrayList<Integer> item1ratings, ArrayList<Integer> item2ratings) {
        float similarity = 0.0f;

        // if similarity can't be computed return -2
        if (item1ratings.size() == 0 || item2ratings.size() == 0 || item1ratings.size() != item2ratings.size()) {
            return -2;
        }

        int crossSum = 0;
        int squareSum1 = 0;
        int squareSum2 = 0;

        for (int i = 0; i < item1ratings.size(); i++) {
            // cross sum of item 1 and 2 ratings
            crossSum += item1ratings.get(i) * item2ratings.get(i);
            // square sum of item 1 ratings
            squareSum1 += item1ratings.get(i) * item1ratings.get(i);
            // sqaure sum of item 2 ratings
            squareSum2 += item2ratings.get(i) * item2ratings.get(i);
        }

        similarity = (float) crossSum / (float) (Math.sqrt(squareSum1) * Math.sqrt(squareSum2));

        System.out.println("Cosine similarity:");
        System.out.println(similarity);

        return similarity;
    }

    public static ArrayList<Integer> recommendGroup(Group group) {
        ArrayList<User> users = group.getUsers();
        // Iterating through every user of group
        for (User user : users) {
            for (Integer movieId : user.getUnseenMovies()) {
                predictRating(user, movieId);
            }
        }
        // Calculate group recommendations by average aggregation.
        System.out.println("Average approach Top 20:\n");
        return averageAggregation(group);

        // System.out.println("-------------------------");
        // System.out.println("Least misery approach Top 20:\n");
        // leastMiseryAggregation(users);
        // System.out.println("-------------------------");
        // System.out.println("Treshold Disagreement approach Top 20:\n");
        // thresholdDisagreementAggregation(users, 1.5f);
        // System.out.println("-------------------------");
        // sequentialRecommendation(group);
    }

    public static ArrayList<Integer> averageAggregation(Group group) {
        // (Movie ID : Predicted rating for group)
        HashMap<Integer, Float> groupRecommendations = new HashMap<>();
        int i;
        // Iterating over every movie
        for (i = 1; i < movies.length; i++) {
            // Creating movie object
            Movie movie = movies[i];
            boolean validMovie = true;
            // Getting movie id
            Integer movieId = movie.getMovieId();
            Float sum = 0.0f;
            // Iterating through every user of the group
            for (User user : group.getUsers()) {
                Float rating = user.getPredictionForMovie(movieId);
                Integer realRating = user.getRatingForMovie(movieId);
                // Checking if user hasn't rated or doesn't have prediction for the movie
                if (rating == 0.0f && realRating == 0) {
                    validMovie = false;
                } else if (rating == 0.0f) {
                    sum += realRating;
                } else {
                    sum += rating;
                }
            }
            // If ratings for the movie were found from every user of the group, continue.
            if (validMovie) {
                Float prediction = sum / group.getUsers().size();
                // Adding prediction for the movie.
                groupRecommendations.put(movieId, prediction);
                group.setRecommendedMovie(movieId);
            }
        }
        // Getting top 20 recommended movies
        ArrayList<Integer> movieRecommendations = helper.getKSlice(20, groupRecommendations);

        // Save 40 movies to save for later
        group.setTop40Movies(helper.getKSlice(40, groupRecommendations));

        // Printing every movie with their name and predicted rating.
        for (Integer movieId : movieRecommendations) {
            System.out.println("[" + Integer.toString(movieId) + "]:" + movies[movieId].getName()
                    + "  > Predicted rating: " + df.format(groupRecommendations.get(movieId)));
        }
        return movieRecommendations;
    }

    public static void leastMiseryAggregation(ArrayList<User> userGroup) {

        // (Movie ID : Predicted rating for group)
        HashMap<Integer, Float> groupRecommendations = new HashMap<>();
        int i;
        // Iterating over every movie
        for (i = 1; i < movies.length; i++) {
            // Creating movie object
            Movie movie = movies[i];
            boolean validMovie = true;
            // Getting movie id
            Integer movieId = movie.getMovieId();
            Float smallest = null;

            // Iterating through every user of the group
            for (User user : userGroup) {
                Float predictedRating = user.getPredictionForMovie(movieId);

                Integer realRating = user.getRatingForMovie(movieId);

                // Checking if user hasn't rated or doesn't have prediction for the movie
                if (predictedRating == 0.0f && realRating == 0) {
                    validMovie = false;
                    // Checking for user's real rating and whether it's the lowest of the group
                } else if (predictedRating == 0.0f) {
                    if (smallest == null || (float) realRating < smallest)
                        smallest = (float) realRating;
                    // Checking for user's predicted rating and whether it's the lowest of the group
                } else if (smallest == null || predictedRating < smallest) {
                    smallest = predictedRating;
                }
            }
            // If ratings for the movie were found from every user of the group, continue.
            if (validMovie) {

                Float prediction = smallest;
                // Adding prediction for the movie.
                groupRecommendations.put(movieId, prediction);
            }
        }
        // Getting top 20 recommended movies
        ArrayList<Integer> movieRecommendations = helper.getKSlice(20, groupRecommendations);
        // Printing every movie with their name and predicted rating.
        for (Integer movieId : movieRecommendations) {
            System.out.println(movies[movieId].getName() + "  > Predicted rating: "
                    + df.format(groupRecommendations.get(movieId)));
        }
    }

    /**
     * Perform threshold disagreement aggregation for group movie ratings.
     * 
     * 
     * @param userGroup List of user objects.
     * 
     * @param threshold Floating point value to adjust the algorithm.
     * 
     * @return void.
     */
    public static void thresholdDisagreementAggregation(ArrayList<User> userGroup, Float threshold) {

        // (Movie ID : Predicted rating for group)
        HashMap<Integer, Float> groupRecommendations = new HashMap<>();
        int i;
        // Iterating over every movie
        for (i = 1; i < movies.length; i++) {
            // Creating movie object
            Movie movie = movies[i];
            // Flag to mark a valid movie (Every user has a rating or prediction for it)
            boolean validMovie = true;
            // Counter for disagreements.
            Integer disagreements = 0;
            // Getting movie id
            Integer movieId = movie.getMovieId();
            // List of collected ratings from each user in the group.
            List<Float> collectedRatings = new ArrayList<>();
            Float sum = 0.0f;
            Float predictSum = 0.0f;

            // Iterating through every user of the group
            for (User user : userGroup) {
                // Getting a rating for a movie from the user object.
                Float predictedRating = user.getPredictionForMovie(movieId);
                Float realRating = user.getRatingForMovie(movieId).floatValue();
                // Checking that movie has been rated by or been predicted for user.
                validMovie = helper.checkMovieValidity(user, movieId);
                if (validMovie) {
                    // Setting ratings as real rating or predicted depending on which exists.
                    Float currentRating = realRating != 0 ? realRating : predictedRating;
                    // Increasing sum.
                    sum += currentRating;
                    // Collecting rating score into a list.
                    collectedRatings.add(currentRating);
                }
            }

            // If ratings for the movie were found from every user of the group, continue.
            if (validMovie) {
                // Hold unprocessed ratings.
                Integer remainingRatings = collectedRatings.size();
                // Looping while there are enough ratings.
                while (remainingRatings > 1) {
                    // Getting max value of ratings
                    Float max = Collections.max(collectedRatings);
                    // Getting min value ratings
                    Float min = Collections.min(collectedRatings);
                    // If absolute value of max subtracted from min
                    // is over the treshold, we have a disagreement.
                    if (Math.abs(min - max) >= threshold) {
                        // Calculate average of their ratings and sum it up.
                        predictSum += (min + max) / 2;
                        // Increase the number of disagreements.
                        disagreements += 1;
                    }
                    // Removing values that have been processed.
                    collectedRatings.remove(max);
                    collectedRatings.remove(min);
                    // Remove 2 from remaining ratings, since 2 values (min and max) were processed.
                    remainingRatings -= 2;
                }
                // Using average of disagreements as predictor if disagreements were found,
                // otherwise use average aggregation as predictor.
                Float prediction = disagreements > 0 ? predictSum / disagreements : sum / userGroup.size();
                // Adding prediction for the movie.
                groupRecommendations.put(movieId, prediction);
            }
        }

        // Getting top 20 recommended movies
        ArrayList<Integer> movieRecommendations = helper.getKSlice(20, groupRecommendations);
        // Printing every movie with their name and predicted rating.
        for (Integer movieId : movieRecommendations) {
            System.out.println(movies[movieId].getName() + "  > Predicted rating: "
                    + df.format(groupRecommendations.get(movieId)));
        }
    }

    /**
     * Perform sequential recommendation for a group. Prints top-20 recommendations
     * for the group in 5 iterations.
     * 
     * @param group Group object that contains n users.
     * 
     * @return void.
     */
    public static void sequentialRecommendation(Group group) {
        int i;
        float alpha = 0.0f;
        float threshold = 2.0f;
        // recommendations per iteration
        ArrayList<Integer> iterationRecs = new ArrayList<>();
        ArrayList<Float> iterationSatValues = new ArrayList<>();
        // 5 rounds for computing recommendations + extra round for computing
        // satisfaction values for the last round
        for (i = 0; i < 6; i++) {
            // satisfaction and alpha are computed after first iteration
            if (i > 0) {
                iterationSatValues = calculateUsersSatisfaction(group, iterationRecs, i + 1);
                alpha = Collections.max(iterationSatValues) - Collections.min(iterationSatValues);
                System.out.println("alpha " + alpha);
                System.out.println("--------------------------\n");
            }
            if (i < 5) {
                System.out.println("Recommendations for iteration " + (i + 1) + "\n");
                iterationRecs.clear();
                iterationRecs = sequentialAggregation(group, threshold, alpha);
            }
        }
    }

    /**
     * Computes individual satisfaction value of iteration for every user in the
     * group and finally computes the mean of those values and sets it as a group
     * satisfaction value for iteration
     * 
     * @param group                Group object
     * @param groupRecommendations List of movies that were recommended for group in
     *                             one iteration
     * @param iteration            Iteration round
     * @return List of individual satisfaction values of all users in the group
     */
    public static ArrayList<Float> calculateUsersSatisfaction(Group group, ArrayList<Integer> groupRecommendations,
            Integer iteration) {

        System.out.println("\n");
        group.clearUserIterationSat();
        // sum of users' individual satisfaction values
        Float totalSum = 0.0f;

        for (User user : group.getUsers()) {
            // sum of user's predictions of top 20 group recommendations
            Float sumGroup = 0.0f;
            // sum of user's predictions of top 20 own preferences
            Float sumOwn = 0.0f;

            // list of user's individual preferences for iteration (movies that would have
            // been ideal for this user)
            ArrayList<Integer> preferences = new ArrayList<>();
            if (iteration == 1) {
                preferences = user.getKRecommendedMovies(20);
            } else {
                // get user's top 20 preferences (movies that has not yet been recommended)
                for (Integer movieId : user.getKRecommendedMovies(20 * iteration)) {
                    if (!group.getRecommendedMovies().contains(movieId) && preferences.size() < 20) {
                        preferences.add(movieId);
                    }
                }
            }

            for (Integer movieId : groupRecommendations) {
                sumGroup += user.getPredictionForMovie(movieId);
            }
            for (Integer movieId : preferences) {
                sumOwn += user.getPredictionForMovie(movieId);
            }

            Float userSatisfaction = sumGroup / sumOwn;
            // sum for computing total iteration satisfaction
            totalSum += userSatisfaction;
            System.out.println("User (id: " + user.getUserId() + ") satisfaction: " + userSatisfaction);
            group.setUserIterationSat(userSatisfaction);
        }
        // setting total iteration satisfaction value for group
        Float totalIterationSat = totalSum / group.getUsers().size();
        group.setTotalIterationSat(totalIterationSat);
        System.out.println("Group's total satisfaction of iteration: " + totalIterationSat);

        return group.getUserIterationSat();
    }

    /**
     * Calculate similarity between two users.
     * 
     * Returns NaN if either of the arrays has zero variance (i.e., if one of the
     * arrays does not contain at least two distinct values)
     * 
     * @param user1 User object 1
     * @param user2 User object 2
     * @return Pearson's correlation coefficient for two arrays (users).
     */
    public static float calculateSimilarity(User user1, User user2) {
        ArrayList<Integer> common = user1.getCommonMovies(user2);

        // common array length should be higher than two to compute correlation
        // coefficient
        if (common.size() > 2) {
            // computing mean of ratings for user1 and user2
            int sumUser1 = 0;
            int sumUser2 = 0;
            float meanUser1 = 0.0f;
            float meanUser2 = 0.0f;

            for (int i = 0; i < common.size(); i++) {
                sumUser1 += user1.getRatingForMovie(common.get(i));
                sumUser2 += user2.getRatingForMovie(common.get(i));
            }
            // computed means
            meanUser1 = sumUser1 / common.size();
            meanUser2 = sumUser2 / common.size();

            // top part of similarity formula
            float sumTop = 0.0f;
            // first part of bottom
            float sumBottom1 = 0.0f;
            // second part of bottom
            float sumBottom2 = 0.0f;
            // bottom part of formula
            float bottom = 0.0f;

            // iterating through movies rated by both and computing sums for formula
            for (int i = 0; i < common.size(); i++) {
                // (user1 rating - user1 average) - (user2 rating - user2 average)
                sumTop += (user1.getRatingForMovie(common.get(i)) - meanUser1)
                        * (user2.getRatingForMovie(common.get(i)) - meanUser2);
                // square of (user1 rating - user1 average)
                sumBottom1 += Math.pow(user1.getRatingForMovie(common.get(i)) - meanUser1, 2);
                // square of (user2 rating - user2 average)
                sumBottom2 += Math.pow(user2.getRatingForMovie(common.get(i)) - meanUser2, 2);
            }

            bottom = (float) Math.sqrt(sumBottom1) * (float) Math.sqrt(sumBottom2);

            float similarity = sumTop / bottom;
            user1.setSimilarity(user2.getUserId(), similarity);
            return similarity;
        } else {
            return 0;
        }
    }

    /**
     * Computes prediction scores for group by using hybrid aggregation method
     * combining threshold disagreement aggregation and least misery aggregation.
     * Alpha is used as a weight and set according to group's satisfaction of
     * previous iteration.
     * 
     * 
     * @param group     Group object
     * @param threshold Floating point value to adjust the treshold disagreement
     *                  algorithm
     * @param alpha     Float value set between 0 and 1. 0 = treshold disagreement
     *                  method is weighted 1 = least misery method is weighted
     * @return List of top 20 movie recommendations for group in one iteration
     */
    public static ArrayList<Integer> sequentialAggregation(Group group, Float threshold, Float alpha) {

        // (Movie ID : Predicted rating for group)
        HashMap<Integer, Float> groupRecommendations = new HashMap<>();
        // Movies that has already been recommended for this group
        ArrayList<Integer> recommendedMovies = group.getRecommendedMovies();
        int i;
        // Iterating over every movie
        for (i = 1; i < movies.length; i++) {
            // Creating movie object
            Movie movie = movies[i];
            // Flag to mark a valid movie (None of the users has seen the movie and has
            // prediction for it)
            boolean validMovie = true;
            // Counter for disagreements (treshold disagreement method)
            Integer disagreements = 0;
            // Getting movie id
            Integer movieId = movie.getMovieId();
            // List of collected predictions from each user in the group
            List<Float> collectedRatings = new ArrayList<>();
            Float sum = 0.0f;
            Float predictSum = 0.0f;

            // Checking that movie has not already been recommended for the group
            if (!recommendedMovies.contains(movieId)) {
                // Iterating through every user of the group
                for (User user : group.getUsers()) {
                    // Getting prediction for a movie from the user object.
                    Float predictedRating = user.getPredictionForMovie(movieId);
                    // Movie is valid if user has not yet seen it
                    validMovie = !user.hasRatedMovie(movieId) && predictedRating != 0.0;
                    if (validMovie) {
                        // Increasing sum
                        sum += predictedRating;
                        // Collecting predicted ratings into a list.
                        collectedRatings.add(predictedRating);
                    }
                }

                // If none of the users had seen the movie, continue
                if (validMovie) {
                    // Hold unprocessed ratings (treshold disagreement method)
                    Integer remainingRatings = collectedRatings.size();
                    Float leastMisery = Collections.min(collectedRatings);
                    // Looping while there are enough ratings
                    while (remainingRatings > 1) {
                        // Getting max value of ratings
                        Float max = Collections.max(collectedRatings);
                        // Getting min value ratings
                        Float min = Collections.min(collectedRatings);
                        // If absolute value of max subtracted from min
                        // is over the treshold, we have a disagreement
                        if (Math.abs(min - max) >= threshold) {
                            // Calculate average of their ratings and sum it up.
                            predictSum += (min + max) / 2;
                            // Increase the number of disagreements.
                            disagreements += 1;
                        }
                        // Removing values that have been processed.
                        collectedRatings.remove(max);
                        collectedRatings.remove(min);
                        // Remove 2 from remaining ratings, since 2 values (min and max) were processed.
                        remainingRatings -= 2;
                    }
                    // Using average of disagreements as predictor if disagreements were found,
                    // otherwise use average aggregation as predictor.
                    Float firstPart = disagreements > 0 ? (predictSum / disagreements)
                            : (sum / group.getUsers().size());

                    // Hybrid aggregation method which uses weighted combination of our own approach
                    // and least misery approach
                    Float prediction = ((1 - alpha) * firstPart) + (alpha * leastMisery);

                    // Adding prediction for the movie.
                    groupRecommendations.put(movieId, prediction);
                }

            }
        }
        // Getting top 20 recommended movies
        ArrayList<Integer> movieRecommendations = helper.getKSlice(20, groupRecommendations);
        // Keeping track of movies that have been recommended for group
        group.setTop20Movies(movieRecommendations);
        for (Integer movieId : movieRecommendations) {
            System.out.println(movies[movieId].getName() + "  > Predicted rating: "
                    + df.format(groupRecommendations.get(movieId)));
        }
        return movieRecommendations;
    }
}
