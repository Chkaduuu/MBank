package com.chkaduuu.mcbank.commands;

import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.*;
import org.bukkit.block.Block;
import org.bukkit.OfflinePlayer;
import com.chkaduuu.mcbank.listeners.ChatInputListener;
import com.chkaduuu.mcbank.models.BankAccount;
import com.chkaduuu.mcbank.models.PlayerData;
import com.chkaduuu.mcbank.managers.LanguageManager;
import com.chkaduuu.mcbank.managers.AccountManager;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.chkaduuu.mcbank.McBank;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;

public class McBankCommand implements CommandExecutor, TabCompleter
{
    private final McBank plugin;

    public McBankCommand(final McBank plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Player player = this.toPlayer(sender);
        if (args.length != 0) {
            final String sub = args[0].toLowerCase();
            switch (sub) {
                case "create": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.handleCreate(player);
                    break;
                }
                case "deposit": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.handleDeposit(sender, args);
                    break;
                }
                case "withdraw": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.handleWithdraw(sender, args);
                    break;
                }
                case "cashout": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.handleCashout(sender, args);
                    break;
                }
                case "info": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.handleInfo(player);
                    break;
                }
                case "loan": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.plugin.getGuiManager().openMainMenu(player);
                    break;
                }
                case "transfer": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.handleTransfer(player);
                    break;
                }
                case "history": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.handleHistory(player);
                    break;
                }
                case "top": {
                    if (!this.hasPermission(sender, "mcbank.use")) { this.noPermission(sender); return true; }
                    this.handleTop(sender);
                    break;
                }
                case "stats": {
                    if (!this.hasPermission(sender, "mcbank.admin")) { this.noPermission(sender); return true; }
                    this.handleStats(sender);
                    break;
                }
                case "help": {
                    this.handleHelp(sender);
                    break;
                }
                case "reload": {
                    if (!this.hasPermission(sender, "mcbank.admin")) { this.noPermission(sender); return true; }
                    this.plugin.reload();
                    if (player != null) this.plugin.getLanguageManager().send(player, "plugin_reloaded");
                    else sender.sendMessage(this.plugin.getLanguageManager().get("plugin_reloaded"));
                    break;
                }
                case "reset": {
                    if (!this.hasPermission(sender, "mcbank.admin")) { this.noPermission(sender); return true; }
                    this.handleReset(sender, args);
                    break;
                }
                case "level": {
                    if (!this.hasPermission(sender, "mcbank.admin")) { this.noPermission(sender); return true; }
                    this.handleLevel(sender, args);
                    break;
                }
                case "atm": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.atm.set")) { this.noPermission((CommandSender) player); return true; }
                    this.handleAtm(player, args);
                    break;
                }
                case "pin": {
                    if (player == null) { this.notPlayer(sender); return true; }
                    if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
                    this.handlePin(player, args);
                    break;
                }
                case "unpin": {
                    // Разблокировка PIN игрока администратором
                    if (!this.hasPermission(sender, "mcbank.admin")) { this.noPermission(sender); return true; }
                    this.handleUnpin(sender, args);
                    break;
                }
                default: {
                    if (player != null) this.plugin.getLanguageManager().send(player, "invalid_command");
                    else sender.sendMessage(this.plugin.getLanguageManager().get("invalid_command"));
                    break;
                }
            }
            return true;
        }
        if (player == null) { this.notPlayer(sender); return true; }
        if (!this.hasPermission((CommandSender) player, "mcbank.use")) { this.noPermission((CommandSender) player); return true; }
        if (!this.plugin.getAccountManager().hasAccount(player.getUniqueId())) {
            this.plugin.getLanguageManager().send(player, "no_account");
            return true;
        }
        this.plugin.getGuiManager().openMainMenu(player);
        return true;
    }

    // --- История транзакций ---
    private void handleHistory(final Player player) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        final AccountManager am = this.plugin.getAccountManager();
        if (!am.hasAccount(player.getUniqueId())) {
            lm.send(player, "no_account");
            return;
        }
        final BankAccount acc = am.getPrimaryAccount(player.getUniqueId());
        if (acc == null) return;

        player.sendMessage(lm.get("history_header"));
        final List<String> history = acc.getTransactionHistory();
        if (history.isEmpty()) {
            player.sendMessage(lm.get("history_empty"));
        } else {
            for (int i = 0; i < history.size(); i++) {
                player.sendMessage(lm.getFormatted("history_entry",
                    "%num%", String.valueOf(i + 1),
                    "%entry%", history.get(i)));
            }
        }
        player.sendMessage(lm.get("history_footer"));
    }

    // --- Топ игроков ---
    private void handleTop(final CommandSender sender) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        final List<Map.Entry<String, Double>> top = this.plugin.getAccountManager().getTopPlayers(10);
        sender.sendMessage(lm.get("top_header"));
        if (top.isEmpty()) {
            sender.sendMessage(lm.get("top_empty"));
        } else {
            for (int i = 0; i < top.size(); i++) {
                sender.sendMessage(lm.getFormatted("top_entry",
                    "%pos%", String.valueOf(i + 1),
                    "%player%", top.get(i).getKey(),
                    "%balance%", String.format("%.2f", top.get(i).getValue())));
            }
        }
        sender.sendMessage(lm.get("top_footer"));
    }

    // --- Статистика сервера (только для админов) ---
    private void handleStats(final CommandSender sender) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        final AccountManager am = this.plugin.getAccountManager();
        sender.sendMessage(lm.get("stats_header"));
        sender.sendMessage(lm.getFormatted("stats_total_money",
            "%amount%", String.format("%.2f", am.getTotalMoneyInCirculation())));
        sender.sendMessage(lm.getFormatted("stats_total_loans",
            "%amount%", String.format("%.2f", am.getTotalLoans())));
        sender.sendMessage(lm.getFormatted("stats_total_accounts",
            "%count%", String.valueOf(am.getTotalAccounts())));
        sender.sendMessage(lm.get("stats_footer"));
    }

    // --- Разблокировка PIN ---
    private void handleUnpin(final CommandSender sender, final String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /bank unpin <player>");
            return;
        }
        final OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[1]);
        final PlayerData pd = this.plugin.getAccountManager().getPlayerData(target.getUniqueId());
        if (pd == null) {
            sender.sendMessage(this.plugin.getLanguageManager().getFormatted("level_player_not_found", "%player%", args[1]));
            return;
        }
        pd.resetPinFailed();
        this.plugin.getAccountManager().saveData();
        sender.sendMessage(this.plugin.getLanguageManager().getFormatted("pin_unlocked", "%player%", args[1]));
    }

    // --- Стандартные методы (без изменений) ---
    private void handleCreate(final Player player) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        final AccountManager am = this.plugin.getAccountManager();
        if (am.hasAccount(player.getUniqueId())) { lm.send(player, "account_already_exists"); return; }
        final PlayerData pd = am.getOrCreatePlayerData(player.getUniqueId(), player.getName());
        if (pd.getAccounts().size() >= this.plugin.getConfigManager().getMaxAccountsPerPlayer()) {
            lm.send(player, "account_limit_reached", "%max%", String.valueOf(this.plugin.getConfigManager().getMaxAccountsPerPlayer()));
            return;
        }
        am.createAccount(player.getUniqueId(), player.getName());
        lm.send(player, "account_created");
        final BankAccount acc = am.getPrimaryAccount(player.getUniqueId());
        if (acc != null) lm.send(player, "account_number", "%account_number%", acc.getAccountNumber());
    }

    private void handleDeposit(final CommandSender sender, final String[] args) {
        final Player player = (Player) sender;
        final AccountManager am = this.plugin.getAccountManager();
        if (!am.hasAccount(player.getUniqueId())) { this.plugin.getLanguageManager().send(player, "no_account"); return; }
        if (args.length < 2) {
            this.plugin.getLanguageManager().send(player, "deposit_prompt");
            this.plugin.getChatInput().awaitInput(player, ChatInputListener.InputType.DEPOSIT, input -> this.processDeposit(player, input));
            return;
        }
        this.processDeposit(player, args[1]);
    }

    private void processDeposit(final Player player, final String input) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        final UUID uuid = player.getUniqueId();
        double amount;
        try { amount = Double.parseDouble(input.trim().replace(",", ".")); }
        catch (final NumberFormatException e) { lm.send(player, "invalid_amount"); return; }
        if (amount <= 0.0) { lm.send(player, "invalid_amount"); return; }
        final double minDeposit = this.plugin.getConfigManager().getMinDeposit();
        if (amount < minDeposit) { lm.send(player, "deposit_min_amount", "%min%", String.format("%.2f", minDeposit)); return; }
        if (this.plugin.getVaultHook() == null || !this.plugin.getVaultHook().isEnabled()) { lm.send(player, "vault_not_found"); return; }
        final double playerBalance = this.plugin.getVaultHook().getBalance(player);
        if (playerBalance < amount) { lm.send(player, "vault_not_enough_money", "%balance%", String.format("%.2f", playerBalance)); return; }
        final BankAccount acc = this.plugin.getAccountManager().getPrimaryAccount(uuid);
        if (acc == null) { lm.send(player, "no_account"); return; }
        final double limit = this.plugin.getAccountManager().getBankLimit(uuid);
        if (acc.getBalance() >= limit) { lm.send(player, "deposit_limit_reached", "%limit%", String.format("%.2f", limit)); return; }
        final double space = limit - acc.getBalance();
        if (amount > space) { amount = space; lm.send(player, "deposit_capped", "%amount%", String.format("%.2f", amount), "%limit%", String.format("%.2f", limit)); }
        this.plugin.getVaultHook().withdraw(player, amount);
        acc.deposit(amount);
        this.plugin.getAccountManager().recordTransaction(uuid, "DEPOSIT", amount, "");
        this.plugin.getAccountManager().saveData();
        lm.send(player, "deposit_success", "%amount%", String.format("%.2f", amount));
    }

    private void handleWithdraw(final CommandSender sender, final String[] args) {
        final Player player = (Player) sender;
        final AccountManager am = this.plugin.getAccountManager();
        if (!am.hasAccount(player.getUniqueId())) { this.plugin.getLanguageManager().send(player, "no_account"); return; }
        if (args.length < 2) {
            this.plugin.getLanguageManager().send(player, "withdraw_prompt");
            this.plugin.getChatInput().awaitInput(player, ChatInputListener.InputType.WITHDRAW, input -> this.processWithdraw(player, input));
            return;
        }
        this.processWithdraw(player, args[1]);
    }

    private void processWithdraw(final Player player, final String input) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        final UUID uuid = player.getUniqueId();
        double amount;
        try { amount = Double.parseDouble(input.trim().replace(",", ".")); }
        catch (final NumberFormatException e) { lm.send(player, "invalid_amount"); return; }
        if (amount <= 0.0) { lm.send(player, "invalid_amount"); return; }
        final double minWithdraw = this.plugin.getConfigManager().getMinWithdraw();
        if (amount < minWithdraw) { lm.send(player, "withdraw_min_amount", "%min%", String.format("%.2f", minWithdraw)); return; }
        final BankAccount acc = this.plugin.getAccountManager().getPrimaryAccount(uuid);
        if (acc == null) { lm.send(player, "no_account"); return; }
        if (acc.getBalance() < amount) { lm.send(player, "withdraw_insufficient", "%balance%", String.format("%.2f", acc.getBalance())); return; }
        if (this.plugin.getVaultHook() == null || !this.plugin.getVaultHook().isEnabled()) { lm.send(player, "vault_not_found"); return; }
        acc.withdraw(amount);
        this.plugin.getVaultHook().deposit(player, amount);
        this.plugin.getAccountManager().recordTransaction(uuid, "WITHDRAW", amount, "");
        this.plugin.getAccountManager().saveData();
        lm.send(player, "withdraw_success", "%amount%", String.format("%.2f", amount));
    }

    private void handleCashout(final CommandSender sender, final String[] args) {
        final Player player = (Player) sender;
        if (args.length < 2) {
            this.plugin.getLanguageManager().send(player, "cashout_prompt");
            this.plugin.getChatInput().awaitInput(player, ChatInputListener.InputType.CASHOUT, input -> this.processCashout(player, input));
            return;
        }
        this.processCashout(player, args[1]);
    }

    private void processCashout(final Player player, final String input) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        final UUID uuid = player.getUniqueId();
        double amount;
        try { amount = Double.parseDouble(input.trim().replace(",", ".")); }
        catch (final NumberFormatException e) { lm.send(player, "invalid_amount"); return; }
        if (amount <= 0.0) { lm.send(player, "invalid_amount"); return; }
        final BankAccount acc = this.plugin.getAccountManager().getPrimaryAccount(uuid);
        if (acc == null) { lm.send(player, "no_account"); return; }
        if (acc.getBalance() < amount) { lm.send(player, "cashout_insufficient", "%balance%", String.format("%.2f", acc.getBalance())); return; }
        acc.withdraw(amount);
        this.plugin.getAccountManager().recordTransaction(uuid, "CASHOUT", amount, "");
        this.plugin.getAccountManager().saveData();
        lm.send(player, "cashout_success", "%amount%", String.format("%.2f", amount));
    }

    private void handleInfo(final Player player) {
        final UUID uuid = player.getUniqueId();
        final LanguageManager lm = this.plugin.getLanguageManager();
        final AccountManager am = this.plugin.getAccountManager();
        if (!am.hasAccount(uuid)) { lm.send(player, "no_account"); return; }
        final BankAccount acc = am.getPrimaryAccount(uuid);
        final PlayerData pd = am.getPlayerData(uuid);
        if (acc == null || pd == null) return;
        final double loanLimit = am.getLoanLimit(uuid);
        final double interestPct = this.plugin.getConfigManager().getInterestPercent();
        final String status = acc.isActive() ? lm.get("info_status_active") : lm.get("info_status_inactive");
        player.sendMessage(lm.get("info_header"));
        player.sendMessage(lm.getFormatted("info_account_number", "%account_number%", acc.getAccountNumber()));
        player.sendMessage(lm.getFormatted("info_balance", "%balance%", String.format("%.2f", acc.getBalance())));
        player.sendMessage(lm.getFormatted("info_loan", "%loan%", String.format("%.2f", acc.getLoan())));
        player.sendMessage(lm.getFormatted("info_loan_limit", "%loan_limit%", String.format("%.2f", loanLimit)));
        player.sendMessage(lm.getFormatted("info_loan_percent", "%percent%", String.valueOf(interestPct)));
        player.sendMessage(lm.getFormatted("info_level", "%level%", String.valueOf(pd.getLevel())));
        player.sendMessage(lm.getFormatted("info_status", "%status%", status));
        player.sendMessage(lm.get("info_footer"));
    }

    private void handleTransfer(final Player player) {
        if (!this.plugin.getAccountManager().hasAccount(player.getUniqueId())) {
            this.plugin.getLanguageManager().send(player, "no_account"); return;
        }
        this.plugin.getGuiManager().openMainMenu(player);
    }

    private void handleHelp(final CommandSender sender) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        sender.sendMessage(lm.get("help_header"));
        sender.sendMessage(lm.get("help_open"));
        sender.sendMessage(lm.get("help_create"));
        sender.sendMessage(lm.get("help_deposit"));
        sender.sendMessage(lm.get("help_withdraw"));
        sender.sendMessage(lm.get("help_cashout"));
        sender.sendMessage(lm.get("help_info"));
        sender.sendMessage(lm.get("help_loan"));
        sender.sendMessage(lm.get("help_history"));
        sender.sendMessage(lm.get("help_top"));
        sender.sendMessage(lm.get("help_help"));
        sender.sendMessage(lm.get("help_pin_create"));
        sender.sendMessage(lm.get("help_pin_edit"));
        if (sender.hasPermission("mcbank.admin")) {
            sender.sendMessage(lm.get("help_admin_header"));
            sender.sendMessage(lm.get("help_stats"));
            sender.sendMessage(lm.get("help_reset"));
            sender.sendMessage(lm.get("help_level_give"));
            sender.sendMessage(lm.get("help_level_take"));
            sender.sendMessage(lm.get("help_atm_set"));
            sender.sendMessage(lm.get("help_atm_remove"));
            sender.sendMessage(lm.get("help_pin_info"));
            sender.sendMessage(lm.get("help_unpin"));
            sender.sendMessage(lm.get("help_reload"));
        }
        sender.sendMessage(lm.get("help_author"));
        sender.sendMessage(lm.get("help_footer"));
    }

    private void handleReset(final CommandSender sender, final String[] args) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        if (args.length < 2) { sender.sendMessage("Usage: /bank reset <player|all>"); return; }
        if (args[1].equalsIgnoreCase("all")) {
            this.plugin.getAccountManager().resetAll();
            sender.sendMessage(lm.get("reset_all_success"));
        } else {
            final OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[1]);
            this.plugin.getAccountManager().resetAccount(target.getUniqueId());
            sender.sendMessage(lm.getFormatted("reset_success", "%player%", args[1]));
        }
    }

    private void handleLevel(final CommandSender sender, final String[] args) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        if (args.length < 3) { sender.sendMessage("Usage: /bank level give|take <player>"); return; }
        final OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[2]);
        final UUID targetUUID = target.getUniqueId();
        final AccountManager am = this.plugin.getAccountManager();
        final PlayerData pd = am.getPlayerData(targetUUID);
        if (pd == null) { sender.sendMessage(lm.getFormatted("level_player_not_found", "%player%", args[2])); return; }
        if (args[1].equalsIgnoreCase("give")) {
            am.giveLoanLevel(targetUUID);
            sender.sendMessage(lm.getFormatted("level_give", "%player%", args[2], "%level%", String.valueOf(pd.getLevel())));
        } else if (args[1].equalsIgnoreCase("take")) {
            if (pd.getLevel() <= 0) { sender.sendMessage(lm.getFormatted("level_min", "%player%", args[2])); return; }
            am.takeLoanLevel(targetUUID);
            sender.sendMessage(lm.getFormatted("level_take", "%player%", args[2], "%level%", String.valueOf(pd.getLevel())));
        }
    }

    private void handleAtm(final Player player, final String[] args) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        if (args.length < 2) { player.sendMessage("Usage: /bank atm set|remove"); return; }
        final Block targetBlock = player.getTargetBlockExact(5);
        if (args[1].equalsIgnoreCase("set")) {
            if (targetBlock == null) { lm.send(player, "atm_set_look"); return; }
            if (this.plugin.getAtmManager().isATM(targetBlock)) { lm.send(player, "atm_set_already"); return; }
            this.plugin.getAtmManager().addATM(targetBlock, player.getName());
            lm.send(player, "atm_set_success");
        } else if (args[1].equalsIgnoreCase("remove")) {
            if (targetBlock == null || !this.plugin.getAtmManager().isATM(targetBlock)) { lm.send(player, "atm_remove_not_found"); return; }
            this.plugin.getAtmManager().removeATM(targetBlock);
            lm.send(player, "atm_remove_success");
        }
    }

    private void handlePin(final Player player, final String[] args) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        if (args.length < 2) { player.sendMessage("Usage: /bank pin create|edit|info"); return; }
        final UUID uuid = player.getUniqueId();
        final String action = args[1].toLowerCase();
        if (action.equals("create")) {
            if (args.length < 3) { player.sendMessage("Usage: /bank pin create <4-digit-pin>"); return; }
            if (!args[2].matches("\\d{4}")) { lm.send(player, "pin_invalid_format"); return; }
            if (this.plugin.getAccountManager().getPin(uuid) != null) { lm.send(player, "pin_already_exists"); return; }
            this.plugin.getAccountManager().setPin(uuid, args[2]);
            lm.send(player, "pin_created");
        } else if (action.equals("edit")) {
            if (args.length < 4) { player.sendMessage("Usage: /bank pin edit <old> <new>"); return; }
            if (!args[3].matches("\\d{4}")) { lm.send(player, "pin_invalid_format"); return; }
            final String stored = this.plugin.getAccountManager().getPin(uuid);
            if (stored == null) { lm.send(player, "pin_no_pin"); return; }
            if (!stored.equals(args[2])) { lm.send(player, "pin_wrong_old"); return; }
            this.plugin.getAccountManager().setPin(uuid, args[3]);
            lm.send(player, "pin_changed");
        } else if (action.equals("info") && player.hasPermission("mcbank.admin")) {
            if (args.length < 3) { player.sendMessage("Usage: /bank pin info <player>"); return; }
            final OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[2]);
            final String pin = this.plugin.getAccountManager().getPin(target.getUniqueId());
            if (pin != null) player.sendMessage(lm.getFormatted("pin_info_has", "%player%", args[2], "%pin%", pin));
            else player.sendMessage(lm.getFormatted("pin_info_no", "%player%", args[2]));
        }
    }

    private Player toPlayer(final CommandSender sender) {
        return (sender instanceof Player) ? (Player) sender : null;
    }

    private boolean hasPermission(final CommandSender sender, final String perm) {
        return sender.hasPermission(perm);
    }

    private void noPermission(final CommandSender sender) {
        sender.sendMessage(this.plugin.getLanguageManager().get("no_permission"));
    }

    private void notPlayer(final CommandSender sender) {
        sender.sendMessage(this.plugin.getLanguageManager().get("player_only"));
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        List<String> subs = new ArrayList<>(Arrays.asList(
            "create", "deposit", "withdraw", "cashout", "info", "loan", "transfer", "history", "top", "help", "pin"));
        if (sender.hasPermission("mcbank.admin")) {
            subs.addAll(Arrays.asList("reload", "reset", "level", "atm", "stats", "unpin"));
        }
        if (args.length == 1) {
            final String prefix = args[0].toLowerCase();
            return subs.stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("level")) return Arrays.asList("give", "take");
            if (args[0].equalsIgnoreCase("atm")) return Arrays.asList("set", "remove");
            if (args[0].equalsIgnoreCase("pin")) return Arrays.asList("create", "edit", "info");
            if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("unpin")) {
                final List<String> list = new ArrayList<>();
                list.add("all");
                this.plugin.getServer().getOnlinePlayers().forEach(p -> list.add(p.getName()));
                return list;
            }
        }
        if (args.length == 3 && (args[0].equalsIgnoreCase("level") || args[0].equalsIgnoreCase("reset"))) {
            return this.plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
