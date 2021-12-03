import java.util.*;

public class Group {

    private ArrayList<Float> userIterationSat;
    private ArrayList<Float> totalIterationSat;
    private ArrayList<Integer> recommendedMovies;
    private ArrayList<Integer> top40Movies;
    private ArrayList<User> userList;
    private float totalSat;
    Helper helper = new Helper();

    public Group(ArrayList<User> userList) {
        this.userList = userList;
        this.totalIterationSat = new ArrayList<>();
        this.userIterationSat = new ArrayList<>();
        this.recommendedMovies = new ArrayList<>();
        this.top40Movies = new ArrayList<>();
        this.totalSat = 0.0f;
    }

    public ArrayList<User> getUsers() {
        return userList;
    }

    // Set the iteration satisfaction score for the group
    public void setTotalIterationSat(Float sat) {
        totalIterationSat.add(sat);
    }

    // Get iteration satisfaction in given index
    public Float getTotalIterationSat(Integer index) {
        return totalIterationSat.get(index);
    }

    // Set total satisfaction
    public void setTotalSat(Float sat) {
        this.totalSat = sat;
    }

    // Get total satisfaction
    public Float getTotalSat() {
        return this.totalSat;
    }

    public void setUserIterationSat(Float sat) {
        userIterationSat.add(sat);
    }

    public ArrayList<Float> getUserIterationSat() {
        return userIterationSat;
    }

    public void clearUserIterationSat() {
        userIterationSat.clear();
    }

    public ArrayList<Integer> getRecommendedMovies() {
        return recommendedMovies;
    }

    public void setRecommendedMovie(Integer movieId) {
        recommendedMovies.add(movieId);
    }

    public void setTop20Movies(ArrayList<Integer> movies) {
        this.recommendedMovies.addAll(movies);
    }

    public void setTop40Movies(ArrayList<Integer> movies) {
        top40Movies.addAll(movies);
    }

    public ArrayList<Integer> getTop40Movies() {
        return top40Movies;
    }
}
