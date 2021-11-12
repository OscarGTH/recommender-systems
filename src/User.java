import java.util.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedList;

public class User {
    private Integer id;
    // <movieId : rating>
    private HashMap<Integer, Integer> ratingData;
    private ArrayList<Integer> ratedMovies;
    private ArrayList<Integer> unseenMovies;
    // <userId : similarityValue>
    private HashMap<Integer, Float> similarities;
    // <movieId : predictedRating>
    private HashMap<Integer, Float> predictions;
    Helper helper = new Helper();

    public User(Integer userId) {
        this.id = userId;
        this.ratingData = new HashMap<Integer, Integer>();
        this.ratedMovies = new ArrayList<Integer>();
        this.unseenMovies = new ArrayList<Integer>();
        this.similarities = new HashMap<Integer, Float>();
        this.predictions = new HashMap<Integer, Float>();

    }

    public Integer getUserId() {
        return id;
    }

    public void setRating(Integer movieId, Integer rating) {
        ratingData.put(movieId, rating);
    }

    public HashMap<Integer, Integer> getRatingData() {
        return ratingData;
    }

    public Integer getRatingForMovie(Integer movieId) {
        Integer rating = ratingData.get(movieId);
        if (rating != null) {
            return rating;
        } else {
            return 0;
        }
    }

    public Float getPredictionForMovie(Integer movieId) {
        Float rating = predictions.get(movieId);
        if (rating != null) {
            return rating;
        } else {
            return 0.0f;
        }
    }

    public void setMovieAsRated(Integer moviedId) {
        ratedMovies.add(moviedId);
    }

    public boolean hasRatedMovie(Integer movieId) {
        return ratedMovies.contains(movieId);
    }

    public ArrayList<Integer> getRatedMovies() {
        return ratedMovies;
    }

    public ArrayList<Integer> getUnseenMovies() {
        for (int i = 0; i < 1682; i++) {
            unseenMovies.add(i);
        }
        unseenMovies.removeAll(ratedMovies);
        return unseenMovies;
    }

    public void setSimilarity(Integer userId, Float similarity) {
        if (!similarity.isNaN()) {
            similarities.put(userId, similarity);
        }
    }

    public Float getSimilarity(Integer userId) {
        return similarities.get(userId);
    }

    public Integer getSimilaritiesLength() {
        return similarities.size();
    }

    public ArrayList<Integer> getKSimilarUsers(int k) {
        return helper.getKSlice(k, similarities);
    }

    public void setPrediction(Integer movieId, Float prediction) {
        if (!prediction.isNaN()) {
            predictions.put(movieId, prediction);
        }
    }

    public ArrayList<Integer> getKRecommendedMovies(int k) {
        return helper.getKSlice(k, predictions);
    }

    // Returns list of common movies between two users.
    public ArrayList<Integer> getCommonMovies(User user2) {
        ArrayList<Integer> commonMovies = new ArrayList<>();

        // Check which movies this user and other user have in common
        for (Integer movieId : this.ratedMovies) {
            // If movie id can be found from user 2, then they have both rated the movie
            if (user2.hasRatedMovie(movieId)) {
                // Add common movie to list.
                commonMovies.add(movieId);
            }
        }
        return commonMovies;
    }

    public float getAverageRating() {
        int sum = 0;
        if (ratedMovies.size() > 0) {
            for (Integer movieId : ratedMovies) {
                sum += this.getRatingForMovie(movieId);
            }
            float mean = sum / ratedMovies.size();
            return mean;
        } else {
            return 0.0f;
        }

    }
}
