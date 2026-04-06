import { useState } from 'react';
import { search, getStats } from './api';
import type { SearchResult, IndexStats } from './api';

export default function SearchPanel() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<SearchResult[]>([]);
  const [stats, setStats] = useState<IndexStats | null>(null);
  const [searched, setSearched] = useState(false);
  const [latency, setLatency] = useState(0);

  const handleSearch = async () => {
    if (!query.trim()) return;

    const start = performance.now();
    const [res, st] = await Promise.all([search(query.trim()), getStats()]);
    const elapsed = Math.round(performance.now() - start);

    setResults(res);
    setStats(st);
    setLatency(elapsed);
    setSearched(true);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const highlightTerms = (text: string, q: string) => {
    const terms = q.toLowerCase().split(/\s+/);
    const regex = new RegExp(`(${terms.map(t => t.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')).join('|')})`, 'gi');
    const parts = text.split(regex);

    return parts.map((part, i) =>
      terms.includes(part.toLowerCase())
        ? <mark key={i}>{part}</mark>
        : <span key={i}>{part}</span>
    );
  };

  return (
    <div className="panel">
      <h2>Search Indexed Pages</h2>

      <div className="input-row">
        <input
          type="text"
          placeholder="Search..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <button onClick={handleSearch}>Search</button>
      </div>

      {searched && (
        <p className="result-meta">
          {results.length} result{results.length !== 1 ? 's' : ''} found in {latency}ms
          {stats && <> &middot; {stats.documents} docs indexed &middot; {stats.terms} unique terms</>}
        </p>
      )}

      <div className="results">
        {results.map((r, i) => (
          <div key={i} className="result-card">
            <div className="result-title">
              <a href={r.url} target="_blank" rel="noopener noreferrer">
                {r.title || r.url}
              </a>
              <span className="score">{r.score}</span>
            </div>
            <div className="result-url">{r.url}</div>
            <div className="result-snippet">
              {highlightTerms(r.snippet, query)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
