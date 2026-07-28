# FlashMob GRE Frontend

React + TypeScript frontend built with Vite.

## Scripts

```bash
npm install
npm run dev
```

The dev server runs at `http://localhost:3000`.

```bash
npm run build
npm run preview
npm test
```

## Backend API

By default, `.env.development` sets `VITE_API_BASE_URL=/api`. Vite proxies `/api` to `http://localhost:8080`, so the app calls `/api/getWordData` in development.

For production, set this environment variable:

```text
VITE_API_BASE_URL=https://your-backend-domain.example
```

Use `.env.production.example` as the template if you want a local production env file.

## Render Deployment

The repository-level `render.yaml` deploys this app as a Render static site with:

```text
Root directory: Frontend/flashmob-gre-frontend
Build command: npm ci && npm run build
Publish directory: dist
```

The Blueprint gets `VITE_API_BASE_URL` from the Render API service automatically and rewrites all unmatched routes to `index.html`, so direct visits to React routes work.
