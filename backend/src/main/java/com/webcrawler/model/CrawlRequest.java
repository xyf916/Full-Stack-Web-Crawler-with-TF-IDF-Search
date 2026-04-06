package com.webcrawler.model;

public class CrawlRequest {
    private String url;
    private int maxPages = 500;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getMaxPages() { return maxPages; }
    public void setMaxPages(int maxPages) { this.maxPages = maxPages; }
}
