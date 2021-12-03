public class Genre {
    private Integer genreId;
    private String genreName;

    public Genre(Integer genreId, String genreName) {
        this.genreId = genreId;
        this.genreName = genreName;
    }

    public String getGenreName() {
        return genreName;
    }

    public Integer getGenreId() {
        return genreId;
    }
}
