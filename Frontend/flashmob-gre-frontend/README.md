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

By default, Vite proxies `/api` to `http://localhost:8080`, so the app calls `/api/getWordData` in development.

To point at another backend URL, set:

```bash
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```
