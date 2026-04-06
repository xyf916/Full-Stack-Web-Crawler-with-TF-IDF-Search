import { useCallback, useState } from 'react';
import CrawlPanel from './CrawlPanel';
import SearchPanel from './SearchPanel';
import './App.css';

function App() {
  const [crawlDone, setCrawlDone] = useState(false);

  const handleCrawlComplete = useCallback(() => {
    setCrawlDone(true);
  }, []);

  return (
    <div className="app">
      <header>
        <h1>Web Crawler + Search Engine</h1>
        <p className="subtitle">Java 21 Virtual Threads &middot; Spring Boot &middot; React</p>
      </header>
      <main>
        <CrawlPanel onCrawlComplete={handleCrawlComplete} />
        {crawlDone && <SearchPanel />}
      </main>
    </div>
  );
}

export default App;
