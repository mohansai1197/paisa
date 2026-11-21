import React, { useEffect, useMemo, useRef, useState } from "react";
import {
  LayoutDashboard,
  ArrowLeftRight,
  Wallet,
  Target,
  Sparkles,
  FileText,
  Plus,
  Bell,
  ChevronDown,
  Menu,
  X,
  LogOut,
  LogIn,
  CheckCircle2,
  Info,
  RefreshCw,
  Leaf,
  ShieldCheck,
  ExternalLink,
} from "lucide-react";
import { api, resetCsrf } from "./api";
import { readSample, storeSample, makeSample, currentMonth } from "./data";
import { summarize, reportCsv, downloadBlob, monthLabel, validateBalanceChange } from "./finance";
import {
  Button,
  TransactionModal,
  AccountModal,
  BudgetModal,
  AuthModal,
  ConfirmModal,
  Modal,
} from "./components";
import {
  Overview,
  Transactions,
  Budgets,
  Accounts,
  Insights,
  Reports,
} from "./pages";

const links = [
  ["overview", "Overview", LayoutDashboard],
  ["transactions", "Transactions", ArrowLeftRight],
  ["accounts", "Accounts", Wallet],
  ["budgets", "Budgets", Target],
  ["insights", "AI insights", Sparkles],
  ["reports", "Reports", FileText],
];
const initialRoute = () =>
  links.some((l) => l[0] === window.location.hash.slice(1))
    ? window.location.hash.slice(1)
    : "overview";
function Application() {
  const [data, setData] = useState(readSample),
    [mode, setMode] = useState("sample"),
    [route, setRoute] = useState(initialRoute),
    [month, setMonth] = useState(currentMonth),
    [status, setStatus] = useState(null),
    [modal, setModal] = useState(null),
    [toast, setToast] = useState(null),
    [mobileOpen, setMobileOpen] = useState(false),
    [profileOpen, setProfileOpen] = useState(false),
    [notifications, setNotifications] = useState(false),
    [categoryFilter, setCategoryFilter] = useState(null),
    [insights, setInsights] = useState(null),
    [aiBusy, setAiBusy] = useState(false),
    [busy, setBusy] = useState(false);
  const timer = useRef();
  const insightRequest = useRef(0);
  const stats = useMemo(() => summarize(data, month), [data, month]);
  const notify = (message, error = false) => {
    setToast({ message, error });
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setToast(null), 6500);
  };
  useEffect(() => {
    let active = true;
    api("/status")
      .then(async (s) => {
        if (!active) return;
        if (s?.service !== "Paisa" || typeof s.aiConfigured !== "boolean")
          throw new Error("The workspace service is not connected.");
        setStatus({ ...s, available: true });
        try {
          const workspace = await api("/workspace");
          if (active) {
            setData(workspace);
            setMode("connected");
          }
        } catch {
          /* Logged-out users can explore the explicitly labeled sample workspace. */
        }
      })
      .catch(() => {
        if (active) setStatus({ available: false });
      });
    return () => {
      active = false;
      clearTimeout(timer.current);
    };
  }, []);
  useEffect(() => {
    const onHash = () => setRoute(initialRoute());
    window.addEventListener("hashchange", onHash);
    return () => window.removeEventListener("hashchange", onHash);
  }, []);
  useEffect(() => {
    // A result for older records or a different month must never replace the current review.
    insightRequest.current += 1;
    setInsights(null);
    setAiBusy(false);
  }, [data, month, mode]);
  function navigate(next) {
    setRoute(next);
    window.location.hash = next;
    setMobileOpen(false);
    setProfileOpen(false);
    setNotifications(false);
    window.scrollTo({ top: 0, behavior: "instant" });
  }
  function commitSample(next) {
    storeSample(next);
    setData(next);
    setInsights(null);
  }
  function openAuth() {
    setModal({ kind: status?.available === false ? "connection" : "auth" });
    setProfileOpen(false);
  }
  async function save(resource, input, id) {
    validateBalanceChange(data, resource, input, id);
    let result;
    if (mode === "connected") {
      result = await api(`/${resource}${id ? `/${id}` : ""}`, {
        method: id ? "PUT" : "POST",
        body: input,
      });
      setData(await api("/workspace"));
      setInsights(null);
    } else {
      const next = structuredClone(data),
        list = next[resource];
      if (
        resource === "categories" &&
        list.some(
          (c) =>
            c.type === input.type &&
            c.name.toLowerCase() === input.name.trim().toLowerCase(),
        )
      )
        throw new Error("That category already exists.");
      let index = id ? list.findIndex((v) => v.id === id) : -1;
      if (resource === "budgets")
        index = list.findIndex(
          (b) => b.categoryId === input.categoryId && b.month === input.month,
        );
      result = {
        ...input,
        id:
          index >= 0
            ? list[index].id
            : Math.max(0, ...list.map((v) => v.id)) + 1,
      };
      if (index >= 0) list[index] = result;
      else list.push(result);
      commitSample(next);
    }
    notify(
      resource === "transactions"
        ? "Transaction saved. Your financial picture is up to date."
        : resource === "budgets"
          ? "Your monthly budget is saved."
          : resource === "accounts"
            ? "Account saved."
            : "Category added.",
    );
    return result;
  }
  async function remove(resource, item) {
    validateBalanceChange(data, resource, null, item.id, true);
    if (
      resource === "accounts" &&
      data.transactions.some((t) => t.accountId === item.id)
    )
      throw new Error(
        "This account has transactions. Keep it to preserve your history.",
      );
    if (mode === "connected") {
      await api(`/${resource}/${item.id}`, { method: "DELETE" });
      setData(await api("/workspace"));
      setInsights(null);
    } else {
      const next = structuredClone(data);
      next[resource] = next[resource].filter((v) => v.id !== item.id);
      commitSample(next);
    }
    notify("Record removed. Your totals have been updated.");
  }
  function add(type = "EXPENSE") {
    if (!data.accounts.length) {
      setModal({ kind: "account" });
      notify("Add an account first, then record your transaction.");
      return;
    }
    setModal({ kind: "transaction", type });
  }
  function confirmDelete(resource, item) {
    setModal({
      kind: "confirm",
      title: `Delete ${resource === "transactions" ? "transaction" : resource === "accounts" ? "account" : "budget"}?`,
      description:
        resource === "transactions"
          ? `Remove “${item.description}”? Your balances, charts, and budget progress will update.`
          : resource === "accounts"
            ? `Remove “${item.name}”? Accounts with existing transactions cannot be deleted.`
            : "Remove this monthly category budget? Your transactions will stay unchanged.",
      label: "Delete record",
      onConfirm: () => remove(resource, item),
    });
  }
  async function authenticate(path, body) {
    const result = await api(path, { method: "POST", body });
    if (path !== "/auth/login") return result;
    resetCsrf();
    const workspace = await api("/workspace");
    setData(workspace);
    setMode("connected");
    setInsights(null);
    const s = await api("/status");
    setStatus(s);
    notify(
      `Welcome, ${workspace.user.name.split(" ")[0]}. Your workspace is ready.`,
    );
  }
  async function logout() {
    try {
      if (mode === "connected") await api("/auth/logout", { method: "POST" });
      resetCsrf();
      setMode("sample");
      setData(readSample());
      setInsights(null);
      setProfileOpen(false);
      notify("Signed out. You’re now viewing the sample workspace.");
    } catch (e) {
      notify(e.message, true);
    }
  }
  async function seed() {
    if (mode === "sample") return;
    if (!window.confirm("Add fictional demo records in two separate Demo accounts? Your existing records and budget limits will be kept. These records will be included in your charts and AI insights.")) return;
    setBusy(true);
    try {
      setData(await api("/demo/seed", { method: "POST" }));
      notify("Fictional sample records loaded into your workspace.");
    } catch (e) {
      notify(e.message, true);
    } finally {
      setBusy(false);
    }
  }
  async function generate() {
    if (mode === "sample") {
      notify(
        "Sign in to a connected workspace to generate real AI insights.",
        true,
      );
      return;
    }
    if (!stats.rows.length) {
      notify(
        "Add transactions for this month before generating insights.",
        true,
      );
      return;
    }
    const request = ++insightRequest.current;
    setAiBusy(true);
    try {
      const result = await api(
        `/ai/monthly?month=${encodeURIComponent(month)}`,
        { method: "POST" },
      );
      if (request !== insightRequest.current) return;
      setInsights(result);
      notify(
        "Your monthly insights are ready. Review suggestions before applying them.",
      );
    } catch (e) {
      if (request === insightRequest.current) notify(e.message, true);
    } finally {
      if (request === insightRequest.current) setAiBusy(false);
    }
  }
  async function download() {
    try {
      const blob =
        mode === "connected"
          ? await api(`/reports?month=${encodeURIComponent(month)}`, {
              download: true,
            })
          : new Blob(["\uFEFF" + reportCsv(data, month)], {
              type: "text/csv;charset=utf-8;",
            });
      downloadBlob(blob, `paisa-${month}.csv`);
      notify("Your monthly CSV report is ready.");
    } catch (e) {
      notify(e.message, true);
    }
  }
  const onBudget = (budget) => setModal({ kind: "budget", budget }),
    onAccount = (account) => setModal({ kind: "account", account }),
    onEdit = (transaction) => setModal({ kind: "transaction", transaction });
  const shared = {
    data,
    month,
    onAdd: add,
    onBudget,
    onNavigate: navigate,
    onEdit,
    mode,
    busy,
  };
  const alerts = stats.budgets.filter((b) => b.spent / b.limitAmount >= 0.85);
  return (
    <div className="app-shell">
      {mobileOpen && (
        <button
          className="sidebar-backdrop"
          aria-label="Close navigation"
          onClick={() => setMobileOpen(false)}
        />
      )}
      <aside className={`sidebar ${mobileOpen ? "open" : ""}`}>
        <a
          href="#overview"
          className="brand"
          onClick={() => navigate("overview")}
        >
          <span className="brand-mark">
            <Wallet size={23} strokeWidth={1.8} />
          </span>
          <span>
            pai<span className="brand-light">sa</span>
            <small>A clearer kind of finance.</small>
          </span>
        </a>
        <button
          className="mobile-close icon-button"
          aria-label="Close navigation"
          onClick={() => setMobileOpen(false)}
        >
          <X size={20} />
        </button>
        <div className="workspace-label">
          <span className="workspace-monogram">P</span>
          <div>
            <strong>Personal workspace</strong>
            <small>
              {mode === "sample"
                ? "Sample workspace"
                : "Your private workspace"}
            </small>
          </div>
          <ShieldCheck size={16} />
        </div>
        <div className="nav-label">WORKSPACE</div>
        <nav aria-label="Main navigation">
          {links.map(([key, label, Icon]) => (
            <button
              key={key}
              className={`nav-item ${route === key ? "selected" : ""}`}
              aria-current={route === key ? "page" : undefined}
              onClick={() => navigate(key)}
            >
              <Icon size={19} />
              {label}
              {key === "insights" && <span className="nav-ai">AI</span>}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="sidebar-thought">
            <div className="thought-icon">
              <Leaf size={22} />
            </div>
            <strong>
              Small steps.
              <br />A clearer tomorrow.
            </strong>
            <p>A little attention to your money goes a long way.</p>
            <button className="text-button" onClick={() => navigate("budgets")}>
              Make a spending plan
            </button>
          </div>
          <div className="sidebar-version">
            <ShieldCheck size={14} />
            Built around your privacy
          </div>
        </div>
      </aside>
      <div className="main-shell">
        <header className="topbar">
          <div className="topbar-left">
            <button
              className="mobile-menu icon-button"
              aria-label="Open navigation"
              onClick={() => setMobileOpen(true)}
            >
              <Menu size={21} />
            </button>
            <span className="breadcrumb">
              Workspace <span>/</span>{" "}
              <strong>{links.find((l) => l[0] === route)?.[1]}</strong>
            </span>
          </div>
          <div className="topbar-right">
            {route !== "accounts" && (
              <label className="month-picker">
                <span className="sr-only">Selected month</span>
                <input
                  type="month"
                  value={month}
                  onChange={(e) => {
                    if (e.target.value) {
                      setMonth(e.target.value);
                      setInsights(null);
                    }
                  }}
                />
              </label>
            )}
            <div className="popover-anchor">
              <button
                className={`icon-button notification-button ${alerts.length ? "has-alert" : ""}`}
                aria-label="Budget notifications"
                onClick={() => {
                  setNotifications(!notifications);
                  setProfileOpen(false);
                }}
              >
                <Bell size={19} />
              </button>
              {notifications && (
                <div className="popover notification-popover">
                  <strong>Budget check-in</strong>
                  {alerts.length ? (
                    alerts.map((b) => (
                      <button
                        key={b.id}
                        onClick={() => {
                          navigate("budgets");
                          setNotifications(false);
                        }}
                      >
                        {b.category?.name}
                        <small>
                          {Math.round((b.spent / b.limitAmount) * 100)}% of your
                          monthly limit used
                        </small>
                      </button>
                    ))
                  ) : (
                    <p>No budget alerts for this month.</p>
                  )}
                </div>
              )}
            </div>
            <span className="topbar-divider" />
            <div className="popover-anchor">
              <button
                className="profile-button"
                onClick={() => {
                  setProfileOpen(!profileOpen);
                  setNotifications(false);
                }}
                aria-label="Workspace account menu"
                aria-expanded={profileOpen}
              >
                <span className="avatar">
                  {data.user.name
                    .split(" ")
                    .map((s) => s[0])
                    .slice(0, 2)
                    .join("")}
                </span>
                <div>
                  <strong>{data.user.name.split(" ")[0]}</strong>
                  <small>
                    {mode === "sample"
                      ? "Sample workspace"
                      : "Personal account"}
                  </small>
                </div>
                <ChevronDown size={14} />
              </button>
              {profileOpen && (
                <div className="popover account-popover">
                  <strong>{data.user.name}</strong>
                  <small>{data.user.email}</small>
                  {mode === "sample" ? (
                    <>
                      <button
                        onClick={openAuth}
                        disabled={!status}
                      >
                        <LogIn size={16} />
                        {status?.available === false ? "Workspace connection" : "Sign in / register"}
                      </button>
                      <button
                        onClick={() =>
                          setModal({
                            kind: "confirm",
                            title: "Reset sample workspace?",
                            description:
                              "Replace this browser’s sample records with fresh fictional examples.",
                            label: "Reset sample records",
                            onConfirm: async () => {
                              commitSample(makeSample());
                              notify("Sample workspace reset.");
                            },
                          })
                        }
                      >
                        <RefreshCw size={16} />
                        Reset sample data
                      </button>
                    </>
                  ) : (
                    <button onClick={logout}>
                      <LogOut size={16} />
                      Sign out
                    </button>
                  )}
                </div>
              )}
            </div>
          </div>
        </header>
        <div
          className={`workspace-status ${mode === "sample" ? "sample-status" : ""}`}
        >
          <span>
            <Info size={13} />
            {mode === "sample"
              ? "Sample workspace · Fictional data, saved in this browser"
              : status?.storage === "H2 local demo"
                ? "Connected workspace · Local demo database"
                : "Connected workspace · MySQL storage"}
          </span>
          {mode === "sample" && (
            <button onClick={openAuth} disabled={!status}>
              {!status ? "Checking connection…" : status.available === false
                ? "About this sample preview" : "Sign in to your workspace"}
            </button>
          )}
        </div>
        <main>
          {route === "overview" && (
            <Overview
              {...shared}
              onCategoryFilter={(id) => {
                setCategoryFilter(id);
                navigate("transactions");
              }}
              onSeed={seed}
            />
          )}
          {route === "transactions" && (
            <Transactions
              {...shared}
              categoryFilter={categoryFilter}
              setCategoryFilter={setCategoryFilter}
              onDelete={(t) => confirmDelete("transactions", t)}
            />
          )}
          {route === "budgets" && (
            <Budgets
              {...shared}
              onDelete={(b) => confirmDelete("budgets", b)}
            />
          )}
          {route === "accounts" && (
            <Accounts
              data={data}
              onAccount={onAccount}
              onDelete={(a) => confirmDelete("accounts", a)}
            />
          )}
          {route === "insights" && (
            <Insights
              {...shared}
              status={status}
              insights={insights}
              loading={aiBusy}
              onGenerate={generate}
              onApply={(r) =>
                onBudget({
                  categoryId: r.categoryId,
                  month,
                  limitAmount: r.suggestedLimit,
                })
              }
            />
          )}
          {route === "reports" && <Reports {...shared} onDownload={download} />}
        </main>
      </div>
      {toast && (
        <div
          className={`toast ${toast.error ? "toast-error" : ""}`}
          role={toast.error ? "alert" : "status"}
        >
          {toast.error ? <Info size={19} /> : <CheckCircle2 size={19} />}
          <span>{toast.message}</span>
          <button aria-label="Dismiss message" onClick={() => setToast(null)}>
            <X size={16} />
          </button>
        </div>
      )}
      {modal?.kind === "transaction" && (
        <TransactionModal
          data={data}
          transaction={modal.transaction}
          type={modal.type}
          mode={mode}
          onClose={() => setModal(null)}
          onSave={(d) => save("transactions", d, modal.transaction?.id)}
          onCategory={(c) => save("categories", c)}
        />
      )}
      {modal?.kind === "account" && (
        <AccountModal
          account={modal.account}
          onClose={() => setModal(null)}
          onSave={(d) => save("accounts", d, modal.account?.id)}
        />
      )}
      {modal?.kind === "budget" && (
        <BudgetModal
          budget={modal.budget}
          data={data}
          month={month}
          onClose={() => setModal(null)}
          onSave={(d) => save("budgets", d)}
        />
      )}
      {modal?.kind === "auth" && (
        <AuthModal onClose={() => setModal(null)} onAuth={authenticate} />
      )}
      {modal?.kind === "connection" && (
        <Modal title="Your workspace connection" onClose={() => setModal(null)}>
          <p>This page is using fictional sample data saved in your browser because its workspace service is not connected. Registration, sign-in, and real AI require the connected Paisa app.</p>
          <p>On the computer running Paisa, <a href="http://127.0.0.1:5173/">open the local app</a> to create your workspace. The hosted sample preview does not include the workspace service.</p>
          <p>If the local app also cannot connect, start the Paisa backend and refresh the page.</p>
          <div className="modal-footer">
            <Button onClick={() => setModal(null)}>Continue exploring</Button>
          </div>
        </Modal>
      )}
      {modal?.kind === "confirm" && (
        <ConfirmModal {...modal} onClose={() => setModal(null)} />
      )}
    </div>
  );
}
export default class App extends React.Component {
  state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  render() {
    if (this.state.failed)
      return (
        <div className="fatal-error">
          <Wallet size={38} />
          <h1>Let’s get your workspace back.</h1>
          <p>
            Something interrupted this view. Reload to restore your saved
            workspace.
          </p>
          <Button onClick={() => window.location.reload()}>
            Reload workspace
          </Button>
        </div>
      );
    return <Application />;
  }
}
