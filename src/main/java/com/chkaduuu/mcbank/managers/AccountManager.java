package com.chkaduuu.mcbank.managers;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.IOException;
import org.bukkit.configuration.ConfigurationSection;
import com.chkaduuu.mcbank.models.BankAccount;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import com.chkaduuu.mcbank.models.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import com.chkaduuu.mcbank.McBank;

public class AccountManager
{
    private final McBank plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, PlayerData> playerDataMap;
    private BukkitTask schedulerTask;
    private BukkitTask loanPenaltyTask;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd.MM HH:mm");

    public AccountManager(final McBank plugin) {
        this.playerDataMap = new HashMap<>();
        this.plugin = plugin;
    }

    public void load() {
        this.dataFile = new File(this.plugin.getDataFolder(), "files/data.yml");
        if (!this.dataFile.exists()) {
            this.plugin.saveResource("files/data.yml", false);
        }
        this.dataConfig = (FileConfiguration) YamlConfiguration.loadConfiguration(this.dataFile);
        this.loadData();
    }

    private void loadData() {
        this.playerDataMap.clear();
        final ConfigurationSection players = this.dataConfig.getConfigurationSection("players");
        if (players == null) return;

        for (final String uuidStr : players.getKeys(false)) {
            try {
                final UUID uuid = UUID.fromString(uuidStr);
                final ConfigurationSection ps = players.getConfigurationSection(uuidStr);
                if (ps == null) continue;

                final String name = ps.getString("name", "Unknown");
                final int level = ps.getInt("level", 0);
                final String pin = ps.getString("pin", null);
                final int bankLevel = ps.getInt("bank_level", 1);
                final boolean pinLocked = ps.getBoolean("pin_locked", false);
                final int pinFailed = ps.getInt("pin_failed", 0);
                final int atmOps = ps.getInt("atm_ops_today", 0);
                final long atmDay = ps.getLong("atm_last_day", 0L);

                final PlayerData pd = new PlayerData(name);
                pd.setLevel(level);
                pd.setPin(pin);
                pd.setBankLevel(bankLevel);
                pd.setPinLocked(pinLocked);
                pd.setPinFailedAttempts(pinFailed);
                pd.setAtmOperationsToday(atmOps);
                pd.setAtmLastResetDay(atmDay);

                // Оффлайн уведомления
                final List<String> notifs = ps.getStringList("pending_notifications");
                for (String n : notifs) pd.addPendingNotification(n);

                final ConfigurationSection accounts = ps.getConfigurationSection("accounts");
                if (accounts != null) {
                    for (final String accNum : accounts.getKeys(false)) {
                        final ConfigurationSection acc = accounts.getConfigurationSection(accNum);
                        if (acc == null) continue;
                        final double balance = acc.getDouble("balance", 0.0);
                        final double loan = acc.getDouble("loan", 0.0);
                        final boolean active = acc.getBoolean("active", true);
                        final long created = acc.getLong("created", System.currentTimeMillis());
                        final BankAccount bankAccount = new BankAccount(accNum, balance, loan, active, created);
                        bankAccount.setLoanTakenAt(acc.getLong("loan_taken_at", 0L));
                        bankAccount.setLoanDueDays(acc.getLong("loan_due_days", 0L));
                        // История транзакций
                        final List<String> history = acc.getStringList("history");
                        for (String h : history) bankAccount.addTransaction(h);
                        pd.getAccounts().put(accNum, bankAccount);
                    }
                }
                this.playerDataMap.put(uuid, pd);
            } catch (final IllegalArgumentException ignored) {}
        }
    }

    public void saveData() {
        this.dataConfig.set("players", null);
        for (Map.Entry<UUID, PlayerData> entry : this.playerDataMap.entrySet()) {
            final String base = "players." + entry.getKey();
            final PlayerData pd = entry.getValue();
            this.dataConfig.set(base + ".name", pd.getName());
            this.dataConfig.set(base + ".level", pd.getLevel());
            this.dataConfig.set(base + ".pin", pd.getPin());
            this.dataConfig.set(base + ".bank_level", pd.getBankLevel());
            this.dataConfig.set(base + ".pin_locked", pd.isPinLocked());
            this.dataConfig.set(base + ".pin_failed", pd.getPinFailedAttempts());
            this.dataConfig.set(base + ".atm_ops_today", pd.getAtmOperationsToday());
            this.dataConfig.set(base + ".atm_last_day", pd.getAtmLastResetDay());
            this.dataConfig.set(base + ".pending_notifications", pd.getPendingNotifications());

            for (Map.Entry<String, BankAccount> accEntry : pd.getAccounts().entrySet()) {
                final String accBase = base + ".accounts." + accEntry.getKey();
                final BankAccount acc = accEntry.getValue();
                this.dataConfig.set(accBase + ".balance", acc.getBalance());
                this.dataConfig.set(accBase + ".loan", acc.getLoan());
                this.dataConfig.set(accBase + ".active", acc.isActive());
                this.dataConfig.set(accBase + ".created", acc.getCreated());
                this.dataConfig.set(accBase + ".loan_taken_at", acc.getLoanTakenAt());
                this.dataConfig.set(accBase + ".loan_due_days", acc.getLoanDueDays());
                this.dataConfig.set(accBase + ".history", acc.getTransactionHistory());
            }
        }
        try {
            this.dataConfig.save(this.dataFile);
        } catch (final IOException e) {
            this.plugin.getLogger().severe("Failed to save data.yml: " + e.getMessage());
        }
    }

    // --- Запись транзакции ---
    public void recordTransaction(final UUID uuid, final String type, final double amount, final String extra) {
        final BankAccount acc = getPrimaryAccount(uuid);
        if (acc == null) return;
        final String date = DATE_FMT.format(new Date());
        String entry = date + " | " + type + " | " + String.format("%.2f", amount);
        if (extra != null && !extra.isEmpty()) entry += " | " + extra;
        acc.addTransaction(entry);
    }

    // --- ТОП игроков по балансу ---
    public List<Map.Entry<String, Double>> getTopPlayers(int limit) {
        final List<Map.Entry<String, Double>> list = new ArrayList<>();
        for (PlayerData pd : playerDataMap.values()) {
            final BankAccount acc = pd.getPrimaryAccount();
            if (acc != null) {
                list.add(new AbstractMap.SimpleEntry<>(pd.getName(), acc.getBalance()));
            }
        }
        list.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    // --- Статистика сервера ---
    public double getTotalMoneyInCirculation() {
        double total = 0;
        for (PlayerData pd : playerDataMap.values()) {
            BankAccount acc = pd.getPrimaryAccount();
            if (acc != null) total += acc.getBalance();
        }
        return total;
    }

    public double getTotalLoans() {
        double total = 0;
        for (PlayerData pd : playerDataMap.values()) {
            BankAccount acc = pd.getPrimaryAccount();
            if (acc != null) total += acc.getLoan();
        }
        return total;
    }

    public int getTotalAccounts() {
        int count = 0;
        for (PlayerData pd : playerDataMap.values()) {
            count += pd.getAccounts().size();
        }
        return count;
    }

    // --- Штраф за просроченный кредит ---
    public void startLoanPenaltyScheduler() {
        // Проверка каждые 5 минут
        this.loanPenaltyTask = this.plugin.getServer().getScheduler().runTaskTimer(
            (Plugin) this.plugin,
            this::checkLoanPenalties,
            6000L, 6000L
        );
    }

    public void stopLoanPenaltyScheduler() {
        if (this.loanPenaltyTask != null) {
            this.loanPenaltyTask.cancel();
            this.loanPenaltyTask = null;
        }
    }

    private void checkLoanPenalties() {
        final double penaltyPercent = this.plugin.getConfigManager().getLoanPenaltyPercent();
        if (penaltyPercent <= 0) return;

        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            final UUID uuid = entry.getKey();
            final PlayerData pd = entry.getValue();
            final BankAccount acc = pd.getPrimaryAccount();
            if (acc == null || acc.getLoan() <= 0) continue;
            if (!acc.isLoanOverdue()) continue;

            // Начислить штраф
            final double penalty = acc.getLoan() * (penaltyPercent / 100.0);
            acc.setLoan(acc.getLoan() + penalty);

            final Player player = this.plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                final String msg = this.plugin.getLanguageManager().getFormatted(
                    "loan_overdue_penalty",
                    "%penalty%", String.format("%.2f", penalty),
                    "%loan%", String.format("%.2f", acc.getLoan())
                );
                player.sendMessage(msg);
            } else {
                // Сохранить уведомление для оффлайн игрока
                final String msg = this.plugin.getLanguageManager().getFormatted(
                    "loan_overdue_penalty",
                    "%penalty%", String.format("%.2f", penalty),
                    "%loan%", String.format("%.2f", acc.getLoan())
                );
                pd.addPendingNotification(msg);
            }
        }
        this.saveData();
    }

    // --- Оффлайн уведомления ---
    public void deliverPendingNotifications(final Player player) {
        final PlayerData pd = playerDataMap.get(player.getUniqueId());
        if (pd == null || pd.getPendingNotifications().isEmpty()) return;
        for (String msg : pd.getPendingNotifications()) {
            player.sendMessage(msg);
        }
        pd.clearPendingNotifications();
        saveData();
    }

    // --- Стандартные методы (без изменений) ---
    public boolean hasAccount(final UUID uuid) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        return pd != null && !pd.getAccounts().isEmpty();
    }

    public PlayerData getPlayerData(final UUID uuid) { return this.playerDataMap.get(uuid); }

    public PlayerData getOrCreatePlayerData(final UUID uuid, final String name) {
        return this.playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(name));
    }

    public BankAccount getPrimaryAccount(final UUID uuid) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        if (pd == null) return null;
        return pd.getPrimaryAccount();
    }

    public void createAccount(final UUID uuid, final String playerName) {
        final PlayerData pd = this.getOrCreatePlayerData(uuid, playerName);
        pd.setName(playerName);
        final String accNumber = this.generateAccountNumber();
        final double startingBalance = this.plugin.getConfigManager().getStartingBalance();
        final BankAccount account = new BankAccount(accNumber, startingBalance, 0.0, true, System.currentTimeMillis());
        pd.getAccounts().put(accNumber, account);
        this.saveData();
    }

    private String generateAccountNumber() {
        final List<String> formats = this.plugin.getConfigManager().getAccountFormats();
        final String format = formats.isEmpty() ? "GE87BG0000000" : formats.get(ThreadLocalRandom.current().nextInt(formats.size()));
        final StringBuilder num = new StringBuilder();
        for (int i = 0; i < 7; ++i) num.append(ThreadLocalRandom.current().nextInt(10));
        return format.replaceAll("0+$", num.toString());
    }

    public double getBankLimit(final UUID uuid) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        if (pd == null) return 0.0;
        return this.plugin.getBankUpgradeManager().getLimit(pd.getBankLevel());
    }

    public double getLoanLimit(final UUID uuid) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        if (pd == null) return 0.0;
        return this.plugin.getBankUpgradeManager().getLoanLimit(pd.getLevel());
    }

    public String getPin(final UUID uuid) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        return (pd != null) ? pd.getPin() : null;
    }

    public void setPin(final UUID uuid, final String pin) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        if (pd != null) pd.setPin(pin);
        this.saveData();
    }

    public void resetAccount(final UUID uuid) {
        this.playerDataMap.remove(uuid);
        this.saveData();
    }

    public void resetAll() {
        this.playerDataMap.clear();
        this.saveData();
    }

    public void giveLoanLevel(final UUID uuid) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        if (pd != null) pd.setLevel(pd.getLevel() + 1);
        this.saveData();
    }

    public void takeLoanLevel(final UUID uuid) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        if (pd != null && pd.getLevel() > 0) pd.setLevel(pd.getLevel() - 1);
        this.saveData();
    }

    public void upgradeBankLevel(final UUID uuid) {
        final PlayerData pd = this.playerDataMap.get(uuid);
        if (pd != null) pd.setBankLevel(pd.getBankLevel() + 1);
        this.saveData();
    }

    public void startIncomeScheduler() {
        final long ticks = this.parseTicks(this.plugin.getConfigManager().getBankIncomeTime());
        this.schedulerTask = this.plugin.getServer().getScheduler().runTaskTimer(
            (Plugin) this.plugin, this::distributeIncome, ticks, ticks);
    }

    public void stopIncomeScheduler() {
        if (this.schedulerTask != null) {
            this.schedulerTask.cancel();
            this.schedulerTask = null;
        }
    }

    private void distributeIncome() {
        for (final Map.Entry<UUID, PlayerData> entry : this.playerDataMap.entrySet()) {
            final UUID uuid = entry.getKey();
            final PlayerData pd = entry.getValue();
            final BankAccount acc = pd.getPrimaryAccount();
            if (acc == null) continue;
            final int bankLevel = pd.getBankLevel();
            final double incomeRate = this.plugin.getBankUpgradeManager().getOnlineIncomeRate(bankLevel);
            if (incomeRate <= 0.0) continue;
            final double income = acc.getBalance() * (incomeRate / 100.0);
            final double limit = this.plugin.getBankUpgradeManager().getLimit(bankLevel);
            final double newBalance = Math.min(acc.getBalance() + income, limit);
            final double actualIncome = newBalance - acc.getBalance();
            if (actualIncome <= 0.0) continue;
            acc.setBalance(newBalance);
            final Player player = this.plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                final String msg = this.plugin.getLanguageManager().getFormatted(
                    "bank_income_received", "%amount%", String.format("%.2f", actualIncome));
                player.sendMessage(msg);
            }
        }
        this.saveData();
    }

    private long parseTicks(final String time) {
        if (time == null || time.isEmpty()) return 6000L;
        try {
            if (time.endsWith("m")) return Long.parseLong(time.substring(0, time.length() - 1)) * 1200L;
            if (time.endsWith("h")) return Long.parseLong(time.substring(0, time.length() - 1)) * 72000L;
            if (time.endsWith("s")) return Long.parseLong(time.substring(0, time.length() - 1)) * 20L;
            return Long.parseLong(time) * 20L;
        } catch (final NumberFormatException e) {
            return 6000L;
        }
    }

    public Map<UUID, PlayerData> getAllPlayerData() { return this.playerDataMap; }

    public long getIncomeTaskTimeRemaining() {
        if (this.schedulerTask == null) return -1L;
        return this.parseTicks(this.plugin.getConfigManager().getBankIncomeTime());
    }
}
