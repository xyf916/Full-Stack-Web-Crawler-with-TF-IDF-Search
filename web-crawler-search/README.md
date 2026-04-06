# Web Crawler + Search Engine

A full-stack web application that crawls a website and lets you search through the indexed pages in real time.

## What it does

1. You enter a URL and a page limit, then hit **Crawl**
2. The backend fetches and indexes every page on that domain (up to your limit)
3. Once crawling finishes, a search box appears — type a query and get ranked results with highlighted snippets

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Maven |
| Crawling | Jsoup (HTML parser) |
| Concurrency | Java 21 Virtual Threads |
| Search | In-memory inverted index with TF-IDF ranking |
| Frontend | React 19, TypeScript, Vite |

## Prerequisites

- **Java 21+** — [Download](https://adoptium.net/)
- **Node.js 18+** — [Download](https://nodejs.org/)
- **Maven** — bundled with most IDEs, or [install separately](https://maven.apache.org/install.html)

## Running the project

### 1. Start the backend

```bash
cd backend
./mvnw spring:boot run
```

On Windows:
```bash
cd backend
mvnw.cmd spring-boot:run
```

The backend starts on `http://localhost:8080`.

### 2. Start the frontend

Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173` — open that in your browser.

## How to use it

1. Enter a website URL (e.g. `https://en.wikipedia.org/wiki/Java_(programming_language)`)
2. Set the max pages slider (default 100, max 1000)
3. Click **Crawl** and watch the progress bar
4. Once done, the search panel appears — type any query and hit **Search**
5. Results are ranked by relevance with the matching terms highlighted

## Project structure

```
web-crawler-search/
├── backend/
│   └── src/main/java/com/webcrawler/
│       ├── controller/    # REST API endpoints
│       ├── service/       # Crawler and search logic
│       ├── model/         # Data models
│       └── config/        # CORS configuration
└── frontend/
    └── src/
        ├── App.tsx        # Root component
        ├── CrawlPanel.tsx # Crawl controls and status
        ├── SearchPanel.tsx# Search input and results
        └── api.ts         # Backend API calls
```
