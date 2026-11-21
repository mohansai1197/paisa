import React, { useEffect, useRef, useState } from "react";
import {
  X,
  Plus,
  Sparkles,
  LoaderCircle,
  Utensils,
  ShoppingBag,
  Car,
  Zap,
  Clapperboard,
  Heart,
  Briefcase,
  MoreHorizontal,
  ArrowDownLeft,
  ArrowUpRight,
  Pencil,
  Trash2,
  Wallet,
} from "lucide-react";
import { money, dateLabel, accountBalance, cents } from "./finance";
import { today } from "./data";
import { api } from "./api";

export function Button({
  children,
  variant = "primary",
  className = "",
  ...props
}) {
  return (
    <button className={`button ${variant} ${className}`} {...props}>
      {children}
    </button>
  );
}
export function Empty({ icon: Icon = Wallet, title, description, action }) {
  return (
    <div className="empty-state">
      <div className="empty-icon">
        <Icon size={25} />
      </div>
      <h3>{title}</h3>
      <p>{description}</p>
      {action}
    </div>
  );
}
export function SectionTitle({ title, subtitle, action }) {
  return (
    <div className="section-heading">
      <div>
        <h2>{title}</h2>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}
export function PageHeader({ eyebrow, title, description, actions }) {
  return (
    <div className="page-heading">
      <div>
        <div className="eyebrow">{eyebrow || "YOUR PERSONAL WORKSPACE"}</div>
        <h1>{title}</h1>
        <p>{description}</p>
      </div>
      <div className="heading-actions">{actions}</div>
    </div>
  );
}
export function CategoryIcon({ category, size = 18 }) {
  const name = category?.name || "";
  const Icon = /food|dining/i.test(name)
    ? Utensils
    : /shop/i.test(name)
      ? ShoppingBag
      : /transport/i.test(name)
        ? Car
        : /bill/i.test(name)
          ? Zap
          : /entertain/i.test(name)
            ? Clapperboard
            : /health/i.test(name)
              ? Heart
              : /salary|freelance/i.test(name)
                ? Briefcase
                : MoreHorizontal;
  return (
    <span
      className="category-icon"
      style={{
        color: category?.color || "#21866c",
        background: `${category?.color || "#21866c"}15`,
      }}
    >
      <Icon size={size} />
    </span>
  );
}
export function Progress({ spent, limit }) {
  const pct = limit > 0 ? Math.round((spent / limit) * 100) : 0;
  return (
    <div
      className="budget-track"
      role="progressbar"
      aria-label={`${pct}% of budget spent`}
      aria-valuenow={Math.min(pct, 100)}
      aria-valuemin={0}
      aria-valuemax={100}
    >
      <span
        style={{
          width: `${Math.min(100, Math.max(0, pct))}%`,
          background:
            pct >= 100 ? "#c35b61" : pct >= 85 ? "#d6a04c" : "#24886c",
        }}
      />
    </div>
  );
}
export function BudgetRow({ budget, onEdit }) {
  const pct = Math.round((budget.spent / budget.limitAmount) * 100);
  return (
    <button className="budget-row" onClick={() => onEdit(budget)}>
      <div className="budget-row-top">
        <span className="budget-name">
          <CategoryIcon category={budget.category} />
          {budget.category?.name || "Category"}
        </span>
        <span>
          {money(budget.spent)} <small>/ {money(budget.limitAmount)}</small>
        </span>
      </div>
      <Progress spent={budget.spent} limit={budget.limitAmount} />
      <div className="budget-row-bottom">
        <span>
          {pct >= 100
            ? "Budget exceeded"
            : `${money(budget.limitAmount - budget.spent)} left`}
        </span>
        <span>{pct}%</span>
      </div>
    </button>
  );
}
export function TransactionTable({
  rows,
  data,
  onEdit,
  onDelete,
  compact = false,
}) {
  return (
    <div className="table-wrap">
      <table className="transactions-table">
        <thead>
          <tr>
            <th>Transaction</th>
            <th>Category</th>
            {!compact && <th>Account</th>}
            <th>Date</th>
            <th className="amount-cell">Amount</th>
            {!compact && (
              <th>
                <span className="sr-only">Actions</span>
              </th>
            )}
          </tr>
        </thead>
        <tbody>
          {rows.map((t) => {
            const category = data.categories.find((c) => c.id === t.categoryId);
            return (
              <tr key={t.id}>
                <td>
                  <button
                    className="transaction-name"
                    onClick={() => onEdit(t)}
                  >
                    <CategoryIcon category={category} />
                    <span>
                      {t.description}
                      <small>
                        {t.type === "INCOME" ? "Money in" : "Money out"}
                      </small>
                    </span>
                  </button>
                </td>
                <td>
                  <span className="category-label">
                    {category?.name || "Uncategorized"}
                  </span>
                </td>
                {!compact && (
                  <td className="muted">
                    {data.accounts.find((a) => a.id === t.accountId)?.name ||
                      "Account"}
                  </td>
                )}
                <td className="muted nowrap">{dateLabel(t.date)}</td>
                <td
                  className={`amount-cell ${t.type === "INCOME" ? "positive" : ""}`}
                >
                  {t.type === "INCOME" ? "+" : "−"}
                  {money(t.amount, 2)}
                </td>
                {!compact && (
                  <td>
                    <div className="row-actions">
                      <button
                        className="icon-button"
                        onClick={() => onEdit(t)}
                        aria-label={`Edit ${t.description}`}
                      >
                        <Pencil size={15} />
                      </button>
                      <button
                        className="icon-button danger-text"
                        onClick={() => onDelete(t)}
                        aria-label={`Delete ${t.description}`}
                      >
                        <Trash2 size={15} />
                      </button>
                    </div>
                  </td>
                )}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
export function Modal({ title, description, children, onClose, wide = false }) {
  const ref = useRef(null);
  useEffect(() => {
    const dialog = ref.current;
    dialog.showModal();
    return () => dialog.close();
  }, []);
  return (
    <dialog
      ref={ref}
      className={`modal ${wide ? "wide" : ""}`}
      onCancel={(e) => {
        e.preventDefault();
        onClose();
      }}
      onClick={(e) => {
        if (e.target === ref.current) onClose();
      }}
    >
      <div className="modal-inner">
        <div className="modal-header">
          <div>
            <h2>{title}</h2>
            {description && <p>{description}</p>}
          </div>
          <button
            className="icon-button"
            aria-label="Close dialog"
            onClick={onClose}
          >
            <X size={20} />
          </button>
        </div>
        {children}
      </div>
    </dialog>
  );
}
function FormError({ error }) {
  return error ? (
    <div role="alert" className="form-error">
      {error}
    </div>
  ) : null;
}
export function TransactionModal({
  data,
  transaction,
  type = "EXPENSE",
  mode,
  onClose,
  onSave,
  onCategory,
}) {
  const [draft, setDraft] = useState(
    transaction || {
      type,
      description: "",
      amount: "",
      date: today(),
      accountId: data.accounts[0]?.id || "",
      categoryId: data.categories.find((c) => c.type === type)?.id || "",
    },
  );
  const [busy, setBusy] = useState(false),
    [aiBusy, setAiBusy] = useState(false),
    [error, setError] = useState(""),
    [hint, setHint] = useState(""),
    [addingCategory, setAddingCategory] = useState(false),
    [categoryName, setCategoryName] = useState("");
  const field = (key, value) => setDraft((d) => ({ ...d, [key]: value }));
  const selectedAccount = data.accounts.find((a) => a.id === Number(draft.accountId));
  const available = (cents(accountBalance(data, Number(draft.accountId))) -
    (transaction?.accountId === Number(draft.accountId)
      ? (transaction.type === "INCOME" ? 1 : -1) * cents(transaction.amount) : 0)) / 100;
  async function save(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      await onSave({
        ...draft,
        amount: Number(draft.amount),
        accountId: Number(draft.accountId),
        categoryId: Number(draft.categoryId),
      });
      onClose();
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  async function categorize() {
    setError("");
    setHint("");
    if (!draft.description.trim()) {
      setError("Add a description so AI has something to categorize.");
      return;
    }
    if (mode === "sample") {
      setError(
        "AI needs a connected workspace. Sign in and connect an AI provider to generate a real suggestion.",
      );
      return;
    }
    setAiBusy(true);
    try {
      const r = await api("/ai/categorize", {
        method: "POST",
        body: { description: draft.description },
      });
      field("categoryId", r.categoryId);
      setHint(r.explanation + " You can change the suggestion before saving.");
    } catch (e) {
      setError(e.message);
    } finally {
      setAiBusy(false);
    }
  }
  async function addCategory() {
    if (!categoryName.trim()) return;
    setBusy(true);
    setError("");
    try {
      const c = await onCategory({
        name: categoryName.trim(),
        type: draft.type,
      });
      field("categoryId", c.id);
      setAddingCategory(false);
      setCategoryName("");
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <Modal
      title={transaction ? "Edit transaction" : "Add a transaction"}
      description="A small entry. A clearer financial picture."
      onClose={onClose}
    >
      <form onSubmit={save}>
        <div className="type-switch">
          <button
            type="button"
            aria-pressed={draft.type === "EXPENSE"}
            className={draft.type === "EXPENSE" ? "active" : ""}
            onClick={() => {
              setDraft((d) => ({
                ...d,
                type: "EXPENSE",
                categoryId:
                  data.categories.find((c) => c.type === "EXPENSE")?.id || "",
              }));
              setHint("");
            }}
          >
            <ArrowUpRight size={16} />
            Expense
          </button>
          <button
            type="button"
            aria-pressed={draft.type === "INCOME"}
            className={draft.type === "INCOME" ? "active" : ""}
            onClick={() => {
              setDraft((d) => ({
                ...d,
                type: "INCOME",
                categoryId:
                  data.categories.find((c) => c.type === "INCOME")?.id || "",
              }));
              setHint("");
            }}
          >
            <ArrowDownLeft size={16} />
            Income
          </button>
        </div>
        <label>
          Amount (INR)
          <input
            autoFocus
            type="number"
            min="0.01"
            max="99999999999"
            step="0.01"
            required
            placeholder="0.00"
            value={draft.amount}
            onChange={(e) => field("amount", e.target.value)}
            className="amount-input"
          />
        </label>
        <p className="form-note">
          {selectedAccount?.name}: {money(available, 2)} available before this entry.
          {" "}Cash, bank, and wallet balances must stay at zero or above.
        </p>
        <label>
          Description
          <input
            maxLength={180}
            required
            placeholder="What was this for?"
            value={draft.description}
            onChange={(e) => field("description", e.target.value)}
          />
        </label>
        <div className="form-grid">
          <label>
            Category
            <select
              required
              value={draft.categoryId}
              onChange={(e) => field("categoryId", Number(e.target.value))}
            >
              {data.categories
                .filter((c) => c.type === draft.type)
                .map((c) => (
                  <option value={c.id} key={c.id}>
                    {c.name}
                  </option>
                ))}
            </select>
          </label>
          <label>
            Account
            <select
              required
              value={draft.accountId}
              onChange={(e) => field("accountId", Number(e.target.value))}
            >
              <option value="" disabled>
                Choose an account
              </option>
              {data.accounts.map((a) => (
                <option value={a.id} key={a.id}>
                  {a.name}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="form-inline">
          <button
            className="text-button"
            type="button"
            onClick={() => setAddingCategory(!addingCategory)}
          >
            <Plus size={14} />
            New category
          </button>
          {draft.type === "EXPENSE" && (
            <button
              className="text-button"
              type="button"
              disabled={aiBusy}
              onClick={categorize}
            >
              {aiBusy ? (
                <LoaderCircle className="spin" size={14} />
              ) : (
                <Sparkles size={14} />
              )}
              Suggest category with AI
            </button>
          )}
        </div>
        {addingCategory && (
          <div className="inline-category">
            <input
              aria-label="New category name"
              maxLength={60}
              placeholder="Category name"
              value={categoryName}
              onChange={(e) => setCategoryName(e.target.value)}
            />
            <Button
              type="button"
              variant="secondary"
              disabled={busy}
              onClick={addCategory}
            >
              Add
            </Button>
          </div>
        )}
        <label>
          Date
          <input
            type="date"
            required
            value={draft.date}
            onChange={(e) => field("date", e.target.value)}
          />
        </label>
        {hint && (
          <div className="form-hint" aria-live="polite">
            <Sparkles size={16} />
            {hint}
          </div>
        )}
        <FormError error={error} />
        <div className="modal-footer">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" disabled={busy || !data.accounts.length}>
            {busy ? <LoaderCircle className="spin" size={16} /> : null}
            {busy ? "Saving…" : "Save transaction"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
export function AccountModal({ account, onClose, onSave }) {
  const [d, setD] = useState(
    account || { name: "", type: "BANK", openingBalance: 0 },
  );
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  return (
    <Modal
      title={account ? "Edit account" : "Add an account"}
      description="Track your money across bank accounts, wallets, and cash."
      onClose={onClose}
    >
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          try {
            await onSave({ ...d, openingBalance: Number(d.openingBalance) });
            onClose();
          } catch (e) {
            setError(e.message);
          } finally {
            setBusy(false);
          }
        }}
      >
        <label>
          Account name
          <input
            autoFocus
            required
            maxLength={80}
            placeholder="e.g. Everyday account"
            value={d.name}
            onChange={(e) => setD({ ...d, name: e.target.value })}
          />
        </label>
        <label>
          Account type
          <select
            value={d.type}
            onChange={(e) => setD({ ...d, type: e.target.value })}
          >
            <option value="BANK">Bank account</option>
            <option value="CASH">Cash</option>
            <option value="WALLET">Digital wallet</option>
          </select>
        </label>
        <label>
          Opening balance (INR)
          <input
            required
            type="number"
            min="0"
            max="99999999999.99"
            step="0.01"
            value={d.openingBalance}
            onChange={(e) => setD({ ...d, openingBalance: e.target.value })}
          />
        </label>
        <p className="form-note">
          Your current balance is calculated from this opening balance and all
          recorded transactions. Opening and resulting balances must be zero or more.
        </p>
        <FormError error={error} />
        <div className="modal-footer">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" disabled={busy}>
            {busy ? "Saving…" : "Save account"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
export function BudgetModal({ budget, data, month, onClose, onSave }) {
  const [d, setD] = useState(
    budget || {
      categoryId: data.categories.find((c) => c.type === "EXPENSE")?.id || "",
      month,
      limitAmount: "",
    },
  );
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  return (
    <Modal
      title={budget ? "Edit monthly budget" : "Create a budget"}
      description="Give your spending a little direction."
      onClose={onClose}
    >
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          try {
            await onSave({
              categoryId: Number(d.categoryId),
              month: d.month,
              limitAmount: Number(d.limitAmount),
            });
            onClose();
          } catch (e) {
            setError(e.message);
          } finally {
            setBusy(false);
          }
        }}
      >
        <label>
          Category
          <select
            required
            value={d.categoryId}
            onChange={(e) => setD({ ...d, categoryId: e.target.value })}
          >
            {data.categories
              .filter((c) => c.type === "EXPENSE")
              .map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
          </select>
        </label>
        <label>
          Month
          <input
            required
            type="month"
            value={d.month}
            onChange={(e) => setD({ ...d, month: e.target.value })}
          />
        </label>
        <label>
          Monthly limit (INR)
          <input
            autoFocus
            type="number"
            min="0.01"
            step="0.01"
            required
            placeholder="e.g. 5000"
            value={d.limitAmount}
            onChange={(e) => setD({ ...d, limitAmount: e.target.value })}
          />
        </label>
        <p className="form-note">
          Saving replaces any existing budget for this category and month.
        </p>
        <FormError error={error} />
        <div className="modal-footer">
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" disabled={busy}>
            {busy ? "Saving…" : "Save budget"}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
export function AuthModal({ onClose, onAuth }) {
  const [stage, setStage] = useState("login"),
    [name, setName] = useState(""),
    [email, setEmail] = useState(""),
    [password, setPassword] = useState(""),
    [confirmation, setConfirmation] = useState(""),
    [code, setCode] = useState(""),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(""),
    [notice, setNotice] = useState("");
  const register = stage === "register", verify = stage === "verify",
    forgot = stage === "forgot", reset = stage === "reset";
  function switchStage(next) {
    setStage(next); setError(""); setNotice(""); setCode(""); setPassword(""); setConfirmation("");
  }
  async function submit(e) {
    e.preventDefault(); setBusy(true); setError(""); setNotice("");
    try {
      if (forgot) {
        const result = await onAuth("/auth/forgot-password", { email });
        setStage("reset"); setNotice(result.message); setCode("");
      } else if (reset) {
        if (password !== confirmation) throw new Error("The passwords do not match.");
        const result = await onAuth("/auth/reset-password", { email, code, password });
        switchStage("login"); setNotice(result.message);
      } else if (register) {
        const result = await onAuth("/auth/register", { name, email, password });
        setStage("verify"); setNotice(result.message); setCode("");
      } else if (verify) {
        const result = await onAuth("/auth/verify", { email, code, password });
        switchStage("login"); setNotice(result.message);
      } else {
        await onAuth("/auth/login", { email, password }); onClose();
      }
    } catch (e) {
      setError(e.message);
      if (e.verificationRequired) { setStage("verify"); setCode(""); }
    } finally { setBusy(false); }
  }
  async function resend() {
    setBusy(true); setError(""); setNotice("");
    try {
      const result = await onAuth(reset ? "/auth/forgot-password" : "/auth/resend-verification", { email });
      setNotice(result.message); setCode("");
    } catch (e) { setError(e.message); }
    finally { setBusy(false); }
  }
  return (
    <Modal
      title={forgot ? "Forgot your password?" : reset ? "Choose a new password" : verify ? "Verify your email" : register ? "A fresh start for your finances" : "Welcome back"}
      description={forgot ? "We’ll email a reset code if your account is eligible." : reset ? "Use the reset code sent to your inbox." : verify ? "Confirm your email before accessing your workspace." : register ? "Create your personal Paisa workspace." : "Sign in to your connected workspace."}
      onClose={onClose}
    >
      <form onSubmit={submit}>
        {register && <label>Your name<input autoFocus required maxLength={80} autoComplete="name" value={name} onChange={(e) => setName(e.target.value)} /></label>}
        <label>Email<input autoFocus={!register} type="email" required maxLength={180} autoComplete="email" value={email} onChange={(e) => { setEmail(e.target.value); setCode(""); }} /></label>
        {(verify || reset) && <>
          <p className="form-note">Enter the six-digit code from your inbox. Codes expire in 10 minutes. You can request another after 60 seconds.</p>
          <label>{reset ? "Password reset code" : "Verification code"}<input inputMode="numeric" pattern="[0-9]{6}" maxLength={6} required autoComplete="one-time-code" value={code} onChange={(e) => setCode(e.target.value.replace(/[^0-9]/g, ""))} /></label>
        </>}
        {!forgot && <label>{reset ? "New password" : verify ? "Choose your account password" : "Password"}<input type="password" minLength={stage === "login" ? 1 : 8} maxLength={72} required autoComplete={stage === "login" ? "current-password" : "new-password"} value={password} onChange={(e) => setPassword(e.target.value)} /></label>}
        {reset && <label>Confirm new password<input type="password" minLength={8} maxLength={72} required autoComplete="new-password" value={confirmation} onChange={(e) => setConfirmation(e.target.value)} /></label>}
        {reset && <p className="form-note">Use at least 8 characters. Resetting your password signs out previous sessions.</p>}
        {(register || verify) && <p className="form-note">Use at least 8 characters. Your workspace stays locked until your email is verified.</p>}
        {notice && <p className="form-note" role="status">{notice}</p>}
        <FormError error={error} />
        <Button className="full-width" type="submit" disabled={busy}>{busy ? "One moment…" : forgot ? "Send reset code" : reset ? "Reset password" : verify ? "Verify email" : register ? "Send verification code" : "Sign in"}</Button>
        {(verify || reset) && <Button type="button" variant="secondary" className="full-width" disabled={busy || !email.trim()} onClick={resend}>Send another code</Button>}
        {stage === "login" && <p className="auth-switch"><button type="button" className="text-button" disabled={busy} onClick={() => switchStage("forgot")}>Forgot password?</button></p>}
        <p className="auth-switch">
          {stage === "login" ? "New to Paisa?" : forgot || reset ? "Remember your password?" : "Already verified?"}{" "}
          <button type="button" className="text-button" disabled={busy} onClick={() => switchStage(stage === "login" ? "register" : "login")}>{stage === "login" ? "Create an account" : "Sign in"}</button>
        </p>
        {(register || stage === "login") && <p className="auth-switch"><button type="button" className="text-button" disabled={busy} onClick={() => switchStage("verify")}>Verify an existing email</button></p>}
      </form>
    </Modal>
  );
}
export function ConfirmModal({
  title,
  description,
  label = "Confirm",
  onClose,
  onConfirm,
}) {
  const [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  return (
    <Modal title={title} description={description} onClose={onClose}>
      <FormError error={error} />
      <div className="modal-footer">
        <Button variant="secondary" onClick={onClose}>
          Cancel
        </Button>
        <Button
          variant="danger"
          disabled={busy}
          onClick={async () => {
            setBusy(true);
            try {
              await onConfirm();
              onClose();
            } catch (e) {
              setError(e.message);
            } finally {
              setBusy(false);
            }
          }}
        >
          {busy ? "Working…" : label}
        </Button>
      </div>
    </Modal>
  );
}
