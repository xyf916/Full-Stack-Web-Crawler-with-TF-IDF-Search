package com.webcrawler.service;

import com.webcrawler.model.CrawlStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CrawlerService {

    private final SearchService searchService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger pagesCrawled = new AtomicInteger(0);
    private final ConcurrentHashMap<String, Boolean> visited = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
    private ExecutorService executor;
    private volatile int maxPages = 500;
    private volatile long startTime;
    private volatile String baseDomain;

    public CrawlerService(SearchService searchService) {
        this.searchService = searchService;
    }

    public boolean startCrawl(String seedUrl, int maxPages) {
        if (running.get()) return false;

        this.maxPages = maxPages;
        pagesCrawled.set(0);
        visited.clear();
        queue.clear();
        searchService.clear();

        try {
            URI uri = URI.create(seedUrl);
            baseDomain = uri.getHost();
        } catch (Exception e) {
            return false;
        }

        queue.add(seedUrl);
        visited.put(seedUrl, true);
        running.set(true);
        startTime = System.currentTimeMillis();

        executor = Executors.newVirtualThreadPerTaskExecutor();

        Thread.startVirtualThread(this::coordinateCrawl);
        return true;
    }

    private void coordinateCrawl() {
        Semaphore concurrencyLimit = new Semaphore(50);

        try {
            while (running.get() && pagesCrawled.get() < maxPages) {
                String url = queue.poll();

                if (url == null) {
                    try { Thread.sleep(200); } catch (InterruptedException e) { break; }
                    if (queue.isEmpty()) {
                        try { Thread.sleep(500); } catch (InterruptedException e) { break; }
                        if (queue.isEmpty()) break;
                    }
                    continue;
                }

                try {
                    concurrencyLimit.acquire();
                } catch (InterruptedException e) {
                    break;
                }

                executor.submit(() -> {
                    try {
                        crawlPage(url);
                    } finally {
                        concurrencyLimit.release();
                    }
                });
            }
        } finally {
            running.set(false);
            executor.shutdown();
        }
    }

    private void crawlPage(String url) {
        if (pagesCrawled.get() >= maxPages || !running.get()) return;

        try {
            Document doc = Jsoup.connect(url)
                .userAgent("WebCrawlerSearch/1.0")
                .timeout(5000)
                .get();

            String title = doc.title();
            String text = doc.body() != null ? doc.body().text() : "";

            if (!text.isBlank()) {
                searchService.indexPage(title, url, text);
                pagesCrawled.incrementAndGet();
            }

            for (Element link : doc.select("a[href]")) {
                String absUrl = link.absUrl("href");
                if (absUrl.isBlank()) continue;

                int hashIndex = absUrl.indexOf('#');
                if (hashIndex > 0) absUrl = absUrl.substring(0, hashIndex);

                try {
                    URI uri = URI.create(absUrl);
                    String host = uri.getHost();
                    if (host == null || !(host.equals(baseDomain) || host.endsWith("." + baseDomain))) continue;
                    if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) continue;
                } catch (Exception e) {
                    continue;
                }

                String lower = absUrl.toLowerCase();
                if (lower.endsWith(".pdf") || lower.endsWith(".png") || lower.endsWith(".jpg") ||
                    lower.endsWith(".gif") || lower.endsWith(".zip") || lower.endsWith(".css") ||
                    lower.endsWith(".js") || lower.endsWith(".xml")) {
                    continue;
                }

                if (visited.putIfAbsent(absUrl, true) == null) {
                    queue.add(absUrl);
                }
            }
        } catch (Exception e) {
        }
    }

    public void stopCrawl() {
        running.set(false);
        if (executor != null) executor.shutdown();
    }

    public CrawlStatus getStatus() {
        int crawled = pagesCrawled.get();
        int queued = queue.size();
        boolean isRunning = running.get();
        double pagesPerSec = 0;

        if (crawled > 0 && startTime > 0) {
            double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
            if (elapsed > 0) pagesPerSec = Math.round((crawled / elapsed) * 10.0) / 10.0;
        }

        return new CrawlStatus(crawled, queued, isRunning, pagesPerSec);
    }
}
