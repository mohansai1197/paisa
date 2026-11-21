# Demo data

On the connected workspace's Overview page, **Load sample records** appears beside **Add transaction**, including when accounts already exist. Confirm the fictional-data notice to load it.

The backend adds **Demo · Everyday account** and **Demo · Cash wallet**, 28 clearly marked transactions across the current and previous month, and up to five current-month budgets. Existing accounts, transactions, and budget limits stay intact. Default categories are matched by name and type, with missing ones created. Repeated loading is rejected while either Demo account remains. The existing user balance lock serializes demo loads.

Demo records contribute to charts, reports, and AI insights. Open **AI insights → Generate monthly insights** to analyze them with the configured backend AI provider.

Files: `frontend/src/pages.jsx` (button), `frontend/src/App.jsx` (confirmation and request), `backend/src/main/java/com/paisa/service/FinanceService.java` (data generation), `backend/src/test/java/com/paisa/FinanceIntegrationTest.java` (preservation and duplicate-load checks).
