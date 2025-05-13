package assignment03.model;

import java.util.*;

public record WordCloudItem(String word, double relativeHeight) {

    public static ArrayList<WordCloudItem> computeItems(ArrayList<String> words) {
        Map<String, Integer> freqMap = new HashMap<>();

        for (String word : words) {
            freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
        }

        int maxFreq = freqMap.values().stream().max(Integer::compareTo).orElse(1);

        ArrayList<WordCloudItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            double relHeight = Math.sqrt(entry.getValue()) / Math.sqrt(maxFreq);
            items.add(new WordCloudItem(entry.getKey(), relHeight));
        }

        // Sort descending by frequency
        items.sort((a, b) -> Double.compare(
                freqMap.get(b.word()), freqMap.get(a.word()))
        );

        return items;
    }
}
