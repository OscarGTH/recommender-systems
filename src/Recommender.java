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
        findSimilarUsers(100, dataSource);
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

    public static void findSimilarUsers(int userId, String dataSource) {
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

            List<Integer> unprocessedUsers = new ArrayList<>(unprocessedUsersSet);
            // Iterating over unprocessed users.
            for (int i = 0; i < unprocessedUsers.size(); i++) {
                for (int x = 0; x < userRatingData.size(); x++) {
                    if (userRatingData.get(x)[0] == unprocessedUsers.get(i)) {
                        user2RatingData.put(userRatingData.get(x)[1], userRatingData.get(x)[2]);
                    }
                }
                // Calculating similarity
                double similarity = calculateSimilarity(user1RatingData, user2RatingData);
                user2RatingData.clear();
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
     * Returns NaN if either of the arrays has zero variance (i.e., if one 
     * of the arrays does not contain at least two distinct values)
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
        //common array length should be higher than two to compute correlation coefficient
        if (user1Ratings.size() > 2) {

            // Calculating mean for both users.
            //double user1Mean = calculateRatingMean(user1RatingData);
            //double user2Mean = calculateRatingMean(user2RatingData);
            
            int sumUser1 = 0;
            int sumUser2 = 0;
            int sumBoth = 0;
            int squareSum1 = 0;
            int squareSum2 = 0;
            int n = user1Ratings.size();


            for (int i= 0; i < n; i++) {
                // sum of user1 ratings
                sumUser1 = sumUser1 + user1Ratings.get(i);
       
                // sum of user2 ratings
                sumUser2 = sumUser2 + user2Ratings.get(i);
       
                // sum of both users' ratings
                sumBoth = sumBoth + user1Ratings.get(i) * user2Ratings.get(i);
       
                // sum of square of array elements
                squareSum1 = squareSum1 + user1Ratings.get(i) * user1Ratings.get(i);
                squareSum2 = squareSum2 + user2Ratings.get(i) * user2Ratings.get(i);
            }
            
            System.out.println("User1 ratings: " + user1Ratings);
            System.out.println("User2 ratings: " + user2Ratings);
            
            //computing Pearson's correlation coefficient for user1 and user2
            float similarity = (float)(n * sumBoth - sumUser1 * sumUser2) / 
                               (float)(Math.sqrt((n * squareSum1 - sumUser1 * sumUser1) * (n * squareSum2 - sumUser2 * sumUser2)));

            System.out.println("Similarity");
            System.out.println(similarity);
            System.out.println("----------");

            return similarity;

            // https://www.geeksforgeeks.org/program-find-correlation-coefficient/
            // https://www.wallstreetmojo.com/pearson-correlation-coefficient/
        } else {
            return 0;
        }

        
    }

    public static double calculateRatingMean(HashMap<Integer, Integer> ratingData) {
        double mean;
        int sum = 0;
        // Iterating over values of hashmap
        for (Integer rating : ratingData.values()) {
            // Increasing sum of rating values
            sum = sum + rating;
        }
        // Diving sum by amount of ratings.
        mean = sum / ratingData.size();
        // Returning mean.
        return mean;
    }
}
