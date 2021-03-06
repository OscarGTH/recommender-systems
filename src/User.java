import java.lang.reflect.Array;
import java.util.*;

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

    public ArrayList<ArrayList<Float>> getPeerTuples(User[] users, Integer movieId, Integer slice) {
        ArrayList<ArrayList<Float>> tupleList = new ArrayList<>();
        /*
         * Each tuple describes a peer who has rated the targeted item and consists of
         * three values:
         * (i) the peer???s id,
         * (ii) the score that they have given to the item,
         * (iii) the similarity shared between the peer and the user
         */

        for (Integer userId : getKSimilarUsers(slice)) {
            // Making sure that the peer has rated the movie
            if (users[userId].hasRatedMovie(movieId)) {
                ArrayList<Float> tuple = new ArrayList<>();
                // Add user id to tuple
                tuple.add((float) userId);
                // Add given rating for movie to tuple
                tuple.add((float) users[userId].getRatingForMovie(movieId));
                // Add similarity to tuple
                tuple.add(getSimilarity(userId));
                tupleList.add(tuple);
            }
        }

        return tupleList;
    }

    public ArrayList<GroupTuple> getPeerTuplesByGenre(User[] users, Movie[] movies, String genre, Integer slice) {
        
        //List of GroupTuples: 
        //GroupTuple contains peer as User-object, similarity value
        //and list of movies this peer has rated wit this genre
        ArrayList<GroupTuple> tupleList = new ArrayList<>();
        
        for (Integer userId : getKSimilarUsers(slice)) {

            GroupTuple tuple = new GroupTuple(users[userId], getSimilarity(userId));

            //Iterates through all movies
            for (int i = 1; i < movies.length; i++) {
                //Checks that peer has rated the movie and that the movie belongs to selected genre
                if(users[userId].hasRatedMovie(i) && movies[i].getGenres().contains(genre)) {
                    tuple.setMovieAsRated(i);
                }
            }
            //add tuple to list only if peer has rated at least one movie from selected genre
            if(tuple.getRatedMoviesByGenre().size() > 0) {
                tupleList.add(tuple);
            }
            
        }
        return tupleList;
    }
}
