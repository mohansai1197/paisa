export const currentMonth = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
};
export const today = () => {
  const d = new Date();
  return `${currentMonth()}-${String(d.getDate()).padStart(2, "0")}`;
};
export function makeSample() {
  const categories = [
    ["Food & dining", "EXPENSE", "#21866c"],
    ["Shopping", "EXPENSE", "#7984ca"],
    ["Transport", "EXPENSE", "#e2ac65"],
    ["Bills & utilities", "EXPENSE", "#d58691"],
    ["Entertainment", "EXPENSE", "#6baebd"],
    ["Health", "EXPENSE", "#a28ac3"],
    ["Other", "EXPENSE", "#9bada4"],
    ["Salary", "INCOME", "#21866c"],
    ["Freelance", "INCOME", "#6baebd"],
  ].map(([name, type, color], i) => ({ id: i + 1, name, type, color }));
  const transactions = [];
  const records = [
    ["Monthly salary", 65000, 8, 1],
    ["Weekly grocery run", 1840, 1, 2],
    ["Internet & electricity", 3200, 4, 3],
    ["Metro pass", 1200, 3, 4],
    ["Coffee at Third Wave", 320, 1, 5],
    ["A little wardrobe refresh", 2499, 2, 6],
    ["Freelance design project", 8500, 9, 8],
    ["Weekend movie night", 850, 5, 9],
    ["Fresh produce", 920, 1, 10],
    ["Lunch with friends", 680, 1, 12],
    ["Health & wellness", 650, 6, 13],
    ["Music subscription", 499, 5, 15],
    ["Grocery essentials", 1260, 1, 17],
    ["Dinner at The Green Table", 1480, 1, 19],
    ["Bookstore finds", 780, 2, 20],
  ];
  const now = new Date();
  for (let p = 0; p < 3; p++) {
    const date = new Date(now.getFullYear(), now.getMonth() - p, 1);
    const month = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
    records.forEach(([description, amount, categoryId, day], i) => {
      transactions.push({
        id: transactions.length + 1,
        description,
        amount: Number(
          (amount * (p === 1 ? 1.12 : p === 2 ? 0.94 : 1)).toFixed(2),
        ),
        categoryId,
        accountId: i % 5 === 0 && i > 0 ? 2 : 1,
        type: categories[categoryId - 1].type,
        date: `${month}-${String(day).padStart(2, "0")}`,
      });
    });
  }
  return {
    user: { name: "Alex Morgan", email: "alex@example.test" },
    accounts: [
      { id: 1, name: "Everyday account", type: "BANK", openingBalance: 12000 },
      { id: 2, name: "Cash wallet", type: "CASH", openingBalance: 12000 },
    ],
    categories,
    transactions,
    budgets: [8000, 5000, 3000, 4500, 2500].map((limitAmount, i) => ({
      id: i + 1,
      categoryId: i + 1,
      month: currentMonth(),
      limitAmount,
    })),
  };
}
const storageKey = "paisa.sample.v1";
export function readSample() {
  try {
    const value = JSON.parse(localStorage.getItem(storageKey));
    if (
      value &&
      Array.isArray(value.transactions) &&
      Array.isArray(value.accounts) &&
      Array.isArray(value.categories) &&
      Array.isArray(value.budgets)
    )
      return value;
  } catch {}
  return makeSample();
}
export function storeSample(value) {
  try {
    localStorage.setItem(storageKey, JSON.stringify(value));
  } catch {
    throw new Error(
      "Browser storage is full or unavailable. This change could not be saved.",
    );
  }
}
