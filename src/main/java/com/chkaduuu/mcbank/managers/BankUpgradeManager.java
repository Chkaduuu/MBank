// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.managers;

import java.util.Iterator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import com.chkaduuu.mcbank.McBank;

public class BankUpgradeManager
{
    private final McBank plugin;
    private FileConfiguration upgradeConfig;
    private FileConfiguration levelConfig;
    private final Map<Integer, Double> limitMap;
    private final Map<Integer, Double> priceMap;
    private final Map<Integer, Double> onlineIncomeMap;
    private final Map<Integer, Double> offlineIncomeMap;
    private final Map<Integer, Double> loanLimitMap;
    
    public BankUpgradeManager(final McBank plugin) {
        this.limitMap = new HashMap<Integer, Double>();
        this.priceMap = new HashMap<Integer, Double>();
        this.onlineIncomeMap = new HashMap<Integer, Double>();
        this.offlineIncomeMap = new HashMap<Integer, Double>();
        this.loanLimitMap = new HashMap<Integer, Double>();
        this.plugin = plugin;
    }
    
    public void load() {
        this.limitMap.clear();
        this.priceMap.clear();
        this.onlineIncomeMap.clear();
        this.offlineIncomeMap.clear();
        this.loanLimitMap.clear();
        final File upgradeFile = new File(this.plugin.getDataFolder(), "files/upgrade.yml");
        if (!upgradeFile.exists()) {
            this.plugin.saveResource("files/upgrade.yml", false);
        }
        this.upgradeConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(upgradeFile);
        final File levelFile = new File(this.plugin.getDataFolder(), "files/level.yml");
        if (!levelFile.exists()) {
            this.plugin.saveResource("files/level.yml", false);
        }
        this.levelConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(levelFile);
        final ConfigurationSection bankLevels = this.upgradeConfig.getConfigurationSection("bank-levels");
        if (bankLevels != null) {
            for (final String key : bankLevels.getKeys(false)) {
                try {
                    final int level = Integer.parseInt(key);
                    final ConfigurationSection sec = bankLevels.getConfigurationSection(key);
                    if (sec == null) {
                        continue;
                    }
                    final String limitStr = sec.getString("limit", "10000");
                    this.limitMap.put(level, this.parseNumber(limitStr));
                    if (sec.contains("price")) {
                        this.priceMap.put(level, sec.getDouble("price", 0.0));
                    }
                    final String incomeStr = sec.getString("income", "0%");
                    this.onlineIncomeMap.put(level, this.parsePercent(incomeStr));
                    final String offlineStr = sec.getString("offline-income", "0%");
                    this.offlineIncomeMap.put(level, this.parsePercent(offlineStr));
                }
                catch (final NumberFormatException ex) {}
            }
        }
        final ConfigurationSection loanLevels = this.levelConfig.getConfigurationSection("loan-levels");
        if (loanLevels != null) {
            for (final String key2 : loanLevels.getKeys(false)) {
                try {
                    final int level2 = Integer.parseInt(key2);
                    final ConfigurationSection sec2 = loanLevels.getConfigurationSection(key2);
                    if (sec2 == null) {
                        continue;
                    }
                    final String loanStr = sec2.getString("loan", "0");
                    this.loanLimitMap.put(level2, this.parseNumber(loanStr));
                }
                catch (final NumberFormatException ex2) {}
            }
        }
    }
    
    private double parseNumber(final String str) {
        if (str == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(str.replace(",", ""));
        }
        catch (final NumberFormatException e) {
            return 0.0;
        }
    }
    
    private double parsePercent(String str) {
        if (str == null) {
            return 0.0;
        }
        str = str.replace("%", "").trim();
        try {
            return Double.parseDouble(str);
        }
        catch (final NumberFormatException e) {
            return 0.0;
        }
    }
    
    public double getLimit(final int bankLevel) {
        return this.limitMap.getOrDefault(bankLevel, 10000.0);
    }
    
    public double getUpgradePrice(final int nextLevel) {
        return this.priceMap.getOrDefault(nextLevel, 0.0);
    }
    
    public double getOnlineIncomeRate(final int bankLevel) {
        return this.onlineIncomeMap.getOrDefault(bankLevel, 0.0);
    }
    
    public double getOfflineIncomeRate(final int bankLevel) {
        return this.offlineIncomeMap.getOrDefault(bankLevel, 0.0);
    }
    
    public double getLoanLimit(final int loanLevel) {
        if (loanLevel <= 0) {
            return 0.0;
        }
        return this.loanLimitMap.getOrDefault(loanLevel, this.plugin.getConfigManager().getDefaultLoanLimit());
    }
    
    public int getMaxBankLevel() {
        return this.plugin.getConfigManager().getMaxBankLevel();
    }
}
