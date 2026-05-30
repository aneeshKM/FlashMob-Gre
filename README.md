# FlashMob GRE

FlashMob GRE is a simple GRE vocabulary flashcard site. It helps users study words from an Excel word list with a clean practice-set flow and click-to-flip flashcards.

The first screen shows practice sets:

- All Words: every word in the list.
- Set 1: words 1-50.
- Set 2: words 51-100.
- Later sets continue in 50-word batches.

Each flashcard first shows only the word. Clicking the card flips it to show the English meaning, Marathi meaning, and a sample sentence. Clicking it again flips back to the word.

## Tech Stack

- Frontend: React, TypeScript, Vite, Bootstrap, React Bootstrap
- Backend: Spring Boot, Java, Apache POI
- Data source: `Backend/flashMobGre/src/main/resources/Words.xlsx`

## Project Structure

```text
Backend/flashMobGre/              Spring Boot API
Frontend/flashmob-gre-frontend/   React frontend
```

## Run Locally

Start the backend:

```bash
cd Backend/flashMobGre
bash ./mvnw spring-boot:run
```

The backend runs on `http://localhost:8080`.

Start the frontend:

```bash
cd Frontend/flashmob-gre-frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:3000` and proxies API calls to the backend.

## API

Get all words:

```http
GET /getWordData
```

Get one 50-word set:

```http
GET /getWordData?offset=50&limit=50
```

`offset` is zero-based, so `offset=0&limit=50` returns words 1-50 and `offset=50&limit=50` returns words 51-100.

## Checks

Frontend:

```bash
cd Frontend/flashmob-gre-frontend
npm run build
npm test
```

Backend:

```bash
cd Backend/flashMobGre
bash ./mvnw test
```
