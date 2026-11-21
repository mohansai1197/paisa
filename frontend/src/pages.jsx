import React, { useState } from "react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from "recharts";
import {
  Plus,
  Download,
  Search,
  SlidersHorizontal,
  Sparkles,
  ArrowDownLeft,
  ArrowUpRight,
  Wallet,
  TrendingUp,
  Target,
  CalendarDays,
  BarChart3,
  FileText,
  Building2,
  Smartphone,
  Pencil,
  Trash2,
  Check,
  Info,
  LoaderCircle,
  ReceiptText,
  ShieldCheck,
  Lightbulb,
  RefreshCw,
} from "lucide-react";
import {
  Button,
  Empty,
  PageHeader,
  SectionTitle,
  CategoryIcon,
  BudgetRow,
  Progress,
  TransactionTable,
} from "./components";
import {
  money,
  shortMoney,
  summarize,
  previousMonth,
  monthLabel,
  accountBalance,
} from "./finance";

export function Overview({
  data,
  month,
  onAdd,
  onBudget,
  onNavigate,
  onEdit,
  onCategoryFilter,
  onSeed,
  busy,
  mode,
}) {
  const stats = summarize(data, month),
    prev = summarize(data, previousMonth(month));
  let inc = 0,
    exp = 0;
  const chart = stats.flow.map((d) => ({
    ...d,
    income: (inc += d.income),
    expenses: (exp += d.expenses),
  }));
  const slices = stats.categories.filter((c) => c.spent > 0),
    latest = [...stats.rows]
      .sort((a, b) => b.date.localeCompare(a.date) || b.id - a.id)
      .slice(0, 5);
  const top = slices[0];
  function delta(a, b) {
    return b > 0
      ? `${a >= b ? "+" : ""}${(((a - b) / b) * 100).toFixed(1)}% vs last month`
      : "No prior-month comparison";
  }
  return (
    <>
      <PageHeader
        eyebrow={`A LITTLE CLARITY, EVERY DAY`}
        title={`Your money, in perspective.`}
        description={`Here’s your financial picture for ${monthLabel(month)}.`}
        actions={
          <>
          {mode === "connected" && (
            <Button variant="secondary" onClick={onSeed} disabled={busy}>
              {busy ? "Loading demo data…" : "Load sample records"}
            </Button>
          )}
          <Button onClick={() => onAdd()}>
            <Plus size={17} />
            Add transaction
          </Button>
          </>
        }
      />
      {!data.accounts.length && (
        <div className="welcome-strip">
          <div>
            <strong>Your fresh start is ready.</strong>
            <p>
              Add an account to begin, or explore with fictional sample records.
            </p>
          </div>
          <Button variant="secondary" onClick={() => onNavigate("accounts")}>
            Add an account
          </Button>
        </div>
      )}
      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-top">
            <span>Total income</span>
            <span className="stat-icon income">
              <ArrowDownLeft size={19} />
            </span>
          </div>
          <div className="stat-value">{money(stats.income)}</div>
          <div className="stat-footer">
            <span
              className={stats.income >= prev.income ? "positive" : "muted"}
            >
              {delta(stats.income, prev.income)}
            </span>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-top">
            <span>Total expenses</span>
            <span className="stat-icon expense">
              <ArrowUpRight size={19} />
            </span>
          </div>
          <div className="stat-value">{money(stats.expenses)}</div>
          <div className="stat-footer">
            <span
              className={stats.expenses <= prev.expenses ? "positive" : "muted"}
            >
              {delta(stats.expenses, prev.expenses)}
            </span>
          </div>
        </div>
        <div className="stat-card featured-stat">
          <div className="stat-top">
            <span>Net cash flow</span>
            <span className="stat-icon net">
              <Wallet size={19} />
            </span>
          </div>
          <div className="stat-value">{money(stats.net)}</div>
          <div className="stat-footer">
            <span>Income minus expenses this month</span>
          </div>
          <div className="net-detail">
            <span className="tiny-line" />
            {stats.income > 0
              ? `${Math.round((stats.net / stats.income) * 100)}% of income remaining`
              : "Add income to see your monthly picture"}
          </div>
        </div>
      </div>
      <div className="chart-grid">
        <section className="panel flow-panel">
          <SectionTitle
            title="Money flow"
            subtitle="Cumulative income and expenses"
            action={<span className="subtle-tag">{monthLabel(month)}</span>}
          />
          <div className="chart-legend">
            <span>
              <i style={{ background: "#288a6b" }} />
              Income
            </span>
            <span>
              <i style={{ background: "#a0a8d4" }} />
              Expenses
            </span>
          </div>
          {stats.rows.length ? (
            <div className="flow-chart">
              <ResponsiveContainer width="100%" height={238}>
                <AreaChart
                  data={chart}
                  margin={{ top: 15, right: 10, left: -20, bottom: 0 }}
                >
                  <defs>
                    <linearGradient id="incomeFill" x1="0" y1="0" x2="0" y2="1">
                      <stop
                        offset="0%"
                        stopColor="#288a6b"
                        stopOpacity={0.18}
                      />
                      <stop offset="100%" stopColor="#288a6b" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient
                      id="expenseFill"
                      x1="0"
                      y1="0"
                      x2="0"
                      y2="1"
                    >
                      <stop
                        offset="0%"
                        stopColor="#a0a8d4"
                        stopOpacity={0.12}
                      />
                      <stop offset="100%" stopColor="#a0a8d4" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid vertical={false} stroke="#edf1f1" />
                  <XAxis
                    dataKey="day"
                    axisLine={false}
                    tickLine={false}
                    interval={4}
                    tick={{ fill: "#8a9896", fontSize: 11 }}
                    dy={9}
                  />
                  <YAxis
                    tickFormatter={shortMoney}
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: "#8a9896", fontSize: 11 }}
                  />
                  <Tooltip
                    formatter={(v, name) => [
                      money(v),
                      name === "income" ? "Income" : "Expenses",
                    ]}
                    labelFormatter={(v) => `${monthLabel(month)} · day ${v}`}
                    contentStyle={{
                      border: "1px solid #e4eae7",
                      borderRadius: 12,
                      fontSize: 12,
                    }}
                  />
                  <Area
                    type="monotone"
                    dataKey="income"
                    stroke="#288a6b"
                    strokeWidth={2.5}
                    fill="url(#incomeFill)"
                  />
                  <Area
                    type="monotone"
                    dataKey="expenses"
                    stroke="#a0a8d4"
                    strokeWidth={2.5}
                    fill="url(#expenseFill)"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <Empty
              icon={BarChart3}
              title="Your story starts with one entry"
              description="Add income or an expense to see your money flow."
              action={
                <Button variant="secondary" onClick={() => onAdd()}>
                  Add transaction
                </Button>
              }
            />
          )}
        </section>
        <section className="panel spending-panel">
          <SectionTitle
            title="Where it went"
            subtitle="Expenses by category"
            action={
              <button
                className="icon-button"
                aria-label="View spending transactions"
                onClick={() => onNavigate("transactions")}
              >
                <SlidersHorizontal size={17} />
              </button>
            }
          />
          {slices.length ? (
            <>
              <div className="donut-wrap">
                <ResponsiveContainer width="100%" height={186}>
                  <PieChart>
                    <Pie
                      data={slices}
                      dataKey="spent"
                      nameKey="name"
                      cx="50%"
                      cy="50%"
                      innerRadius={61}
                      outerRadius={79}
                      paddingAngle={4}
                      stroke="none"
                      onClick={(_, i) => onCategoryFilter(slices[i].id)}
                    >
                      {slices.map((c) => (
                        <Cell key={c.id} fill={c.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(v) => money(v)}
                      contentStyle={{ borderRadius: 12, fontSize: 12 }}
                    />
                  </PieChart>
                </ResponsiveContainer>
                <div className="donut-center">
                  <small>Total spent</small>
                  <strong>{money(stats.expenses)}</strong>
                </div>
              </div>
              <div className="category-breakdown">
                {slices.slice(0, 4).map((c) => (
                  <button key={c.id} onClick={() => onCategoryFilter(c.id)}>
                    <span>
                      <i style={{ background: c.color }} />
                      {c.name}
                    </span>
                    <strong>
                      {money(c.spent)}
                      <small>
                        {Math.round((c.spent / stats.expenses) * 100)}%
                      </small>
                    </strong>
                  </button>
                ))}
              </div>
            </>
          ) : (
            <Empty
              icon={ReceiptText}
              title="Nothing spent yet"
              description="Your expense breakdown will appear here."
            />
          )}
        </section>
      </div>
      <div className="insight-banner">
        <div className="insight-banner-icon">
          <Sparkles size={23} />
        </div>
        <div>
          <div className="banner-eyebrow">A FRESH PERSPECTIVE</div>
          <h3>A little insight. A better next step.</h3>
          <p>
            {top
              ? `${top.name} makes up ${Math.round((top.spent / stats.expenses) * 100)}% of this month’s spending. Explore the bigger picture.`
              : "Turn your transaction history into an understandable monthly review."}
          </p>
        </div>
        <Button variant="light" onClick={() => onNavigate("insights")}>
          Explore insights
        </Button>
      </div>
      <div className="bottom-grid">
        <section className="panel recent-panel">
          <SectionTitle
            title="Recent transactions"
            subtitle="The little things that add up"
            action={
              <button
                className="text-button"
                onClick={() => onNavigate("transactions")}
              >
                View all
              </button>
            }
          />
          {latest.length ? (
            <TransactionTable
              compact
              rows={latest}
              data={data}
              onEdit={onEdit}
            />
          ) : (
            <Empty
              icon={ReceiptText}
              title="No transactions this month"
              description="Your latest income and expenses will appear here."
            />
          )}
        </section>
        <section className="panel budgets-preview">
          <SectionTitle
            title="Monthly budgets"
            subtitle="A plan for what matters"
            action={
              <button
                className="icon-button"
                aria-label="Create budget"
                onClick={() => onBudget()}
              >
                <Plus size={18} />
              </button>
            }
          />
          {stats.budgets.length ? (
            <>
              {stats.budgets.slice(0, 3).map((b) => (
                <BudgetRow key={b.id} budget={b} onEdit={onBudget} />
              ))}
              <button
                className="text-button budget-all"
                onClick={() => onNavigate("budgets")}
              >
                Manage all budgets
              </button>
            </>
          ) : (
            <Empty
              icon={Target}
              title="Make room for your priorities"
              description="Set a category budget to start tracking progress."
              action={
                <Button variant="secondary" onClick={() => onBudget()}>
                  Create budget
                </Button>
              }
            />
          )}
        </section>
      </div>
      <div className="page-footnote">
        <ShieldCheck size={14} />
        {mode === "sample"
          ? "Fictional sample records · Changes stay in this browser"
          : "Your workspace is private · Only your records are shown"}
        <span>Made for a little more peace of mind.</span>
      </div>
    </>
  );
}

export function Transactions({
  data,
  month,
  onAdd,
  onEdit,
  onDelete,
  categoryFilter,
  setCategoryFilter,
}) {
  const [search, setSearch] = useState(""),
    [type, setType] = useState("ALL"),
    [account, setAccount] = useState("ALL");
  const rows = data.transactions
    .filter((t) => t.date.startsWith(month))
    .filter(
      (t) =>
        (type === "ALL" || t.type === type) &&
        (account === "ALL" || t.accountId === Number(account)) &&
        (!categoryFilter || t.categoryId === Number(categoryFilter)) &&
        t.description.toLowerCase().includes(search.toLowerCase()),
    )
    .sort((a, b) => b.date.localeCompare(a.date) || b.id - a.id);
  return (
    <>
      <PageHeader
        title="Every transaction, in one place."
        description="Keep the details. Understand the bigger picture."
        eyebrow="YOUR MONEY JOURNAL"
        actions={
          <Button onClick={() => onAdd()}>
            <Plus size={17} />
            Add transaction
          </Button>
        }
      />
      <section className="panel">
        <div className="transaction-filters">
          <div className="search-field">
            <Search size={17} />
            <input
              aria-label="Search transactions"
              placeholder="Search transactions…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <select
            aria-label="Filter transaction type"
            value={type}
            onChange={(e) => setType(e.target.value)}
          >
            <option value="ALL">All transactions</option>
            <option value="EXPENSE">Expenses</option>
            <option value="INCOME">Income</option>
          </select>
          <select
            aria-label="Filter category"
            value={categoryFilter || ""}
            onChange={(e) =>
              setCategoryFilter(e.target.value ? Number(e.target.value) : null)
            }
          >
            <option value="">All categories</option>
            {data.categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <select
            aria-label="Filter account"
            value={account}
            onChange={(e) => setAccount(e.target.value)}
          >
            <option value="ALL">All accounts</option>
            {data.accounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.name}
              </option>
            ))}
          </select>
        </div>
        {rows.length ? (
          <TransactionTable
            rows={rows}
            data={data}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ) : (
          <Empty
            icon={Search}
            title="No matching transactions"
            description="Try another filter, choose a different month, or add an entry."
            action={
              <Button
                variant="secondary"
                onClick={() => {
                  setSearch("");
                  setType("ALL");
                  setAccount("ALL");
                  setCategoryFilter(null);
                }}
              >
                Clear filters
              </Button>
            }
          />
        )}
        <div className="table-footer">
          <span>
            {rows.length} transaction{rows.length === 1 ? "" : "s"} ·{" "}
            {monthLabel(month)}
          </span>
          <span>Amounts in INR</span>
        </div>
      </section>
    </>
  );
}

export function Budgets({ data, month, onBudget, onDelete }) {
  const s = summarize(data, month);
  const assignedSpent = s.budgets.reduce((v, b) => v + b.spent, 0);
  return (
    <>
      <PageHeader
        eyebrow="SPEND WITH INTENTION"
        title="A plan for your priorities."
        description="Give every category some breathing room."
        actions={
          <Button onClick={() => onBudget()}>
            <Plus size={17} />
            Create budget
          </Button>
        }
      />
      <div className="budget-summary panel">
        <div>
          <span className="muted">Total category budgets</span>
          <strong>{money(s.totalBudget)}</strong>
        </div>
        <div>
          <span className="muted">Spent in budgeted categories</span>
          <strong>{money(assignedSpent)}</strong>
        </div>
        <div>
          <span className="muted">Remaining in those budgets</span>
          <strong
            className={
              s.totalBudget - assignedSpent >= 0 ? "positive" : "danger-text"
            }
          >
            {money(s.totalBudget - assignedSpent)}
          </strong>
        </div>
      </div>
      {s.budgets.length ? (
        <div className="budget-grid">
          {s.budgets.map((b) => {
            const pct = Math.round((b.spent / b.limitAmount) * 100);
            return (
              <section className="panel budget-card" key={b.id}>
                <div className="budget-card-top">
                  <CategoryIcon category={b.category} size={22} />
                  <div className="row-actions">
                    <button
                      className="icon-button"
                      aria-label={`Edit ${b.category?.name} budget`}
                      onClick={() => onBudget(b)}
                    >
                      <Pencil size={16} />
                    </button>
                    <button
                      className="icon-button"
                      aria-label={`Delete ${b.category?.name} budget`}
                      onClick={() => onDelete(b)}
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </div>
                <h2>{b.category?.name}</h2>
                <div className="budget-spent">
                  <strong>{money(b.spent)}</strong>
                  <span>of {money(b.limitAmount)}</span>
                </div>
                <Progress spent={b.spent} limit={b.limitAmount} />
                <div className="budget-card-bottom">
                  <span
                    className={
                      pct >= 100
                        ? "danger-text"
                        : pct >= 85
                          ? "warning-text"
                          : "positive"
                    }
                  >
                    {pct >= 100
                      ? "Over budget"
                      : pct >= 85
                        ? "Approaching limit"
                        : "Within budget"}
                  </span>
                  <span>{pct}% spent</span>
                </div>
              </section>
            );
          })}
          <button className="add-budget-card" onClick={() => onBudget()}>
            <span>
              <Plus size={24} />
            </span>
            <strong>A new priority?</strong>
            <small>Create another category budget</small>
          </button>
        </div>
      ) : (
        <section className="panel">
          <Empty
            icon={Target}
            title="Your spending, with a little direction"
            description="Create your first monthly budget. You choose the category and the limit."
            action={
              <Button onClick={() => onBudget()}>
                Create your first budget
              </Button>
            }
          />
        </section>
      )}
      <div className="info-note">
        <Info size={17} />
        Category budgets are spending limits you set, not a guarantee that the
        amounts are affordable.
      </div>
    </>
  );
}

export function Accounts({ data, onAccount, onDelete }) {
  const total = data.accounts.reduce(
    (s, a) => s + accountBalance(data, a.id),
    0,
  );
  return (
    <>
      <PageHeader
        title="All your money. One clear view."
        eyebrow="YOUR ACCOUNTS"
        description="Bank, wallet, or a little cash on hand—keep it together."
        actions={
          <Button onClick={() => onAccount()}>
            <Plus size={17} />
            Add account
          </Button>
        }
      />
      <section className="accounts-total">
        <span>Combined recorded balance</span>
        <strong>{money(total, 2)}</strong>
        <small>Opening balances plus all recorded transactions</small>
      </section>
      <div className="account-grid">
        {data.accounts.map((a, i) => {
          const Icon =
            a.type === "BANK"
              ? Building2
              : a.type === "WALLET"
                ? Smartphone
                : Wallet;
          return (
            <section
              className={`account-card ${i === 0 ? "main-account" : ""}`}
              key={a.id}
            >
              <div className="account-top">
                <span className="account-symbol">
                  <Icon size={23} />
                </span>
                <span className="account-type">
                  {a.type === "BANK"
                    ? "BANK ACCOUNT"
                    : a.type === "WALLET"
                      ? "DIGITAL WALLET"
                      : "CASH"}
                </span>
              </div>
              <h2>{a.name}</h2>
              <div className="account-amount">
                {money(accountBalance(data, a.id), 2)}
              </div>
              {accountBalance(data, a.id) < 0 && (
                <p className="form-note danger-text" role="alert">
                  Balance needs attention. Record the missing income or edit this account to correct its opening balance.
                </p>
              )}
              <div className="account-footer">
                <span>INR · Recorded balance</span>
                <button
                  className="icon-button"
                  aria-label={`Edit ${a.name}`}
                  onClick={() => onAccount(a)}
                >
                  <Pencil size={17} />
                </button>
                <button
                  className="icon-button"
                  aria-label={`Delete ${a.name}`}
                  onClick={() => onDelete(a)}
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </section>
          );
        })}
        <button className="add-account-card" onClick={() => onAccount()}>
          <Plus size={25} />
          <strong>Add another account</strong>
          <span>Keep your financial picture complete</span>
        </button>
      </div>
      <div className="info-note">
        <ShieldCheck size={17} />
        Accounts track entries you record here. No bank connection or money
        movement is involved.
      </div>
    </>
  );
}

export function Insights({
  data,
  month,
  insights,
  loading,
  onGenerate,
  onAdd,
  onApply,
  mode,
  status,
}) {
  const s = summarize(data, month);
  return (
    <>
      <PageHeader
        eyebrow="A CLEARER PERSPECTIVE"
        title="Understand the why behind your spending."
        description="Four ways to turn everyday records into useful insights."
        actions={
          <Button onClick={onGenerate} disabled={loading}>
            {loading ? (
              <LoaderCircle className="spin" size={17} />
            ) : (
              <Sparkles size={17} />
            )}{" "}
            {loading ? "Analyzing your month…" : "Generate monthly insights"}
          </Button>
        }
      />
      <div
        className={`ai-connection ${status?.aiConfigured && mode !== "sample" ? "connected" : ""}`}
      >
        <Sparkles size={18} />
        <div>
          <strong>
            {mode === "sample"
              ? "You’re exploring a sample workspace"
              : status?.aiConfigured
                ? "AI is connected to your workspace"
                : "AI isn’t connected to this workspace yet"}
          </strong>
          <p>
            {mode === "sample"
              ? "Sample records are fictional. Sign in to a connected workspace to generate real AI insights."
              : status?.aiConfigured
                ? "Insights use your selected month’s records. Always review suggestions before applying them."
                : "You can still track spending and manage budgets. AI results will appear when a provider is connected."}
          </p>
        </div>
      </div>
      <div className="ai-feature-grid">
        <section className="panel ai-feature category-feature">
          <div className="feature-label">
            <span className="feature-icon">
              <ReceiptText size={22} />
            </span>
            <span className="subtle-tag">At transaction entry</span>
          </div>
          <h2>Expense categorization</h2>
          <p>
            Describe an expense. Get a category suggestion you can accept or
            change.
          </p>
          <div className="category-example">
            <span>“Coffee with friends”</span>
            <span className="category-label">Review an AI suggestion</span>
          </div>
          <Button variant="secondary" onClick={() => onAdd("EXPENSE")}>
            <Plus size={16} />
            Try with an expense
          </Button>
        </section>
        <section className="panel ai-feature">
          <div className="feature-label">
            <span className="feature-icon lavender">
              <BarChart3 size={22} />
            </span>
            <span className="subtle-tag">Spending patterns</span>
          </div>
          <h2>See the patterns</h2>
          {insights?.patterns?.length ? (
            <div className="pattern-list">
              {insights.patterns.map((p, i) => (
                <div key={i}>
                  <strong>{p.title}</strong>
                  <p>{p.detail}</p>
                </div>
              ))}
            </div>
          ) : (
            <>
              <p>
                Understand your biggest categories and changes that your records
                support.
              </p>
              <div className="ai-empty">
                <BarChart3 size={23} />
                <span>Your pattern analysis will appear here.</span>
              </div>
            </>
          )}
        </section>
        <section className="panel ai-feature">
          <div className="feature-label">
            <span className="feature-icon amber">
              <Target size={22} />
            </span>
            <span className="subtle-tag">Budget recommendations</span>
          </div>
          <h2>Give your budget direction</h2>
          {insights?.recommendations?.length ? (
            <div className="recommendations">
              {insights.recommendations.map((r) => (
                <div key={r.categoryId}>
                  <div>
                    <strong>{r.category}</strong>
                    <span>{money(r.suggestedLimit)} / month</span>
                  </div>
                  <p>{r.reason}</p>
                  <Button variant="secondary" onClick={() => onApply(r)}>
                    Review budget
                  </Button>
                </div>
              ))}
            </div>
          ) : (
            <>
              <p>
                Explore category limits based on your spending. You decide what
                to apply.
              </p>
              <div className="ai-empty">
                <Target size={23} />
                <span>Your budget suggestions will appear here.</span>
              </div>
            </>
          )}
        </section>
        <section className="panel ai-feature">
          <div className="feature-label">
            <span className="feature-icon rose">
              <FileText size={22} />
            </span>
            <span className="subtle-tag">Monthly summary</span>
          </div>
          <h2>Your month, put into words</h2>
          {insights?.summary ? (
            <p className="generated-summary">{insights.summary}</p>
          ) : (
            <>
              <p>
                A clear narrative of your income, expenses, and net cash
                flow—grounded in your records.
              </p>
              <div className="ai-empty">
                <FileText size={23} />
                <span>Your monthly review will appear here.</span>
              </div>
            </>
          )}
        </section>
      </div>
      {insights && (
        <div className="info-note">
          <Info size={17} />
          {insights.note} Generated by {insights.source} for {monthLabel(month)}
          .
        </div>
      )}
      <section className="panel calculated-panel">
        <SectionTitle
          title="The facts behind the insights"
          subtitle="Calculated directly from your records · not generated by AI"
        />
        <div className="facts-grid">
          <div>
            <span>Income</span>
            <strong>{money(s.income)}</strong>
          </div>
          <div>
            <span>Expenses</span>
            <strong>{money(s.expenses)}</strong>
          </div>
          <div>
            <span>Net cash flow</span>
            <strong>{money(s.net)}</strong>
          </div>
          <div>
            <span>Transactions</span>
            <strong>{s.rows.length}</strong>
          </div>
        </div>
      </section>
    </>
  );
}

export function Reports({ data, month, onDownload, onEdit }) {
  const s = summarize(data, month);
  return (
    <>
      <PageHeader
        eyebrow="YOUR MONTH, ON RECORD"
        title="Take the bigger picture with you."
        description="A clear, downloadable record of your monthly finances."
        actions={
          <Button onClick={onDownload}>
            <Download size={17} />
            Download CSV report
          </Button>
        }
      />
      <section className="panel report-cover">
        <div className="report-heading">
          <div className="report-icon">
            <FileText size={29} />
          </div>
          <div>
            <span className="eyebrow">PERSONAL FINANCE REPORT</span>
            <h2>{monthLabel(month)}</h2>
            <p>
              {data.user.name} · {s.rows.length} recorded transactions · INR
            </p>
          </div>
          <span className="subtle-tag">CSV export</span>
        </div>
        <div className="facts-grid">
          <div>
            <span>Total income</span>
            <strong>{money(s.income)}</strong>
          </div>
          <div>
            <span>Total expenses</span>
            <strong>{money(s.expenses)}</strong>
          </div>
          <div>
            <span>Net cash flow</span>
            <strong>{money(s.net)}</strong>
          </div>
        </div>
        <div className="report-includes">
          <Check size={16} />
          Transaction details <Check size={16} />
          Category and account <Check size={16} />
          Monthly totals
        </div>
      </section>
      <section className="panel">
        <SectionTitle
          title="Report preview"
          subtitle="The entries included in this month’s download"
        />
        {s.rows.length ? (
          <TransactionTable
            rows={[...s.rows].sort((a, b) => b.date.localeCompare(a.date))}
            compact
            data={data}
            onEdit={onEdit}
          />
        ) : (
          <Empty
            icon={FileText}
            title="No records for this month"
            description="Add a transaction or choose a different month."
          />
        )}
      </section>
      <div className="info-note">
        <Info size={17} />
        CSV opens in Excel, Google Sheets, and other spreadsheet tools. Reports
        contain recorded data, not a bank statement.
      </div>
    </>
  );
}
