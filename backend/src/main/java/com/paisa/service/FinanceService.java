package com.paisa.service;

import com.paisa.api.Requests.*;
import com.paisa.model.*;
import com.paisa.repository.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class FinanceService {
  final UserRepository users;
  final AccountRepository accounts;
  final CategoryRepository categories;
  final TransactionRepository transactions;
  final BudgetRepository budgets;
  private static final String[] COLORS = {
    "#1c8968", "#6d78cc", "#e6a455", "#d87e85", "#65a9bd", "#b28bc8", "#84998b", "#c0a06b"
  };

  public FinanceService(
      UserRepository u,
      AccountRepository a,
      CategoryRepository c,
      TransactionRepository t,
      BudgetRepository b) {
    users = u;
    accounts = a;
    categories = c;
    transactions = t;
    budgets = b;
  }

  public AppUser user(String email) {
    return users
        .findByEmail(email)
        .filter(AppUser::canAccessWorkspace)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in."));
  }

  private AppUser balanceUser(String email) {
    // Serialize balance-changing writes before reading records, including moves between accounts.
    return users
        .findForBalanceUpdate(email)
        .filter(AppUser::canAccessWorkspace)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in."));
  }

  private static BigDecimal effect(String type, BigDecimal amount) {
    return "INCOME".equals(type) ? amount : amount.negate();
  }

  private BigDecimal balance(Account account, List<Transaction> all) {
    return all.stream()
        .filter(t -> t.getAccount().getId().equals(account.getId()))
        .map(t -> effect(t.getType(), t.getAmount()))
        .reduce(account.getOpeningBalance(), BigDecimal::add);
  }

  private void requireFunds(Account account, BigDecimal projected) {
    if (projected.signum() < 0)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Insufficient funds in “"
              + account.getName()
              + "”. This change would leave a negative balance. Add the missing income or correct the opening balance first.");
  }

  public static YearMonth month(String value) {
    try {
      return YearMonth.parse(value);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid month (YYYY-MM).");
    }
  }

  public void defaultCategories(AppUser user) {
    String[] names = {
      "Food & dining",
      "Shopping",
      "Transport",
      "Bills & utilities",
      "Entertainment",
      "Health",
      "Other",
      "Salary",
      "Freelance"
    };
    for (int i = 0; i < names.length; i++) {
      Category c = new Category();
      c.setOwner(user);
      c.setName(names[i]);
      c.setType(i < 7 ? "EXPENSE" : "INCOME");
      c.setColor(COLORS[i % COLORS.length]);
      categories.save(c);
    }
  }

  private Account account(Long owner, Long id) {
    return accounts
        .findByIdAndOwnerId(id, owner)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found."));
  }

  private Category category(Long owner, Long id) {
    return categories
        .findByIdAndOwnerId(id, owner)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found."));
  }

  public Map<String, Object> accountView(Account a, List<Transaction> all) {
    BigDecimal balance = a.getOpeningBalance();
    for (Transaction t : all)
      if (t.getAccount().getId().equals(a.getId()))
        balance =
            balance.add(t.getType().equals("INCOME") ? t.getAmount() : t.getAmount().negate());
    return Map.of(
        "id",
        a.getId(),
        "name",
        a.getName(),
        "type",
        a.getType(),
        "openingBalance",
        a.getOpeningBalance(),
        "balance",
        balance);
  }

  public static Map<String, Object> categoryView(Category c) {
    return Map.of("id", c.getId(), "name", c.getName(), "type", c.getType(), "color", c.getColor());
  }

  public static Map<String, Object> transactionView(Transaction t) {
    return Map.of(
        "id",
        t.getId(),
        "accountId",
        t.getAccount().getId(),
        "categoryId",
        t.getCategory().getId(),
        "type",
        t.getType(),
        "description",
        t.getDescription(),
        "amount",
        t.getAmount(),
        "date",
        t.getDate().toString());
  }

  public static Map<String, Object> budgetView(Budget b) {
    return Map.of(
        "id",
        b.getId(),
        "categoryId",
        b.getCategory().getId(),
        "month",
        b.getMonth(),
        "limitAmount",
        b.getLimitAmount());
  }

  public Map<String, Object> snapshot(String email) {
    AppUser u = user(email);
    List<Transaction> ts = transactions.findByOwnerIdOrderByDateDescIdDesc(u.getId());
    return Map.of(
        "user",
        Map.of("id", u.getId(), "name", u.getName(), "email", u.getEmail()),
        "accounts",
        accounts.findByOwnerIdOrderByIdAsc(u.getId()).stream()
            .map(a -> accountView(a, ts))
            .toList(),
        "categories",
        categories.findByOwnerIdOrderByIdAsc(u.getId()).stream()
            .map(FinanceService::categoryView)
            .toList(),
        "transactions",
        ts.stream().map(FinanceService::transactionView).toList(),
        "budgets",
        budgets.findByOwnerIdOrderByIdAsc(u.getId()).stream()
            .map(FinanceService::budgetView)
            .toList());
  }

  public Map<String, Object> saveAccount(String email, Long id, AccountInput input) {
    AppUser u = balanceUser(email);
    Account a = id == null ? new Account() : account(u.getId(), id);
    List<Transaction> all = transactions.findByOwnerIdOrderByDateDescIdDesc(u.getId());
    BigDecimal projected =
        id == null
            ? input.openingBalance()
            : balance(a, all).subtract(a.getOpeningBalance()).add(input.openingBalance());
    requireFunds(a, projected);
    a.setOwner(u);
    a.setName(input.name().trim());
    a.setType(input.type());
    a.setOpeningBalance(input.openingBalance());
    accounts.save(a);
    return accountView(a, transactions.findByOwnerIdOrderByDateDescIdDesc(u.getId()));
  }

  public void deleteAccount(String email, Long id) {
    Account a = account(balanceUser(email).getId(), id);
    if (transactions.existsByAccountId(a.getId()))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "This account has transactions. Keep it to preserve your history.");
    accounts.delete(a);
  }

  public Map<String, Object> addCategory(String email, CategoryInput input) {
    AppUser u = user(email);
    Category c = new Category();
    c.setOwner(u);
    c.setName(input.name().trim());
    c.setType(input.type());
    c.setColor(COLORS[categories.findByOwnerIdOrderByIdAsc(u.getId()).size() % COLORS.length]);
    return categoryView(categories.save(c));
  }

  public Map<String, Object> saveTransaction(String email, Long id, TransactionInput input) {
    AppUser u = balanceUser(email);
    Transaction t =
        id == null
            ? new Transaction()
            : transactions
                .findByIdAndOwnerId(id, u.getId())
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Transaction not found."));
    Account a = account(u.getId(), input.accountId());
    Category c = category(u.getId(), input.categoryId());
    if (!c.getType().equals(input.type()))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "The category must match the transaction type.");
    List<Transaction> all = transactions.findByOwnerIdOrderByDateDescIdDesc(u.getId());
    BigDecimal destination = balance(a, all);
    if (id != null) {
      Account source = t.getAccount();
      BigDecimal reversed = balance(source, all).subtract(effect(t.getType(), t.getAmount()));
      if (source.getId().equals(a.getId())) destination = reversed;
      else requireFunds(source, reversed);
    }
    requireFunds(a, destination.add(effect(input.type(), input.amount())));
    t.setOwner(u);
    t.setAccount(a);
    t.setCategory(c);
    t.setType(input.type());
    t.setDescription(input.description().trim());
    t.setAmount(input.amount().setScale(2, RoundingMode.UNNECESSARY));
    t.setDate(input.date());
    return transactionView(transactions.save(t));
  }

  public void deleteTransaction(String email, Long id) {
    AppUser u = balanceUser(email);
    Transaction t =
        transactions
            .findByIdAndOwnerId(id, u.getId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));
    requireFunds(
        t.getAccount(),
        balance(t.getAccount(), transactions.findByOwnerIdOrderByDateDescIdDesc(u.getId()))
            .subtract(effect(t.getType(), t.getAmount())));
    transactions.delete(t);
  }

  public Map<String, Object> saveBudget(String email, BudgetInput input) {
    AppUser u = user(email);
    month(input.month());
    Category c = category(u.getId(), input.categoryId());
    if (!c.getType().equals("EXPENSE"))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Choose an expense category for a budget.");
    Budget b =
        budgets
            .findByOwnerIdAndCategoryIdAndMonth(u.getId(), c.getId(), input.month())
            .orElseGet(Budget::new);
    b.setOwner(u);
    b.setCategory(c);
    b.setMonth(input.month());
    b.setLimitAmount(input.limitAmount());
    return budgetView(budgets.save(b));
  }

  public void deleteBudget(String email, Long id) {
    Budget b =
        budgets
            .findByIdAndOwnerId(id, user(email).getId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found."));
    budgets.delete(b);
  }

  public Map<String, Object> analytics(String email, String value) {
    AppUser u = user(email);
    YearMonth m = month(value);
    List<Transaction> all = transactions.findByOwnerIdOrderByDateDescIdDesc(u.getId());
    List<Transaction> current =
        all.stream().filter(t -> YearMonth.from(t.getDate()).equals(m)).toList();
    List<Transaction> previous =
        all.stream().filter(t -> YearMonth.from(t.getDate()).equals(m.minusMonths(1))).toList();
    BigDecimal income = total(current, "INCOME"), expense = total(current, "EXPENSE");
    List<Map<String, Object>> cats = new ArrayList<>();
    for (Category c : categories.findByOwnerIdOrderByIdAsc(u.getId())) {
      if (!c.getType().equals("EXPENSE")) continue;
      BigDecimal spent =
          current.stream()
              .filter(t -> t.getCategory().getId().equals(c.getId()))
              .map(t -> t.getAmount())
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal before =
          previous.stream()
              .filter(t -> t.getCategory().getId().equals(c.getId()))
              .map(t -> t.getAmount())
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      cats.add(
          Map.of(
              "categoryId",
              c.getId(),
              "name",
              c.getName(),
              "spent",
              spent,
              "previousSpent",
              before,
              "suggestedLimit",
              spent.multiply(new BigDecimal("0.9")).setScale(0, RoundingMode.CEILING)));
    }
    return Map.of(
        "month",
        value,
        "income",
        income,
        "expenses",
        expense,
        "netCashFlow",
        income.subtract(expense),
        "transactionCount",
        current.size(),
        "previousMonthHasData",
        !previous.isEmpty(),
        "categories",
        cats,
        "budgets",
        budgets.findByOwnerIdOrderByIdAsc(u.getId()).stream()
            .filter(b -> b.getMonth().equals(value))
            .map(FinanceService::budgetView)
            .toList());
  }

  static BigDecimal total(List<Transaction> ts, String type) {
    return ts.stream()
        .filter(t -> t.getType().equals(type))
        .map(t -> t.getAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public String report(String email, String value) {
    month(value);
    Map<String, Object> a = analytics(email, value);
    StringBuilder csv =
        new StringBuilder("Date,Description,Type,Category,Account,Amount (INR)\r\n");
    for (Transaction t : transactions.findByOwnerIdOrderByDateDescIdDesc(user(email).getId())) {
      if (!YearMonth.from(t.getDate()).toString().equals(value)) continue;
      csv.append(t.getDate())
          .append(',')
          .append(csvCell(t.getDescription()))
          .append(',')
          .append(t.getType())
          .append(',')
          .append(csvCell(t.getCategory().getName()))
          .append(',')
          .append(csvCell(t.getAccount().getName()))
          .append(',')
          .append(t.getAmount().toPlainString())
          .append("\r\n");
    }
    csv.append("\r\nSummary,Amount (INR)\r\nIncome,")
        .append(a.get("income"))
        .append("\r\nExpenses,")
        .append(a.get("expenses"))
        .append("\r\nNet cash flow,")
        .append(a.get("netCashFlow"))
        .append("\r\n");
    return csv.toString();
  }

  public static String csvCell(String value) {
    String s = value == null ? "" : value;
    if (s.stripLeading().matches("^[=+@\\-\\t\\r].*")) s = "'" + s;
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }

  public void seedDemo(String email) {
    AppUser u = balanceUser(email);
    if (accounts.findByOwnerIdOrderByIdAsc(u.getId()).stream()
        .anyMatch(account -> account.getName().equals("Demo · Everyday account")
            || account.getName().equals("Demo · Cash wallet")))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Demo records are already loaded. Open AI insights to analyze them.");
    Account a = new Account();
    a.setOwner(u);
    a.setName("Demo · Everyday account");
    a.setType("BANK");
    a.setOpeningBalance(new BigDecimal("12000"));
    accounts.save(a);
    Account cash = new Account();
    cash.setOwner(u);
    cash.setName("Demo · Cash wallet");
    cash.setType("CASH");
    cash.setOpeningBalance(new BigDecimal("12000"));
    accounts.save(cash);
    List<Category> existingCategories = categories.findByOwnerIdOrderByIdAsc(u.getId());
    List<Category> cs = new ArrayList<>();
    String[] demoCategories = {"Food & dining", "Shopping", "Transport", "Bills & utilities",
        "Entertainment", "Health", "Other", "Salary", "Freelance"};
    for (int i = 0; i < demoCategories.length; i++) {
      String name = demoCategories[i];
      String type = i < 7 ? "EXPENSE" : "INCOME";
      Category c = existingCategories.stream()
          .filter(category -> name.equals(category.getName()) && type.equals(category.getType()))
          .findFirst().orElse(null);
      if (c == null) {
        c = new Category();
        c.setOwner(u);
        c.setName(name);
        c.setType(type);
        c.setColor(COLORS[i % COLORS.length]);
        categories.save(c);
      }
      cs.add(c);
    }
    YearMonth now = YearMonth.now();
    String[] labels = {
      "Monthly salary",
      "Weekend groceries",
      "Cafe with friends",
      "Metro pass",
      "Internet & electricity",
      "New sneakers",
      "Cinema night",
      "Freelance project",
      "Fresh produce",
      "Dinner with friends",
      "Pharmacy",
      "Online subscription",
      "Coffee & breakfast",
      "Grocery essentials"
    };
    String[] amounts = {
      "65000", "1840", "680", "1200", "3200", "2499", "850", "8500", "920", "1480", "650", "499",
      "320", "1260"
    };
    int[] ci = {7, 0, 0, 2, 3, 1, 4, 8, 0, 0, 5, 4, 0, 0};
    for (int p = 0; p < 2; p++) {
      for (int i = 0; i < labels.length; i++) {
        Category c = cs.get(ci[i]);
        Transaction t = new Transaction();
        t.setOwner(u);
        t.setAccount(i % 5 == 0 && i > 0 ? cash : a);
        t.setCategory(c);
        t.setType(c.getType());
        t.setDescription("Demo · " + labels[i]);
        t.setAmount(
            new BigDecimal(amounts[i])
                .multiply(p == 1 ? new BigDecimal("1.12") : BigDecimal.ONE)
                .setScale(2, RoundingMode.HALF_UP));
        t.setDate(
            now.minusMonths(p).atDay(Math.min(2 + i * 2, now.minusMonths(p).lengthOfMonth())));
        transactions.save(t);
      }
    }
    for (int i = 0; i < 5; i++) {
      if (budgets.findByOwnerIdAndCategoryIdAndMonth(u.getId(), cs.get(i).getId(), now.toString()).isPresent()) continue;
      Budget b = new Budget();
      b.setOwner(u);
      b.setCategory(cs.get(i));
      b.setMonth(now.toString());
      b.setLimitAmount(new BigDecimal(new int[] {8000, 5000, 3000, 4500, 2500}[i]));
      budgets.save(b);
    }
  }
}
