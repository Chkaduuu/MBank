// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.listeners;

import java.util.Map;
import com.chkaduuu.mcbank.models.PlayerData;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.List;
import java.util.ArrayList;
import org.bukkit.Material;
import com.chkaduuu.mcbank.models.BankAccount;
import com.chkaduuu.mcbank.managers.LanguageManager;
import com.chkaduuu.mcbank.managers.AccountManager;
import java.util.UUID;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import com.chkaduuu.mcbank.managers.GUIManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import com.chkaduuu.mcbank.McBank;
import org.bukkit.event.Listener;

public class GUIListener implements Listener
{
    private final McBank plugin;
    
    public GUIListener(final McBank plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        final HumanEntity whoClicked = event.getWhoClicked();
        if (!(whoClicked instanceof Player)) {
            return;
        }
        final Player player = (Player)whoClicked;
        if (event.getClickedInventory() == null) {
            return;
        }
        final String title = event.getView().getTitle();
        final GUIManager gui = this.plugin.getGuiManager();
        if (gui.isMainMenu(title)) {
            event.setCancelled(true);
            final int slot = event.getSlot();
            final boolean rightClick = event.isRightClick();
            this.handleMainMenu(player, slot, rightClick);
        }
        else if (gui.isATMMenu(title)) {
            event.setCancelled(true);
            this.handleATMMenu(player, event.getSlot());
        }
        else if (gui.isPINMenu(title)) {
            event.setCancelled(true);
            final ItemStack item = event.getCurrentItem();
            this.handlePINMenu(player, event.getSlot(), item);
        }
    }
    
    private void handleMainMenu(final Player player, final int slot, final boolean rightClick) {
        final UUID uuid = player.getUniqueId();
        final AccountManager am = this.plugin.getAccountManager();
        final LanguageManager lm = this.plugin.getLanguageManager();
        if (!am.hasAccount(uuid)) {
            lm.send(player, "no_account", new Object[0]);
            player.closeInventory();
            return;
        }
        final GUIManager gui = this.plugin.getGuiManager();
        if (slot == gui.getAccountInfoSlot()) {
            player.closeInventory();
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> gui.openMainMenu(player), 2L);
        }
        else if (slot == gui.getDepositSlot()) {
            player.closeInventory();
            this.handleDeposit(player);
        }
        else if (slot == gui.getWithdrawSlot()) {
            player.closeInventory();
            this.handleWithdraw(player);
        }
        else if (slot == gui.getCashoutSlot()) {
            player.closeInventory();
            this.handleCashout(player);
        }
        else if (slot == gui.getTransferSlot()) {
            player.closeInventory();
            this.handleTransfer(player);
        }
        else if (slot == gui.getLoanSlot()) {
            player.closeInventory();
            if (rightClick) {
                this.handleLoanRepay(player);
            }
            else {
                this.handleLoanTake(player);
            }
        }
        else if (slot == gui.getUpgradeSlot()) {
            player.closeInventory();
            this.handleUpgrade(player);
        }
    }
    
    private void handleDeposit(final Player player) {
        final UUID uuid = player.getUniqueId();
        final double minDeposit = this.plugin.getConfigManager().getMinDeposit();
        this.plugin.getLanguageManager().send(player, "deposit_prompt", new Object[0]);
        this.plugin.getChatInputListener().awaitInput(player, ChatInputListener.InputType.DEPOSIT, input -> this.processDeposit(player, uuid, minDeposit, input));
    }
    
    private void processDeposit(final Player player, final UUID uuid, final double minDeposit, final String input) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        double amount;
        try {
            amount = Double.parseDouble(input.trim().replace(",", "."));
        }
        catch (final NumberFormatException e) {
            lm.send(player, "invalid_amount", new Object[0]);
            return;
        }
        if (amount <= 0.0) {
            lm.send(player, "invalid_amount", new Object[0]);
            return;
        }
        if (amount < minDeposit) {
            lm.send(player, "deposit_min_amount", "%min%", String.format("%.2f", minDeposit));
            return;
        }
        if (this.plugin.getVaultHook() == null || !this.plugin.getVaultHook().isEnabled()) {
            lm.send(player, "vault_not_found", new Object[0]);
            return;
        }
        final double playerBalance = this.plugin.getVaultHook().getBalance(player);
        if (playerBalance < amount) {
            lm.send(player, "vault_not_enough_money", "%balance%", String.format("%.2f", playerBalance));
            return;
        }
        final BankAccount acc = this.plugin.getAccountManager().getPrimaryAccount(uuid);
        if (acc == null) {
            lm.send(player, "no_account", new Object[0]);
            return;
        }
        final double limit = this.plugin.getAccountManager().getBankLimit(uuid);
        if (acc.getBalance() >= limit) {
            lm.send(player, "deposit_limit_reached", "%limit%", String.format("%.2f", limit));
            return;
        }
        final double space = limit - acc.getBalance();
        if (amount > space) {
            amount = space;
            lm.send(player, "deposit_capped", "%amount%", String.format("%.2f", amount), "%limit%", String.format("%.2f", limit));
        }
        this.plugin.getVaultHook().withdraw(player, amount);
        acc.deposit(amount);
        this.plugin.getAccountManager().saveData();
        lm.send(player, "deposit_success", "%amount%", String.format("%.2f", amount));
    }
    
    private void handleWithdraw(final Player player) {
        final UUID uuid = player.getUniqueId();
        final double minWithdraw = this.plugin.getConfigManager().getMinWithdraw();
        this.plugin.getLanguageManager().send(player, "withdraw_prompt", new Object[0]);
        this.plugin.getChatInputListener().awaitInput(player, ChatInputListener.InputType.WITHDRAW, input -> this.processWithdraw(player, uuid, minWithdraw, input));
    }
    
    private void processWithdraw(final Player player, final UUID uuid, final double minWithdraw, final String input) {
        final LanguageManager lm = this.plugin.getLanguageManager();
        double amount;
        try {
            amount = Double.parseDouble(input.trim().replace(",", "."));
        }
        catch (final NumberFormatException e) {
            lm.send(player, "invalid_amount", new Object[0]);
            return;
        }
        if (amount <= 0.0) {
            lm.send(player, "invalid_amount", new Object[0]);
            return;
        }
        if (amount < minWithdraw) {
            lm.send(player, "withdraw_min_amount", "%min%", String.format("%.2f", minWithdraw));
            return;
        }
        final BankAccount acc = this.plugin.getAccountManager().getPrimaryAccount(uuid);
        if (acc == null) {
            lm.send(player, "no_account", new Object[0]);
            return;
        }
        if (acc.getBalance() < amount) {
            lm.send(player, "withdraw_insufficient", "%balance%", String.format("%.2f", acc.getBalance()));
            return;
        }
        if (this.plugin.getVaultHook() == null || !this.plugin.getVaultHook().isEnabled()) {
            lm.send(player, "vault_not_found", new Object[0]);
            return;
        }
        acc.withdraw(amount);
        this.plugin.getVaultHook().deposit(player, amount);
        this.plugin.getAccountManager().saveData();
        lm.send(player, "withdraw_success", "%amount%", String.format("%.2f", amount));
    }
    
    private void handleCashout(final Player player) {
        final UUID uuid = player.getUniqueId();
        final double minCashout = this.plugin.getConfigManager().getMinDeposit();
        this.plugin.getLanguageManager().send(player, "cashout_prompt", new Object[0]);
        this.plugin.getChatInputListener().awaitInput(player, ChatInputListener.InputType.CASHOUT, input -> {
            final LanguageManager lm = this.plugin.getLanguageManager();
            double amount;
            try {
                amount = Double.parseDouble(input.trim().replace(",", "."));
            }
            catch (final NumberFormatException e) {
                lm.send(player, "invalid_amount", new Object[0]);
                return;
            }
            if (amount <= 0.0) {
                lm.send(player, "invalid_amount", new Object[0]);
            }
            else if (amount < minCashout) {
                lm.send(player, "cashout_min_amount", "%min%", String.format("%.2f", minCashout));
            }
            else {
                final BankAccount acc = this.plugin.getAccountManager().getPrimaryAccount(uuid);
                if (acc == null) {
                    lm.send(player, "no_account", new Object[0]);
                }
                else if (acc.getBalance() < amount) {
                    lm.send(player, "cashout_insufficient", "%balance%", String.format("%.2f", acc.getBalance()));
                }
                else {
                    acc.withdraw(amount);
                    this.plugin.getAccountManager().saveData();
                    final ItemStack banknote = new ItemStack(Material.PAPER);
                    final ItemMeta meta = banknote.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(LanguageManager.colorize("§6Bank Note §e" + amount));
                        final ArrayList<String> lore = new ArrayList<String>();
                        lore.add(LanguageManager.colorize("§7Amount: §e" + String.format("%.2f", amount)));
                        lore.add("§8MCBANK_NOTE:" + amount);
                        meta.setLore((List)lore);
                        banknote.setItemMeta(meta);
                    }
                    if (player.getInventory().firstEmpty() == -1) {
                        player.getWorld().dropItemNaturally(player.getLocation(), banknote);
                    }
                    else {
                        player.getInventory().addItem(new ItemStack[] { banknote });
                    }
                    lm.send(player, "cashout_success", "%amount%", String.format("%.2f", amount));
                }
            }
        });
    }
    
    private void handleTransfer(final Player player) {
        final UUID senderUUID = player.getUniqueId();
        final double minTransfer = this.plugin.getConfigManager().getMinTransfer();
        final LanguageManager lm = this.plugin.getLanguageManager();
        lm.send(player, "transfer_prompt_player", new Object[0]);
        final ChatInputListener chatInput = this.plugin.getChatInputListener();
        chatInput.awaitInput(player, ChatInputListener.InputType.TRANSFER_PLAYER, targetName -> {
            final Player target = this.plugin.getServer().getPlayer(targetName);
            if (target == null) {
                lm.send(player, "transfer_player_not_found", "%player%", targetName);
            }
            else if (target.getUniqueId().equals(senderUUID)) {
                lm.send(player, "transfer_self", new Object[0]);
            }
            else if (!this.plugin.getAccountManager().hasAccount(target.getUniqueId())) {
                lm.send(player, "transfer_no_account", "%player%", target.getName());
            }
            else {
                lm.send(player, "transfer_prompt_amount", new Object[0]);
                chatInput.awaitInput(player, ChatInputListener.InputType.TRANSFER_AMOUNT, amountStr -> {
                    double amount;
                    try {
                        amount = Double.parseDouble(amountStr.trim().replace(",", "."));
                    }
                    catch (final NumberFormatException e) {
                        lm.send(player, "invalid_amount", new Object[0]);
                        return;
                    }
                    if (amount <= 0.0) {
                        lm.send(player, "invalid_amount", new Object[0]);
                    }
                    else if (amount < minTransfer) {
                        lm.send(player, "transfer_min_amount", "%min%", String.format("%.2f", minTransfer));
                    }
                    else {
                        final BankAccount senderAcc = this.plugin.getAccountManager().getPrimaryAccount(senderUUID);
                        if (senderAcc == null || senderAcc.getBalance() < amount) {
                            lm.send(player, "transfer_insufficient", "%balance%", String.format("%.2f", (senderAcc != null) ? senderAcc.getBalance() : 0.0));
                        }
                        else {
                            final BankAccount receiverAcc = this.plugin.getAccountManager().getPrimaryAccount(target.getUniqueId());
                            if (receiverAcc == null) {
                                lm.send(player, "transfer_no_account", "%player%", target.getName());
                            }
                            else {
                                senderAcc.withdraw(amount);
                                receiverAcc.deposit(amount);
                                this.plugin.getAccountManager().saveData();
                                lm.send(player, "transfer_success_sender", "%amount%", String.format("%.2f", amount), "%player%", target.getName());
                                lm.send(target, "transfer_success_receiver", "%amount%", String.format("%.2f", amount), "%player%", player.getName());
                            }
                        }
                    }
                });
            }
        });
    }
    
    private void handleLoanTake(final Player player) {
        final UUID uuid = player.getUniqueId();
        final LanguageManager lm = this.plugin.getLanguageManager();
        if (!this.plugin.getConfigManager().isLoanEnabled()) {
            lm.send(player, "loan_not_enabled", new Object[0]);
            return;
        }
        final PlayerData pd = this.plugin.getAccountManager().getPlayerData(uuid);
        if (pd == null || pd.getLevel() <= 0) {
            lm.send(player, "loan_no_level", new Object[0]);
            return;
        }
        final BankAccount acc = pd.getPrimaryAccount();
        if (acc == null) {
            lm.send(player, "no_account", new Object[0]);
            return;
        }
        if (acc.getLoan() > 0.0) {
            lm.send(player, "loan_already_has", "%loan%", String.format("%.2f", acc.getLoan()));
            return;
        }
        final double loanLimit = this.plugin.getAccountManager().getLoanLimit(uuid);
        final double interestPct = this.plugin.getConfigManager().getInterestPercent();
        lm.send(player, "loan_prompt", new Object[0]);
        this.plugin.getChatInputListener().awaitInput(player, ChatInputListener.InputType.LOAN_TAKE, input -> {
            double amount;
            try {
                amount = Double.parseDouble(input.trim().replace(",", "."));
            }
            catch (final NumberFormatException e) {
                lm.send(player, "invalid_amount", new Object[0]);
                return;
            }
            if (amount <= 0.0) {
                lm.send(player, "invalid_amount", new Object[0]);
            }
            else if (amount > loanLimit) {
                lm.send(player, "loan_exceeds_limit", "%limit%", String.format("%.2f", loanLimit));
            }
            else {
                final double withInterest = amount * (1.0 + interestPct / 100.0);
                acc.setLoan(withInterest);
                acc.deposit(amount);
                this.plugin.getAccountManager().saveData();
                lm.send(player, "loan_success", "%amount%", String.format("%.2f", amount));
            }
        });
    }
    
    private void handleLoanRepay(final Player player) {
        final UUID uuid = player.getUniqueId();
        final LanguageManager lm = this.plugin.getLanguageManager();
        final BankAccount acc = this.plugin.getAccountManager().getPrimaryAccount(uuid);
        if (acc == null) {
            lm.send(player, "no_account", new Object[0]);
            return;
        }
        if (acc.getLoan() <= 0.0) {
            lm.send(player, "loan_no_active", new Object[0]);
            return;
        }
        lm.send(player, "loan_repay_prompt", new Object[0]);
        this.plugin.getChatInputListener().awaitInput(player, ChatInputListener.InputType.LOAN_REPAY, input -> {
            double amount;
            try {
                amount = Double.parseDouble(input.trim().replace(",", "."));
            }
            catch (final NumberFormatException e) {
                lm.send(player, "invalid_amount", new Object[0]);
                return;
            }
            if (amount <= 0.0) {
                lm.send(player, "invalid_amount", new Object[0]);
            }
            else if (amount > acc.getLoan()) {
                lm.send(player, "loan_repay_excess", "%loan%", String.format("%.2f", acc.getLoan()));
            }
            else if (acc.getBalance() < amount) {
                lm.send(player, "loan_repay_insufficient_balance", "%balance%", String.format("%.2f", acc.getBalance()));
            }
            else {
                acc.withdraw(amount);
                final double remaining = acc.getLoan() - amount;
                acc.setLoan(Math.max(0.0, remaining));
                this.plugin.getAccountManager().saveData();
                lm.send(player, "loan_repay_success", "%amount%", String.format("%.2f", amount), "%remaining%", String.format("%.2f", acc.getLoan()));
            }
        });
    }
    
    private void handleUpgrade(final Player player) {
        final UUID uuid = player.getUniqueId();
        final LanguageManager lm = this.plugin.getLanguageManager();
        final PlayerData pd = this.plugin.getAccountManager().getPlayerData(uuid);
        if (pd == null) {
            return;
        }
        final int currentLevel = pd.getBankLevel();
        final int maxLevel = this.plugin.getBankUpgradeManager().getMaxBankLevel();
        if (currentLevel >= maxLevel) {
            lm.send(player, "bank_upgrade_max", new Object[0]);
            return;
        }
        final int nextLevel = currentLevel + 1;
        final double price = this.plugin.getBankUpgradeManager().getUpgradePrice(nextLevel);
        if (this.plugin.getVaultHook() == null || !this.plugin.getVaultHook().isEnabled()) {
            lm.send(player, "vault_not_found", new Object[0]);
            return;
        }
        final double balance = this.plugin.getVaultHook().getBalance(player);
        if (balance < price) {
            lm.send(player, "bank_upgrade_insufficient", "%price%", String.format("%.2f", price), "%balance%", String.format("%.2f", balance));
            return;
        }
        this.plugin.getVaultHook().withdraw(player, price);
        this.plugin.getAccountManager().upgradeBankLevel(uuid);
        lm.send(player, "bank_upgrade_success", "%level%", String.valueOf(nextLevel));
    }
    
    private void handleATMMenu(final Player player, final int slot) {
        final GUIManager gui = this.plugin.getGuiManager();
        if (slot == gui.getATMDepositSlot()) {
            player.closeInventory();
            this.handleATMDeposit(player);
        }
        else if (slot == gui.getATMWithdrawSlot()) {
            player.closeInventory();
            this.handleATMWithdraw(player);
        }
    }
    
    private void handleATMDeposit(final Player player) {
        final UUID uuid = player.getUniqueId();
        final double minDeposit = this.plugin.getConfigManager().getMinDeposit();
        this.plugin.getLanguageManager().send(player, "deposit_prompt", new Object[0]);
        this.plugin.getChatInputListener().awaitInput(player, ChatInputListener.InputType.DEPOSIT, input -> this.processDeposit(player, uuid, minDeposit, input));
    }
    
    private void handleATMWithdraw(final Player player) {
        final UUID uuid = player.getUniqueId();
        final double minWithdraw = this.plugin.getConfigManager().getMinWithdraw();
        this.plugin.getLanguageManager().send(player, "withdraw_prompt", new Object[0]);
        this.plugin.getChatInputListener().awaitInput(player, ChatInputListener.InputType.WITHDRAW, input -> this.processWithdraw(player, uuid, minWithdraw, input));
    }
    
    private void handlePINMenu(final Player player, final int slot, final ItemStack item) {
        final GUIManager gui = this.plugin.getGuiManager();
        final ATMListener atmListener = this.plugin.getAtmListener();
        final UUID uuid = player.getUniqueId();
        final Map<UUID, String> pinInput = atmListener.getPinInput();
        if (gui.isNumberButton(slot)) {
            final String digit = gui.getDigitForSlot(slot);
            if (digit == null) {
                return;
            }
            String current = pinInput.getOrDefault(uuid, "");
            if (current.length() < 4) {
                current += digit;
                pinInput.put(uuid, current);
                gui.openPINMenu(player, current);
            }
        }
        else if (slot == gui.getPINClearSlot()) {
            pinInput.put(uuid, "");
            gui.openPINMenu(player, "");
        }
        else if (slot == gui.getPINConfirmSlot()) {
            final String entered = pinInput.getOrDefault(uuid, "");
            if (entered.length() != 4) {
                player.sendMessage(this.plugin.getLanguageManager().get("pin_invalid_format"));
                return;
            }
            final String storedPin = this.plugin.getAccountManager().getPin(uuid);
            if (!entered.equals(storedPin)) {
                player.sendMessage(this.plugin.getLanguageManager().get("pin_incorrect"));
                pinInput.put(uuid, "");
                gui.openPINMenu(player, "");
                return;
            }
            atmListener.removeSession(uuid);
            this.plugin.getGuiManager().openATMMenu(player);
        }
    }
}
