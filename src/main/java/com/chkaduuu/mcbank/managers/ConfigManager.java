// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.managers;

import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import com.chkaduuu.mcbank.McBank;

public class ConfigManager
{
    private final McBank plugin;
    private FileConfiguration config;
    
    public ConfigManager(final McBank plugin) {
        this.plugin = plugin;
    }
    
    public void load() {
        final File configFile = new File(this.plugin.getDataFolder(), "files/config.yml");
        if (!configFile.exists()) {
            this.plugin.saveResource("files/config.yml", false);
        }
        this.config = (FileConfiguration)YamlConfiguration.loadConfiguration(configFile);
    }
    
    public FileConfiguration getConfig() {
        return this.config;
    }
    
    public String getLanguage() {
        return this.config.getString("language", "en");
    }
    
    public int getMaxAccountsPerPlayer() {
        return this.config.getInt("bank.max_accounts_per_player", 1);
    }
    
    public double getStartingBalance() {
        return this.config.getDouble("bank.starting_balance", 0.0);
    }
    
    public double getMinDeposit() {
        return this.config.getDouble("bank.min_deposit", 1.0);
    }
    
    public double getMinWithdraw() {
        return this.config.getDouble("bank.min_withdraw", 1.0);
    }
    
    public double getMinTransfer() {
        return this.config.getDouble("bank.min_transfer", 1.0);
    }
    
    public String getCurrencySymbol() {
        return this.config.getString("bank.currency_symbol", "$");
    }
    
    public String getCurrencyName() {
        return this.config.getString("bank.currency_name", "Coin");
    }
    
    public boolean isLoanEnabled() {
        return this.config.getBoolean("loan.enabled", true);
    }
    
    public double getInterestPercent() {
        return this.config.getDouble("loan.interest_percent", 5.0);
    }
    
    public double getDefaultLoanLimit() {
        return this.config.getDouble("loan.default_loan_limit", 10000.0);
    }
    
    public boolean isVaultEnabled() {
        return this.config.getBoolean("vault.enabled", true);
    }
    
    public boolean isEssentialsEnabled() {
        return this.config.getBoolean("essentialsx.enabled", true);
    }
    
    public boolean isCMIEnabled() {
        return this.config.getBoolean("cmi.enabled", true);
    }
    
    public boolean isPinRequired() {
        return this.config.getBoolean("atm.pin_required", true);
    }
    
    public String getHologramText() {
        return this.config.getString("atm.hologram_text", "&6&lBank ATM");
    }
    
    public double getHologramHeight() {
        return this.config.getDouble("atm.hologram_height", 1.5);
    }
    
    public String getBankIncomeTime() {
        return this.config.getString("bank-income-time", "5m");
    }
    
    public int getMaxBankLevel() {
        return this.config.getInt("max-bank-level", 3);
    }
    
    public List<String> getAccountFormats() {
        return this.config.getStringList("account_formats");
    }
    
    public double getLoanPenaltyPercent() {
        return this.config.getDouble("loan.penalty_percent", 10.0);
    }

    public int getAtmDailyLimit() {
        return this.config.getInt("atm.daily_limit", 100000);
    }

    public boolean isDebug() {
        return this.config.getBoolean("debug", false);
    }
}
