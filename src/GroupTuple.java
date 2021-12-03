import java.util.ArrayList;

public class GroupTuple {
    private User user;
    private ArrayList<Integer> ratedMoviesByGenre;
    private Float similarity;

    public GroupTuple(User user, Float similarity) {
        this.user = user;
        this.similarity = similarity;
        this.ratedMoviesByGenre = new ArrayList<Integer>();
    }

    public ArrayList<Integer> getRatedMoviesByGenre() {
        return ratedMoviesByGenre;
    }

    public Float getSimilarity() {
        return similarity;
    }

    public void setMovieAsRated(Integer movieId) {
        ratedMoviesByGenre.add(movieId);
    }

    public boolean likesThisGenre() {
        float average = getAverage();

        if (average > 3.5) {
            return true;
        } else {
            return false;
        }
    }

    public float getAverage() {
        int sum = 0;
        float average = 0.0f;
        for(Integer movieId : ratedMoviesByGenre) {
            sum += this.user.getRatingForMovie(movieId);
        }
        if(ratedMoviesByGenre.size() > 0) {
            average = sum / ratedMoviesByGenre.size();
        }
        
        return average;
    }
}
