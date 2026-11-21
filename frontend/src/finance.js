export const money = (value, decimals = 0) =>
  new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: decimals,
    minimumFractionDigits: decimals,
  }).format(Number(value) || 0);
export const shortMoney = (value) =>
  Math.abs(value) >= 1000
    ? `₹${(value / 1000).toFixed(value % 1000 === 0 ? 0 : 1)}k`
    : money(value);
export const monthLabel = (month) =>
  new Date(`${month}-15T12:00:00`).toLocaleDateString("en-IN", {
    month: "long",
    year: "numeric",
  });
export const dateLabel = (date) =>
  new Date(`${date}T12:00:00`).toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
  });
export const cents = (value) => Math.round(Number(value) * 100);
export function previousMonth(month) {
  const [y, m] = month.split("-").map(Number);
  const d = new Date(y, m - 2, 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}
export function summarize(data, month) {
  const rows = data.transactions.filter((t) => t.date.startsWith(month));
  const income =
    rows
      .filter((t) => t.type === "INCOME")
      .reduce((s, t) => s + cents(t.amount), 0) / 100;
  const expenses =
    rows
      .filter((t) => t.type === "EXPENSE")
      .reduce((s, t) => s + cents(t.amount), 0) / 100;
  const categories = data.categories
    .filter((c) => c.type === "EXPENSE")
    .map((c) => ({
      ...c,
      spent:
        rows
          .filter((t) => t.categoryId === c.id && t.type === "EXPENSE")
          .reduce((s, t) => s + cents(t.amount), 0) / 100,
    }))
    .sort((a, b) => b.spent - a.spent);
  const budgets = data.budgets
    .filter((b) => b.month === month)
    .map((b) => ({
      ...b,
      category: data.categories.find((c) => c.id === b.categoryId),
      spent: categories.find((c) => c.id === b.categoryId)?.spent || 0,
    }));
  const totalBudget =
    budgets.reduce((s, b) => s + cents(b.limitAmount), 0) / 100;
  const days = new Date(
    Number(month.slice(0, 4)),
    Number(month.slice(5)),
    0,
  ).getDate();
  const flow = Array.from({ length: days }, (_, i) => {
    const r = rows.filter((t) => Number(t.date.slice(-2)) === i + 1);
    return {
      day: String(i + 1),
      income:
        r
          .filter((t) => t.type === "INCOME")
          .reduce((s, t) => s + cents(t.amount), 0) / 100,
      expenses:
        r
          .filter((t) => t.type === "EXPENSE")
          .reduce((s, t) => s + cents(t.amount), 0) / 100,
    };
  });
  return {
    rows,
    income,
    expenses,
    net: Math.round((income - expenses) * 100) / 100,
    categories,
    budgets,
    totalBudget,
    flow,
  };
}
export function accountBalance(data, id) {
  const a = data.accounts.find((v) => v.id === id);
  return (
    (cents(a?.openingBalance || 0) +
      data.transactions
        .filter((t) => t.accountId === id)
        .reduce(
          (sum, t) => sum + (t.type === "INCOME" ? 1 : -1) * cents(t.amount),
          0,
        )) /
    100
  );
}
export function validateBalanceChange(data, resource, input, id, deleting = false) {
  if (!["accounts", "transactions"].includes(resource)) return;
  const next = structuredClone(data);
  const existing = data[resource].find((item) => item.id === id);
  const affected = new Set();
  if (resource === "accounts") {
    if (deleting) return;
    if (!Number.isFinite(Number(input.openingBalance)) || Number(input.openingBalance) < 0)
      throw new Error("Opening balance must be zero or more.");
    affected.add(id ?? "new-account");
  } else {
    if (existing) affected.add(existing.accountId);
    if (!deleting) {
      if (!Number.isFinite(Number(input.amount)) || Number(input.amount) <= 0)
        throw new Error("Enter a transaction amount greater than zero.");
      if (!data.accounts.some((account) => account.id === input.accountId))
        throw new Error("Choose an existing account.");
      affected.add(input.accountId);
    }
  }
  next[resource] = next[resource].filter((item) => item.id !== id);
  if (!deleting) next[resource].push({ ...input, id: id ?? "new-account" });
  for (const accountId of affected) {
    if (accountBalance(next, accountId) < 0) {
      const account = next.accounts.find((item) => item.id === accountId);
      throw new Error(`Insufficient funds in “${account?.name || "this account"}”. This change would leave a negative balance. Add the missing income or correct the opening balance first.`);
    }
  }
}
function csvCell(value) {
  let v = String(value ?? "");
  if (/^[=+@\-\t\r]/.test(v.trimStart())) v = `'${v}`;
  return `"${v.replaceAll('"', '""')}"`;
}
export function reportCsv(data, month) {
  const s = summarize(data, month);
  const rows = [
    ["Date", "Description", "Type", "Category", "Account", "Amount (INR)"],
    ...s.rows.map((t) => [
      t.date,
      t.description,
      t.type,
      data.categories.find((c) => c.id === t.categoryId)?.name,
      data.accounts.find((a) => a.id === t.accountId)?.name,
      Number(t.amount).toFixed(2),
    ]),
    [],
    ["Summary", "Amount (INR)"],
    ["Income", s.income.toFixed(2)],
    ["Expenses", s.expenses.toFixed(2)],
    ["Net cash flow", s.net.toFixed(2)],
  ];
  return rows.map((r) => r.map(csvCell).join(",")).join("\r\n");
}
export function downloadBlob(blob, name) {
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = name;
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 1500);
}
