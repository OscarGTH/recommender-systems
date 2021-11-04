import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
// Importing utility classes
import java.util.*;

public class Recommender {
    final static String genreSource = "ml-100k/u.genre";
    final static String dataSource = "ml-100k/u.data";
    static User[] users;

    public static void main(String[] args) throws Exception {
        //getLineCount(dataSource);
        //readMovieData(dataSource);
        //findSimilarAndPredict(156, dataSource);


        initUsers(dataSource);
        printUsers(10);
        
    }

    public static void initUsers(String dataSource) {
        //User data is saved to index which matches to userId
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
    //prints data of n first users 
    public static void printUsers(int n) {
        
        for (int i = 1; i < n; i++) {
            System.out.print("User id: ");
            System.out.println(users[i].getUserId());
            System.out.print("Ratings : ");
            System.out.println(users[i].getRatingData());
            System.out.println("Total ratings: " + users[i].getRatedMovies().size());
        }
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
            // Contains neighbor user id and it's rating data in hash map
            HashMap<Integer, HashMap<Integer, Integer>> neighborData = new HashMap<>();

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
                // Copying user 2 rating data into temp hash map
                HashMap<Integer, Integer> tempNeighborData = new HashMap<>(user2RatingData);
                if (!Float.isNaN(similarity)) {
                    sims.put(similarity, unprocessedUsers.get(i));
                    // Putting user id and the users data hash map into a separate hash map.
                    neighborData.put(unprocessedUsers.get(i), tempNeighborData);
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
                if ((Float) sorted.keySet().toArray()[i] > 0.70) {
                    // Getting value by index.
                    Float myKey = (Float) sorted.keySet().toArray()[i];
                    // Putting neighbor's values into hash map (User id : similarity)
                    neighbors.put(sorted.get(myKey), myKey);
                }
            }

            // Filtering neighboring data based on keys from neighbors.
            // This reduces the size of our needed data.
            Set<Integer> set = new HashSet<Integer>(neighbors.keySet());
            neighborData.keySet().retainAll(set);

            HashMap<Float, Integer> predictions = new HashMap<Float, Integer>();
            // Iterating through every movie
            for (int i = 0; i < 1682; i++) {
                // Checking that user hasn't rated the movie already
                if (!user1RatingData.containsKey(i)) {
                    float prediction = 0.0f;
                    // Predicting rating for the movie
                    prediction = predictRating(user1RatingData, neighbors, neighborData, i);
                    predictions.put(prediction, i);
                }
            }

            // Sorting prediction values from low to high.
            // (Similarity : User Id)
            TreeMap<Float, Integer> sorted_predictions = new TreeMap<>(predictions);

            // Getting 20 movie recommendations.
            // (Movie Id : predicted rating )
            HashMap<Integer, Float> recommendations = getClosestNeighbors(20, sorted_predictions);
            // Getting 10 most similar users.
            // (User Id : similarity)
            HashMap<Integer, Float> k_closest_neighbors = getClosestNeighbors(10, sorted);

            System.out.println("\n***************");
            System.out.println("Top 10 similar users:\n");
            for (Integer neighbor : k_closest_neighbors.keySet()) {
                System.out.println("--------------");
                System.out.println("User ID " + Integer.toString(neighbor) + " Similarity: "
                        + Float.toString(k_closest_neighbors.get(neighbor)));
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
            HashMap<Integer, HashMap<Integer, Integer>> neighborData, Integer movieId) {
        // Used to contain the rating data of neighbor.
        HashMap<Integer, Integer> neighborRatingData = new HashMap<Integer, Integer>();
        // Top part of prediction formula
        float top = 0.0f;
        // Bottom part of prediction formula
        float bot = 0.0f;
        // Prediction value
        float prediction = 0.0f;

        // Iterating through neighbors
        for (Integer neighbor : neighbors.keySet()) {
            // Fetching rating data for the specific neighbor
            neighborRatingData = neighborData.get(neighbor);
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
}
