# Account balance validation

Cash, bank, and digital wallet accounts cannot be overdrawn in Paisa. This applies
to their recorded balance: opening balance plus every recorded income minus every
recorded expense. Zero is allowed. Bank overdrafts are not supported.

## What leads where

1. The transaction form shows the selected account's balance before this entry.
2. Saving checks the resulting balance, reversing the old entry first when editing.
3. Moving an entry checks both its old and new accounts. Deleting income and
   reducing the opening balance also check the resulting account balance.
4. A negative result shows “Insufficient funds” and leaves all saved records unchanged.
5. The backend repeats the validation within a database transaction. It locks the
   owner's balance-changing writes before reading records so two simultaneous
   expenses cannot spend the same available funds twice.

## Files

- `frontend/src/finance.js`: shared client validation and integer-cent calculations.
- `frontend/src/App.jsx`: checks connected and browser-sample mutations before saving.
- `frontend/src/components.jsx`: available balance and nonnegative opening input.
- `frontend/src/pages.jsx`: explicit warning for legacy negative balances.
- `backend/src/main/java/com/paisa/api/Requests.java`: nonnegative opening validation.
- `backend/src/main/java/com/paisa/service/FinanceService.java`: authoritative checks.
- `backend/src/main/java/com/paisa/repository/UserRepository.java`: database write lock.

These paths are relative to `/Users/mohansai/Documents/ChatGPT/hackathon`.

## Sample funding correction

The old fictional cash fixture had a ₹2,500 opening balance but more cash expenses
than that. New fixtures use ₹12,000. The existing local fictional review wallet and
saved browser sample were explicitly updated to the same opening balance; expense
records were preserved. Other existing records are not automatically rewritten or
clamped to zero. A legacy negative account shows a warning so its owner can record
missing income or correct its true opening balance.

## Verification

- Nineteen backend tests pass, including all three account types, exact-zero
  spending, overspending by ₹0.01, edits, moves, income deletion, opening-balance
  reductions, rollback, seed funding, and simultaneous expenses.
- Four frontend tests pass with `npm test` in `frontend`.
- Live MySQL API checks passed for cash, bank, wallets, income deletion, edits,
  opening changes, exact-zero balances, and concurrent spending. Temporary test
  transactions and accounts were removed.
- Browser checks rejected bank and cash expenses exceeding their balances by ₹0.01.
  No rejected entry was saved. Screenshot: `output/paisa-insufficient-funds.png`.
- Production frontend build and combined Java release passed. The running local
  MySQL backend has been restarted with this release.

This rule validates the resulting recorded balance, not the historical running
balance at each transaction date. It does not connect to an actual bank or move money.
