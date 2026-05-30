// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.hooks;

import com.chkaduuu.mcbank.models.BankAccount;
import com.chkaduuu.mcbank.models.PlayerData;
import com.chkaduuu.mcbank.managers.BankUpgradeManager;
import com.chkaduuu.mcbank.managers.AccountManager;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.chkaduuu.mcbank.McBank;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceholderAPIHook extends PlaceholderExpansion
{
    private final McBank plugin;
    
    public PlaceholderAPIHook(final McBank plugin) {
        this.plugin = plugin;
    }
    
    @NotNull
    public String getIdentifier() {
        return "mcbank";
    }
    
    @NotNull
    public String getAuthor() {
        return "Chkaduuu";
    }
    
    @NotNull
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }
    
    public boolean persist() {
        return true;
    }
    
    public String onPlaceholderRequest(final Player player, @NotNull final String params) {
        if (player == null) {
            return "";
        }
        final UUID uuid = player.getUniqueId();
        final AccountManager am = this.plugin.getAccountManager();
        final BankUpgradeManager bum = this.plugin.getBankUpgradeManager();
        final PlayerData pd = am.getPlayerData(uuid);
        final BankAccount acc = am.getPrimaryAccount(uuid);
        final String lowerCase = params.toLowerCase();
        return switch (lowerCase) {
            case "bank_number" -> (acc != null) ? acc.getAccountNumber() : "N/A";
            case "bank_balance" -> (acc != null) ? String.format("%.2f", acc.getBalance()) : "0.00";
            case "bank_limit" -> String.format("%.2f", am.getBankLimit(uuid));
            case "bank_level" -> (pd != null) ? String.valueOf(pd.getBankLevel()) : "1";
            case "bank_loan" -> (acc != null) ? String.format("%.2f", acc.getLoan()) : "0.00";
            case "bank_loan_limit" -> String.format("%.2f", am.getLoanLimit(uuid));
            case "loan_percent" -> String.valueOf(this.plugin.getConfigManager().getInterestPercent());
            case "income" -> (pd != null) ? ("" + bum.getOnlineIncomeRate(pd.getBankLevel())) : "0%";
            case "offline_income" -> (pd != null) ? ("" + bum.getOfflineIncomeRate(pd.getBankLevel())) : "0%";
            case "time_income" -> {
                final long ticks = am.getIncomeTaskTimeRemaining();
                yield (ticks > 0L) ? ("" + ticks / 20L) : "N/A";
            }
            default -> null;
        };
    }
}
