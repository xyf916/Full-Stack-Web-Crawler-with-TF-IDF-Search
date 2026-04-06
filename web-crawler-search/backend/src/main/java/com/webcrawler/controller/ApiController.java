package com.webcrawler.controller;

import com.webcrawler.model.CrawlRequest;
import com.webcrawler.model.CrawlStatus;
import com.webcrawler.model.SearchResult;
import com.webcrawler.service.CrawlerService;
import com.webcrawler.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final CrawlerService crawlerService;
    private final SearchService searchService;

    public ApiController(CrawlerService crawlerService, SearchService searchService) {
        this.crawlerService = crawlerService;
        this.searchService = searchService;
    }

    @PostMapping("/crawl")
    public Map<String, String> startCrawl(@RequestBody CrawlRequest request) {
        boolean started = crawlerService.startCrawl(request.getUrl(), request.getMaxPages());
        return Map.of("status", started ? "started" : "already_running");
    }

    @GetMapping("/crawl/status")
    public CrawlStatus getCrawlStatus() {
        return crawlerService.getStatus();
    }

    @PostMapping("/crawl/stop")
    public Map<String, String> stopCrawl() {
        crawlerService.stopCrawl();
        return Map.of("status", "stopped");
    }

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam String q,
                                     @RequestParam(defaultValue = "20") int limit) {
        return searchService.search(q, limit);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return Map.of(
            "documents", searchService.getDocumentCount(),
            "terms", searchService.getTermCount()
        );
    }
}
