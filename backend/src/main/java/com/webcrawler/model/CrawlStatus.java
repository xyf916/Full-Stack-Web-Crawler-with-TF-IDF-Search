package com.webcrawler.model;

public class CrawlStatus {
    private int pagesCrawled;
    private int pagesQueued;
    private boolean running;
    private double pagesPerSecond;

    public CrawlStatus(int pagesCrawled, int pagesQueued, boolean running, double pagesPerSecond) {
        this.pagesCrawled = pagesCrawled;
        this.pagesQueued = pagesQueued;
        this.running = running;
        this.pagesPerSecond = pagesPerSecond;
    }

    public int getPagesCrawled() { return pagesCrawled; }
    public int getPagesQueued() { return pagesQueued; }
    public boolean isRunning() { return running; }
    public double getPagesPerSecond() { return pagesPerSecond; }
}
