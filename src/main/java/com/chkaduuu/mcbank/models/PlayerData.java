package com.chkaduuu.mcbank.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerData
{
    private String name;
    private int level;
    private String pin;
    private final Map<String, BankAccount> accounts;
    private int bankLevel;

    // Оффлайн-уведомления о переводах
    private final List<String> pendingNotifications;

    // ATM лимит операций в день
    private int atmOperationsToday;
    private long atmLastResetDay; // день (System.currentTimeMillis() / 86400000)

    // Блокировка PIN после 3 неверных попыток
    private int pinFailedAttempts;
    private boolean pinLocked;

    public PlayerData(final String name) {
        this.name = name;
        this.level = 0;
        this.pin = null;
        this.accounts = new LinkedHashMap<>();
        this.bankLevel = 1;
        this.pendingNotifications = new ArrayList<>();
        this.atmOperationsToday = 0;
        this.atmLastResetDay = today();
        this.pinFailedAttempts = 0;
        this.pinLocked = false;
    }

    private long today() {
        return System.currentTimeMillis() / 86400000L;
    }

    // --- Базовые геттеры/сеттеры ---
    public String getName() { return this.name; }
    public void setName(final String name) { this.name = name; }
    public int getLevel() { return this.level; }
    public void setLevel(final int level) { this.level = level; }
    public String getPin() { return this.pin; }
    public void setPin(final String pin) { this.pin = pin; }
    public Map<String, BankAccount> getAccounts() { return this.accounts; }
    public int getBankLevel() { return this.bankLevel; }
    public void setBankLevel(final int bankLevel) { this.bankLevel = bankLevel; }

    public BankAccount getPrimaryAccount() {
        if (this.accounts.isEmpty()) return null;
        return this.accounts.values().iterator().next();
    }

    // --- Оффлайн уведомления ---
    public List<String> getPendingNotifications() { return this.pendingNotifications; }

    public void addPendingNotification(final String msg) {
        this.pendingNotifications.add(msg);
    }

    public void clearPendingNotifications() {
        this.pendingNotifications.clear();
    }

    // --- ATM лимит операций в день ---
    public int getAtmOperationsToday() {
        if (today() != this.atmLastResetDay) {
            this.atmOperationsToday = 0;
            this.atmLastResetDay = today();
        }
        return this.atmOperationsToday;
    }

    public void incrementAtmOperations() {
        getAtmOperationsToday(); // сбросить если новый день
        this.atmOperationsToday++;
    }

    public long getAtmLastResetDay() { return this.atmLastResetDay; }
    public void setAtmLastResetDay(final long day) { this.atmLastResetDay = day; }
    public void setAtmOperationsToday(final int n) { this.atmOperationsToday = n; }

    // --- PIN блокировка ---
    public int getPinFailedAttempts() { return this.pinFailedAttempts; }
    public void setPinFailedAttempts(final int n) { this.pinFailedAttempts = n; }
    public boolean isPinLocked() { return this.pinLocked; }
    public void setPinLocked(final boolean locked) { this.pinLocked = locked; }

    public void incrementPinFailed() {
        this.pinFailedAttempts++;
        if (this.pinFailedAttempts >= 3) {
            this.pinLocked = true;
        }
    }

    public void resetPinFailed() {
        this.pinFailedAttempts = 0;
        this.pinLocked = false;
    }
}
