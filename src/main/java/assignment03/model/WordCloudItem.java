package assignment03.model;

import java.util.*;

/**
 * Represents a single item in a word cloud, consisting of:
 * - the word itself
 * - a relative height (used for visual scaling, e.g., font size)
 */
public record WordCloudItem(String word, double relativeHeight) {

    /**
     * Static method to compute a list of WordCloudItems from a list of words.
     * Each item's height is relative to the most frequent word,
     * and the list is sorted in descending order of frequency.
     *
     * @param words List of words to include in the word cloud.
     * @return A sorted list of WordCloudItem objects with scaled heights.
     */
    public static ArrayList<WordCloudItem> computeItems(ArrayList<String> words) {
        Map<String, Integer> freqMap = new HashMap<>();

        // Step 1: Count frequency of each word
        for (String word : words) {
            freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
        }

        // Step 2: Determine maximum frequency to use as a scaling base
        int maxFreq = freqMap.values().stream().max(Integer::compareTo).orElse(1);

        ArrayList<WordCloudItem> items = new ArrayList<>();

        // Step 3: Create WordCloudItem for each word with a scaled relative height
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            double relHeight = Math.sqrt(entry.getValue()) / Math.sqrt(maxFreq);
            items.add(new WordCloudItem(entry.getKey(), relHeight));
        }

        // Step 4: Sort the list in descending order by frequency
        items.sort((a, b) -> Double.compare(
                freqMap.get(b.word()), freqMap.get(a.word()))
        );

        return items;
    }
}
