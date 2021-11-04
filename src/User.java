import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private Integer id;
    // <movieId : rating>
    private HashMap<Integer, Integer> ratingData;
    private ArrayList<Integer> ratedMovies;
    private ArrayList<Integer> unseenMovies;
    // <userId : similarityValue>
    private HashMap<Integer, Float> similarities;

    public User(Integer userId) {
        this.id = userId;
        this.ratingData = new HashMap<Integer, Integer>();
        this.ratedMovies = new ArrayList<Integer>();
        this.unseenMovies = new ArrayList<Integer>();
        this.similarities = new HashMap<Integer, Float>();
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
        return ratingData.get(movieId);
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

    // empty list atm
    public ArrayList<Integer> getUnseenMovies() {
        return unseenMovies;
    }

    // empty list atm
    public Float getSimilarity(Integer userId) {
        return similarities.get(userId);
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

}
