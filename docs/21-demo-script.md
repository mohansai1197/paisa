# Paisa demo and explanation

## Three-minute demo

1. **Problem, 20 seconds:** “Recording expenses is easy; understanding what to do
   next is harder. Paisa puts transactions, accounts, monthly budgets, and clear
   explanations in one workspace.”
2. **One entry, 40 seconds:** Open Overview, add a fictional coffee expense, choose
   an account and category, and save. Show that the transaction history, account
   balance, expense chart, net cash flow, and budget progress all update together.
3. **AI, 60 seconds:** In an expense form, describe “Coffee with friends at a cafe”
   and request a category suggestion. Explain that the user can change it. Then
   open AI insights and generate the month’s patterns, recommendations, and summary.
   Point to the measured income/expense/net figures underneath.
4. **User control, 30 seconds:** Open Review budget on a recommendation. The model
   does not silently edit a budget; the user chooses the category, month, and limit
   and explicitly saves. Explain that targets are illustrative, not proven affordable.
5. **Takeaway, 30 seconds:** Download the monthly CSV. “Enter once, understand the
   month, review the suggestion, and keep an export you can inspect.”

Use only fictional records. Remove your own demonstration entry afterward if desired.
If the provider is unavailable, show the actual error and the working manual features.
Do not present saved test output as a fresh generated result.

## Explain the pipeline simply

**React form → Spring Security session + CSRF → validated controller request →
user-scoped service → JPA → configured database → refreshed dashboard.**

For AI: **the service calculates facts → Gemini receives a bounded context and
JSON response schema → the backend checks category IDs and proposed amounts →
React shows a suggestion → the user reviews before saving.**

## Likely judge questions

| Question | Answer |
| --- | --- |
| Where are the four AI features? | Category suggestion in transaction entry; patterns, budget explanations, and monthly summary on AI insights. |
| Why use AI for arithmetic? | Arithmetic uses Java BigDecimal and client integer-cent aggregation. AI explains the calculated data and classifies descriptions. |
| Is the suggested budget personalized financial advice? | It is an illustrative 10% reduction starting point from recorded category spending. It does not establish affordability or guarantee savings. |
| How do you protect different users’ records? | Every related account/category/transaction/budget is checked against the authenticated user. Integration tests verify cross-user access is rejected. |
| Where is the API key? | Private backend environment configuration; never React or the Git repository. |
| What if the model invents a category? | Unknown IDs and unsupported budget amounts are rejected. Generated narrative still requires review. |
| What if Google’s quota is exhausted? | An explicit quota error appears. Recording, budgets, analytics, and reports remain available; the app does not fabricate AI output. |
| What was tested? | Fourteen backend cases, real Google calls for all four AI features, and browser checks of the core flow. See document 20 for the current database/deployment limits. |

## Source guide for explaining implementation

- App state and UI requests: `frontend/src/App.jsx` and `frontend/src/api.js`.
- Screens and forms: `frontend/src/pages.jsx` and `frontend/src/components.jsx`.
- Financial arithmetic: `frontend/src/finance.js` and `backend/src/main/java/com/paisa/service/FinanceService.java`.
- AI rules and provider calls: `AiService.java` and `GeminiClient.java` in the backend service directory.
- Access control: `backend/src/main/java/com/paisa/config/SecurityConfig.java`.
- Database: `database/er-diagram.svg`, `database/schema.sql`, and the five JPA model files.

Do not claim production MySQL, public backend deployment, or a submitted repository
until their current checks pass. Describe AI-assisted development honestly if asked.
