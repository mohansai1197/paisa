import test from "node:test";
import assert from "node:assert/strict";
import { validateBalanceChange, accountBalance } from "./finance.js";
import { makeSample } from "./data.js";

for (const type of ["CASH", "BANK", "WALLET"]) {
  test(`${type}: validate resulting balance on create, edit, move, delete and opening-balance change`, () => {
    const data = {
      accounts: [{ id: 1, name: type, type, openingBalance: 100 },
        { id: 2, name: "Empty", type, openingBalance: 0 }],
      transactions: [{ id: 1, accountId: 1, type: "INCOME", amount: 50 },
        { id: 2, accountId: 1, type: "EXPENSE", amount: 125 }],
    };
    const expense = { accountId: 1, type: "EXPENSE", amount: 25 };
    assert.doesNotThrow(() => validateBalanceChange(data, "transactions", expense));
    assert.throws(() => validateBalanceChange(data, "transactions", { ...expense, amount: 25.01 }), /Insufficient funds/);
    assert.throws(() => validateBalanceChange(data, "transactions", { ...expense, amount: 150.01 }, 2), /Insufficient funds/);
    assert.throws(() => validateBalanceChange(data, "transactions", { ...expense, accountId: 2 }, 2), /Insufficient funds/);
    assert.throws(() => validateBalanceChange(data, "transactions", { accountId: 2, type: "INCOME", amount: 50 }, 1), /Insufficient funds/);
    assert.throws(() => validateBalanceChange(data, "transactions", null, 1, true), /Insufficient funds/);
    assert.doesNotThrow(() => validateBalanceChange(data, "transactions", null, 2, true));
    assert.throws(() => validateBalanceChange(data, "accounts", { ...data.accounts[0], openingBalance: 74.99 }, 1), /Insufficient funds/);
    assert.throws(() => validateBalanceChange(data, "accounts", { name: "New", openingBalance: -1 }), /zero or more/);
    assert.equal(accountBalance(data, 1), 25, "Validation must not mutate records");
  });
}

test("fictional sample accounts have enough funding", () => {
  const sample = makeSample();
  for (const account of sample.accounts) assert.ok(accountBalance(sample, account.id) >= 0);
});
