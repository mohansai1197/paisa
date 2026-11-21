# Paisa frontend

React dashboard for the Paisa hackathon project. Run `npm ci` and `npm run dev`.
The development server proxies `/api` to Spring Boot on port 8080.

`npm run build` creates `dist/`. The Sites publication is a private static preview:
its sample workspace saves fictional records in browser local storage. Real login,
database persistence, and Gemini features require the Spring Boot backend. The full
application Dockerfile in the parent project serves React and Java together.

Source map: `src/App.jsx` coordinates state and navigation; `src/pages.jsx` contains
the six screens; `src/components.jsx` contains forms and shared controls;
`src/api.js` handles authenticated requests; `src/finance.js` calculates totals and
reports; `src/data.js` creates labeled sample data; `src/styles.css` defines the UI.

No API keys or database credentials belong in this frontend.
