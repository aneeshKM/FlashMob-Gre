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

For production, set this in Netlify/Vercel environment variables:

```text
VITE_API_BASE_URL=https://your-backend-domain.example
```

Use `.env.production.example` as the template if you want a local production env file.
