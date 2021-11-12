import java.util.*;
import java.util.Map;
import java.util.Map.Entry;

public class Helper {

    public Helper() {

    }

    public ArrayList<Integer> getKSlice(int k, Map<Integer, Float> objectMap) {
        objectMap = sortByComparator(objectMap, false);
        ArrayList<Integer> kSlice = new ArrayList<>();

        int i;
        for (i = 0; i < k; i++) {
            kSlice.add((Integer) objectMap.keySet().toArray()[i]);
        }
        return kSlice;
    }

    private static HashMap<Integer, Float> sortByComparator(Map<Integer, Float> unsortedMap, final boolean order) {
        List<Entry<Integer, Float>> list = new LinkedList<Entry<Integer, Float>>(unsortedMap.entrySet());
        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Integer, Float>>() {
            public int compare(Entry<Integer, Float> o1, Entry<Integer, Float> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Integer, Float> sortedMap = new LinkedHashMap<Integer, Float>();
        for (Entry<Integer, Float> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public boolean checkMovieValidity(User user, Integer movieId) {
        Float predictedRating = user.getPredictionForMovie(movieId);
        boolean validMovie = true;
        Integer realRating = user.getRatingForMovie(movieId);

        // Checking if user hasn't rated or doesn't have prediction for the movie
        if (predictedRating == 0.0f && realRating == 0) {
            validMovie = false;
        }
        return validMovie;
    }
}
