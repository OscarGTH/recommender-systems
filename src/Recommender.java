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
                    if (userRatingData.get(x)[0] == i) {
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
     * @param user1RatingData The rating data of the user 1.
     * @param user2RatingData The rating data of the user 2.
     * @return similarity value between the users.
     */
    public static double calculateSimilarity(HashMap<Integer, Integer> user1RatingData,
            HashMap<Integer, Integer> user2RatingData) {
        double similarity = 0;
        List<Integer> commonItems = new ArrayList<Integer>();

        // Check which movies user 1 and user 2 have in common
        for (Integer item : user1RatingData.keySet()) {
            // If movie id can be found from user 2, then they have both rated the movie
            if (user2RatingData.containsKey(item)) {
                // Adding movie id to common items list
                commonItems.add(item);
            }
        }
        if (commonItems.size() > 0) {
            // Calculating mean for both users.
            double user1Mean = calculateRatingMean(user1RatingData);
            double user2Mean = calculateRatingMean(user2RatingData);
            // https://www.geeksforgeeks.org/program-find-correlation-coefficient/
        } else {
            return 0;
        }

        return similarity;
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
