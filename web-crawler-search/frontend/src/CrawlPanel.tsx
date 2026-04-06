import { useState, useEffect, useRef } from 'react';
import { startCrawl, stopCrawl, getCrawlStatus } from './api';
import type { CrawlStatus } from './api';

interface Props {
  onCrawlComplete: () => void;
}

export default function CrawlPanel({ onCrawlComplete }: Props) {
  const [url, setUrl] = useState('');
  const [maxPages, setMaxPages] = useState(100);
  const [status, setStatus] = useState<CrawlStatus | null>(null);
  const [error, setError] = useState('');
  const intervalRef = useRef<number | null>(null);

  const isRunning = status?.running ?? false;

  useEffect(() => {
    if (isRunning) {
      intervalRef.current = window.setInterval(async () => {
        const s = await getCrawlStatus();
        setStatus(s);
        if (!s.running) {
          clearInterval(intervalRef.current!);
          onCrawlComplete();
        }
      }, 500);
    }
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isRunning, onCrawlComplete]);

  const handleStart = async () => {
    setError('');
    if (!url.trim()) {
      setError('Enter a URL');
      return;
    }
    try {
      await startCrawl(url.trim(), maxPages);
      const s = await getCrawlStatus();
      setStatus(s);
    } catch {
      setError('Failed to start crawl');
    }
  };

  const handleStop = async () => {
    await stopCrawl();
    const s = await getCrawlStatus();
    setStatus(s);
  };

  const progress = status
    ? Math.min(100, Math.round((status.pagesCrawled / maxPages) * 100))
    : 0;

  return (
    <div className="panel">
      <h2>Crawl a Website</h2>

      <div className="input-row">
        <input
          type="text"
          placeholder="https://example.com"
          value={url}
          onChange={(e) => setUrl(e.target.value)}
          disabled={isRunning}
        />
        {!isRunning ? (
          <button onClick={handleStart}>Crawl</button>
        ) : (
          <button onClick={handleStop} className="stop-btn">Stop</button>
        )}
      </div>

      <div className="slider-row">
        <label>Max pages: {maxPages}</label>
        <input
          type="range"
          min={10}
          max={1000}
          step={10}
          value={maxPages}
          onChange={(e) => setMaxPages(Number(e.target.value))}
          disabled={isRunning}
        />
      </div>

      {error && <p className="error">{error}</p>}

      {status && (
        <div className="status-box">
          <div className="progress-bar-track">
            <div className="progress-bar-fill" style={{ width: `${progress}%` }} />
          </div>
          <div className="stats-row">
            <span>Pages: {status.pagesCrawled} / {maxPages}</span>
            <span>Queued: {status.pagesQueued}</span>
            <span>{status.pagesPerSecond} pages/sec</span>
            <span className={`status-dot ${isRunning ? 'running' : 'done'}`}>
              {isRunning ? 'Crawling...' : 'Done'}
            </span>
          </div>
        </div>
      )}
    </div>
  );
}
