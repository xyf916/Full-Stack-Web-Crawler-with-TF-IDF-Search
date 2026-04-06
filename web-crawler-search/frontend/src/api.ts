export type CrawlStatus = {
  pagesCrawled: number;
  pagesQueued: number;
  running: boolean;
  pagesPerSecond: number;
}

export type SearchResult = {
  title: string;
  url: string;
  snippet: string;
  score: number;
}

export type IndexStats = {
  documents: number;
  terms: number;
}

export async function startCrawl(url: string, maxPages: number) {
  const res = await fetch('/api/crawl', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ url, maxPages }),
  });
  return res.json();
}

export async function getCrawlStatus(): Promise<CrawlStatus> {
  const res = await fetch('/api/crawl/status');
  return res.json();
}

export async function stopCrawl() {
  const res = await fetch('/api/crawl/stop', { method: 'POST' });
  return res.json();
}

export async function search(query: string, limit = 20): Promise<SearchResult[]> {
  const res = await fetch(`/api/search?q=${encodeURIComponent(query)}&limit=${limit}`);
  return res.json();
}

export async function getStats(): Promise<IndexStats> {
  const res = await fetch('/api/stats');
  return res.json();
}
