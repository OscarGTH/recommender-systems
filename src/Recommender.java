import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
// Importing utility classes
import java.util.HashMap;
import java.util.*;
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
        // getLineCount(dataSource);
        // readMovieData(dataSource);
        // findSimilarAndPredict(156, dataSource);
        helper = new Helper();
        System.out.println("Initializing movies...");
        initMovies(movieSource);
        System.out.println("Initializing users...");
        initUsers(dataSource);
        // printUsers(10);
        System.out.println("Initializing similarities...");
        initSimilarities();
        ArrayList<User> userGroup = new ArrayList<>();
        userGroup.add(users[100]);
        userGroup.add(users[101]);
        userGroup.add(users[102]);
        System.out.println("Calculating recommendations for a group of users...\n");
        recommendGroup(userGroup);

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
            int sumUser1 = 0;
            int sumUser2 = 0;
            int crossSum = 0;
            int squareSum1 = 0;
            int squareSum2 = 0;
            int n = common.size();
            for (int i = 0; i < n; i++) {
                // sum of user1 ratings
                sumUser1 = sumUser1 + user1.getRatingForMovie(common.get(i));

                // sum of user2 ratings
                sumUser2 = sumUser2 + user2.getRatingForMovie(common.get(i));

                // sum of both users' ratings
                crossSum = crossSum + user1.getRatingForMovie(common.get(i)) * user2.getRatingForMovie(common.get(i));

                // sum of square of array elements
                squareSum1 = squareSum1
                        + user1.getRatingForMovie(common.get(i)) * user1.getRatingForMovie(common.get(i));
                squareSum2 = squareSum2
                        + user2.getRatingForMovie(common.get(i)) * user2.getRatingForMovie(common.get(i));
            }

            // computing Pearson's correlation coefficient for user1 and user2
            // Top part of formula
            float top = (float) (n * crossSum) - (sumUser1 * sumUser2);
            // Bottom part of formula
            float bot = (float) Math.sqrt(n * squareSum1 - Math.pow(sumUser1, 2))
                    * (float) Math.sqrt(n * squareSum2 - Math.pow(sumUser2, 2));
            // Dividing top part with bottom part
            float similarity = top / bot;
            // Saving similarity data to user 1.
            user1.setSimilarity(user2.getUserId(), similarity);
            return similarity;
        } else {
            return 0;
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

    public static void recommendGroup(ArrayList<User> userGroup) {
        // Iterating through every user of group
        for (User user : userGroup) {
            for (Integer movieId : user.getUnseenMovies()) {
                predictRating(user, movieId);
            }
        }
        // Calculate group recommendations by average aggregation.
        averageAggregation(userGroup);
    }

    public static void averageAggregation(ArrayList<User> userGroup) {
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
            for (User user : userGroup) {
                Float rating = user.getPredictionForMovie(movieId);
                Integer realRating = user.getRatingForMovie(movieId);
                // Checking if user hasn't rated or doesn't have prediction for the movie
                if (rating == null && realRating == null) {
                    validMovie = false;
                } else if (rating == null) {
                    sum += realRating;
                } else {
                    sum += rating;
                }
            }
            // If ratings for the movie were found from every user of the group, continue.
            if (validMovie) {
                Float prediction = sum / userGroup.size();
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

}
