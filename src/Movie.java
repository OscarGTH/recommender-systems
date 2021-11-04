import java.util.ArrayList;

public class Movie {
    private Integer id;
    private String name;
    private ArrayList<Integer> allRatings;

    public Movie(Integer movieId) {
        this.id = movieId;
        this.allRatings = new ArrayList<Integer>();
    }

    public Integer getMovieId() {
        return id;
    }

    public void setRating(Integer rating) {
        allRatings.add(rating);
    }

    public ArrayList<Integer> getAllRatings() {
        return allRatings;
    }

    public void setName(String movieName) {
        this.name = movieName;
    }

    public String getName() {
        return this.name;
    }

    public Integer getRatingCount() {
        return allRatings.size();
    }

    public Integer getRatingSum() {
        Integer sum = 0;
        for (Integer rating : allRatings) {
            sum += rating;
        }
        return sum;
    }

    public Float getRatingSquareSum() {
        Float sum = 0.0f;
        for (Integer rating : allRatings) {
            sum += rating * rating;
        }
        return sum;
    }

    public Float getRatingMean() {
        return (float) getRatingSum() / allRatings.size();
    }

}
