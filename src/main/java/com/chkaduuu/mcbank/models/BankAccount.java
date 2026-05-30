package com.chkaduuu.mcbank.models;

import java.util.ArrayList;
import java.util.List;

public class BankAccount
{
    private final String accountNumber;
    private double balance;
    private double loan;
    private boolean active;
    private final long created;

    // История транзакций (последние 10)
    private final List<String> transactionHistory;

    // Штраф за просроченный кредит
    private long loanTakenAt;
    private long loanDueDays;

    public BankAccount(final String accountNumber) {
        this.accountNumber = accountNumber;
        this.balance = 0.0;
        this.loan = 0.0;
        this.active = true;
        this.created = System.currentTimeMillis();
        this.transactionHistory = new ArrayList<>();
        this.loanTakenAt = 0L;
        this.loanDueDays = 0L;
    }

    public BankAccount(final String accountNumber, final double balance, final double loan,
                       final boolean active, final long created) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.loan = loan;
        this.active = active;
        this.created = created;
        this.transactionHistory = new ArrayList<>();
        this.loanTakenAt = 0L;
        this.loanDueDays = 0L;
    }

    public String getAccountNumber() { return this.accountNumber; }
    public double getBalance() { return this.balance; }
    public void setBalance(final double balance) { this.balance = balance; }
    public double getLoan() { return this.loan; }
    public void setLoan(final double loan) { this.loan = loan; }
    public boolean isActive() { return this.active; }
    public void setActive(final boolean active) { this.active = active; }
    public long getCreated() { return this.created; }

    public void deposit(final double amount) { this.balance += amount; }
    public void withdraw(final double amount) { this.balance -= amount; }

    // --- История транзакций ---
    public List<String> getTransactionHistory() { return this.transactionHistory; }

    public void addTransaction(final String entry) {
        this.transactionHistory.add(0, entry); // новые сверху
        if (this.transactionHistory.size() > 10) {
            this.transactionHistory.remove(this.transactionHistory.size() - 1);
        }
    }

    // --- Кредит с дедлайном ---
    public long getLoanTakenAt() { return this.loanTakenAt; }
    public void setLoanTakenAt(final long loanTakenAt) { this.loanTakenAt = loanTakenAt; }

    public long getLoanDueDays() { return this.loanDueDays; }
    public void setLoanDueDays(final long loanDueDays) { this.loanDueDays = loanDueDays; }

    /** Возвращает true если кредит просрочен */
    public boolean isLoanOverdue() {
        if (this.loan <= 0 || this.loanTakenAt == 0 || this.loanDueDays == 0) return false;
        long dueDateMs = this.loanTakenAt + this.loanDueDays * 24L * 60L * 60L * 1000L;
        return System.currentTimeMillis() > dueDateMs;
    }
}
