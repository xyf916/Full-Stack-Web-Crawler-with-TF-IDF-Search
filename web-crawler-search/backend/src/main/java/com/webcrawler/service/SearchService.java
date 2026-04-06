package com.webcrawler.service;

import com.webcrawler.model.SearchResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SearchService {

    public record Document(int id, String title, String url, String content) {}

    record Posting(int docId, int termFrequency) {}

    private final Map<Integer, Document> documents = new ConcurrentHashMap<>();
    private final Map<String, List<Posting>> invertedIndex = new ConcurrentHashMap<>();
    private final AtomicInteger docIdCounter = new AtomicInteger(0);

    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "with", "by", "is", "it", "as", "be", "was", "are", "were",
        "been", "has", "have", "had", "do", "does", "did", "will", "would",
        "could", "should", "may", "might", "this", "that", "these", "those",
        "not", "no", "from", "into", "than", "then", "he", "she", "we", "they",
        "i", "you", "me", "my", "your", "his", "her", "its", "our", "their",
        "if", "so", "up", "out", "about", "who", "which", "when", "what", "how"
    );

    List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();

        String[] words = text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String word : words) {
            if (!word.isBlank() && word.length() > 1 && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    public void indexPage(String title, String url, String content) {
        int docId = docIdCounter.incrementAndGet();
        documents.put(docId, new Document(docId, title, url, content));

        List<String> tokens = tokenize(title + " " + content);
        Map<String, Integer> termFreqs = new HashMap<>();
        for (String token : tokens) {
            termFreqs.merge(token, 1, Integer::sum);
        }

        for (var entry : termFreqs.entrySet()) {
            invertedIndex.computeIfAbsent(entry.getKey(), k -> Collections.synchronizedList(new ArrayList<>()))
                         .add(new Posting(docId, entry.getValue()));
        }
    }

    public List<SearchResult> search(String query, int maxResults) {
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty()) return List.of();

        int totalDocs = documents.size();
        if (totalDocs == 0) return List.of();

        Map<Integer, Double> scores = new HashMap<>();

        for (String term : queryTerms) {
            List<Posting> postings = invertedIndex.get(term);
            if (postings == null) continue;

            List<Posting> snapshot;
            synchronized (postings) {
                snapshot = List.copyOf(postings);
            }

            double idf = Math.log((double) totalDocs / snapshot.size());

            for (Posting posting : snapshot) {
                double tf = 1.0 + Math.log(posting.termFrequency());
                scores.merge(posting.docId(), tf * idf, Double::sum);
            }
        }

        return scores.entrySet().stream()
            .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
            .limit(maxResults)
            .map(entry -> {
                Document doc = documents.get(entry.getKey());
                String snippet = generateSnippet(doc.content(), queryTerms);
                double roundedScore = Math.round(entry.getValue() * 100.0) / 100.0;
                return new SearchResult(doc.title(), doc.url(), snippet, roundedScore);
            })
            .toList();
    }

    private String generateSnippet(String content, List<String> queryTerms) {
        if (content == null || content.isBlank()) return "";

        String[] sentences = content.split("[.!?]+");
        String bestSentence = "";
        int bestCount = -1;

        for (String sentence : sentences) {
            String lower = sentence.toLowerCase();
            int count = 0;
            for (String term : queryTerms) {
                if (lower.contains(term)) count++;
            }
            if (count > bestCount) {
                bestCount = count;
                bestSentence = sentence.trim();
            }
        }

        if (bestSentence.length() > 200) {
            bestSentence = bestSentence.substring(0, 200) + "...";
        }

        return bestSentence;
    }

    public void clear() {
        documents.clear();
        invertedIndex.clear();
        docIdCounter.set(0);
    }

    public int getDocumentCount() {
        return documents.size();
    }

    public int getTermCount() {
        return invertedIndex.size();
    }
}
