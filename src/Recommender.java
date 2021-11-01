import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
// Importing utility classes
import java.util.*;

public class Recommender {
    final static String genreSource = "ml-100k/u.genre";
    final static String dataSource = "ml-100k/u.data";

    public static void main(String[] args) throws Exception {
        getLineCount(dataSource);
        readMovieData(dataSource);
        findSimilarAndPredict(150, dataSource);
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

    public static void findSimilarAndPredict(int userId, String dataSource) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataSource));
            // Key is item id, value is rating for the item.
            HashMap<Integer, Integer> user1RatingData = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> user2RatingData = new HashMap<Integer, Integer>();

            List<int[]> userRatingData = new ArrayList<int[]>();

            // List of user ids whose similarities have not been computed.
            Set<Integer> unprocessedUsersSet = new HashSet<Integer>();

            // Loop through whole dataset and gather item ids and ratings for user 1
            while (reader.ready()) {
                String line = reader.readLine();
                String[] ratingData = line.split("\\s+");
                if (line.startsWith(Integer.toString(userId))) {
                    user1RatingData.put(Integer.parseInt(ratingData[1]), Integer.parseInt(ratingData[2]));
                } else {
                    int userData[] = new int[3];
                    // User id
                    userData[0] = Integer.parseInt(ratingData[0]);
                    // Item id
                    userData[1] = Integer.parseInt(ratingData[1]);
                    // Rating
                    userData[2] = Integer.parseInt(ratingData[2]);
                    // Adding user data to userRatingData ArrayList.
                    userRatingData.add(userData);

                    // Adding unique user ids to set.
                    unprocessedUsersSet.add(Integer.parseInt(ratingData[0]));
                }
            }
            // Close reader if it is not null
            if (reader != null) {
                reader.close();
            }
            HashMap<Float, Integer> sims = new HashMap<Float, Integer>();
            List<Integer> unprocessedUsers = new ArrayList<>(unprocessedUsersSet);
            // Iterating over unprocessed users.
            for (int i = 0; i < unprocessedUsers.size(); i++) {
                for (int x = 0; x < userRatingData.size(); x++) {
                    if (userRatingData.get(x)[0] == unprocessedUsers.get(i)) {
                        user2RatingData.put(userRatingData.get(x)[1], userRatingData.get(x)[2]);
                    }
                }
                // Calculating similarity
                float similarity = calculateSimilarity(user1RatingData, user2RatingData);

                if (!Float.isNaN(similarity)) {
                    sims.put(similarity, unprocessedUsers.get(i));
                }

                user2RatingData.clear();
            }
            // Sorting similarity values from low to high.
            // (Similarity : User Id)
            TreeMap<Float, Integer> sorted = new TreeMap<>(sims);
            // Most similar neighbors
            HashMap<Integer, Float> neighbors = new HashMap<Integer, Float>();
            // Iterating through sorted tree map
            for (int i = 0; i < sorted.size(); i++) {
                // Get user if their similarity is more than 0.90.
                if ((Float) sorted.keySet().toArray()[i] > 0.90) {
                    // Getting value by index.
                    Float myKey = (Float) sorted.keySet().toArray()[i];
                    // Putting neighbor's values into hash map (User id : similarity)
                    neighbors.put(sorted.get(myKey), myKey);
                }
            }

            HashMap<Float, Integer> predictions = new HashMap<Float, Integer>();
            // Iterating through every movie
            for (int i = 0; i < 1682; i++) {
                // Checking that user hasn't rated the movie already
                if (!user1RatingData.containsKey(i)) {
                    float prediction = 0.0f;
                    // Predicting rating for the movie
                    prediction = predictRating(user1RatingData, neighbors, userRatingData, i);
                    predictions.put(prediction, i);
                }
            }
            // Sorting predictio values from low to high.
            // (Similarity : User Id)
            TreeMap<Float, Integer> sorted_predictions = new TreeMap<>(predictions);
            HashMap<Integer, Float> recommendations = getClosestNeighbors(20, sorted_predictions);

            System.out.println("\n***************");
            System.out.println("Top 10 similar users:\n");
            for (Integer neighbor : neighbors.keySet()) {
                System.out.println("--------------");
                System.out.println("User ID " + Integer.toString(neighbor) + " Similarity: "
                        + Float.toString(neighbors.get(neighbor)));
            }
            System.out.println("\n***************");
            System.out.println("Top 20 recommended movies:\n");
            for (Integer recommendation : recommendations.keySet()) {
                System.out.println("--------------");
                System.out.println("Movie ID " + Integer.toString(recommendation) + " Prediction of rating: "
                        + Float.toString(recommendations.get(recommendation)));
            }

        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        } catch (IOException ioExc) {
            System.out.println(ioExc);
        }
    }

    public static HashMap<Integer, Float> getClosestNeighbors(Integer k, TreeMap<Float, Integer> sorted) {
        HashMap<Integer, Float> neighbors = new HashMap<Integer, Float>();
        for (int i = 0; i < sorted.size(); i++) {
            // Go through, if index is the less or equal than the last n elements in tree
            // map.
            if (i >= (sorted.size() - k)) {
                // Getting value by index.
                Float myKey = (Float) sorted.keySet().toArray()[i];
                // Putting neighbor's values into hash map (User id : similarity)
                neighbors.put(sorted.get(myKey), myKey);
            }
        }
        return neighbors;
    }

    /**
     * Calculate similarity between two users.
     * 
     * Returns NaN if either of the arrays has zero variance (i.e., if one of the
     * arrays does not contain at least two distinct values)
     * 
     * @param user1RatingData The rating data of the user 1.
     * @param user2RatingData The rating data of the user 2.
     * @return Pearson's correlation coefficient for two arrays (users).
     */
    public static float calculateSimilarity(HashMap<Integer, Integer> user1RatingData,
            HashMap<Integer, Integer> user2RatingData) {

        ArrayList<Integer> user1Ratings = new ArrayList<>();
        ArrayList<Integer> user2Ratings = new ArrayList<>();

        // Check which movies user 1 and user 2 have in common
        for (Integer item : user1RatingData.keySet()) {
            // If movie id can be found from user 2, then they have both rated the movie
            if (user2RatingData.containsKey(item)) {
                // Adding rating to users rating list
                user1Ratings.add(user1RatingData.get(item));
                user2Ratings.add(user2RatingData.get(item));
            }
        }
        // common array length should be higher than two to compute correlation
        // coefficient
        if (user1Ratings.size() > 2) {
            int sumUser1 = 0;
            int sumUser2 = 0;
            int crossSum = 0;
            int squareSum1 = 0;
            int squareSum2 = 0;
            int n = user1Ratings.size();

            for (int i = 0; i < n; i++) {
                // sum of user1 ratings
                sumUser1 = sumUser1 + user1Ratings.get(i);

                // sum of user2 ratings
                sumUser2 = sumUser2 + user2Ratings.get(i);

                // sum of both users' ratings
                crossSum = crossSum + user1Ratings.get(i) * user2Ratings.get(i);

                // sum of square of array elements
                squareSum1 = squareSum1 + user1Ratings.get(i) * user1Ratings.get(i);
                squareSum2 = squareSum2 + user2Ratings.get(i) * user2Ratings.get(i);
            }

            // computing Pearson's correlation coefficient for user1 and user2
            // Top part of formula
            float top = (float) (n * crossSum) - (sumUser1 * sumUser2);
            // Bottom part of formula
            float bot = (float) Math.sqrt(n * squareSum1 - Math.pow(sumUser1, 2))
                    * (float) Math.sqrt(n * squareSum2 - Math.pow(sumUser2, 2));
            // Dividing top part with bottom part
            float similarity = top / bot;

            return similarity;
        } else {
            return 0;
        }
    }

    // Predicts user rating for a movie.
    public static float predictRating(HashMap<Integer, Integer> user1RatingData, HashMap<Integer, Float> neighbors,
            List<int[]> userRatingData, Integer movieId) {

        HashMap<Integer, Integer> neighborRatingData = new HashMap<Integer, Integer>();
        // Top part of prediction formula
        float top = 0.0f;
        // Bottom part of prediction formula
        float bot = 0.0f;
        // Prediction value
        float prediction = 0.0f;

        // Iterating through neighbors
        for (Integer neighbor : neighbors.keySet()) {
            // Fetching neighbors data from data set.
            for (int i = 0; i < userRatingData.size(); i++) {
                // If user id matches neighbor, put data into hash map
                if (userRatingData.get(i)[0] == neighbor) {
                    neighborRatingData.put(userRatingData.get(i)[1], userRatingData.get(i)[2]);
                }
            }
            // Check if neighbor has rated the movie
            if (neighborRatingData.containsKey(movieId)) {
                // Get similarity of neighbor
                float similarity = neighbors.get(neighbor);
                // Neighbor's rating of the movie
                Integer rating = neighborRatingData.get(movieId);
                top += similarity * rating;
                bot += similarity;
            }
        }
        // Making sure top and bottom part of formula are neither zero.
        if (top != 0 && bot != 0) {
            // Dividing top and bottom parts of formula to get the final prediction for the
            // given movie.
            prediction = top / bot;
        }
        return prediction;
    }

    public static HashMap<Integer, Float> checkUser(HashMap<Integer, Float> neighbors) {
        return neighbors;
    }
}
