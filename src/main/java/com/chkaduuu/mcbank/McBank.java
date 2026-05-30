// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank;

import com.chkaduuu.mcbank.hooks.PlaceholderAPIHook;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import com.chkaduuu.mcbank.commands.McBankCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import com.chkaduuu.mcbank.listeners.ChatInputListener;
import com.chkaduuu.mcbank.listeners.ATMListener;
import com.chkaduuu.mcbank.listeners.GUIListener;
import com.chkaduuu.mcbank.hooks.CMIHook;
import com.chkaduuu.mcbank.hooks.EssentialsHook;
import com.chkaduuu.mcbank.hooks.VaultHook;
import com.chkaduuu.mcbank.atm.ATMManager;
import com.chkaduuu.mcbank.managers.BankUpgradeManager;
import com.chkaduuu.mcbank.managers.GUIManager;
import com.chkaduuu.mcbank.managers.AccountManager;
import com.chkaduuu.mcbank.managers.LanguageManager;
import com.chkaduuu.mcbank.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class McBank extends JavaPlugin
{
    private static McBank instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private AccountManager accountManager;
    private GUIManager guiManager;
    private BankUpgradeManager bankUpgradeManager;
    private ATMManager atmManager;
    private VaultHook vaultHook;
    private EssentialsHook essentialsHook;
    private CMIHook cmiHook;
    private GUIListener guiListener;
    private ATMListener atmListener;
    private ChatInputListener chatInputListener;
    
    public void onEnable() {
        McBank.instance = this;
        (this.configManager = new ConfigManager(this)).load();
        (this.languageManager = new LanguageManager(this)).load();
        (this.accountManager = new AccountManager(this)).load();
        (this.bankUpgradeManager = new BankUpgradeManager(this)).load();
        (this.atmManager = new ATMManager(this)).load();
        (this.vaultHook = new VaultHook(this)).setup();
        (this.essentialsHook = new EssentialsHook(this)).setup();
        (this.cmiHook = new CMIHook(this)).setup();
        this.guiManager = new GUIManager(this);
        this.chatInputListener = new ChatInputListener(this);
        this.getServer().getPluginManager().registerEvents((Listener)this.chatInputListener, (Plugin)this);
        this.guiListener = new GUIListener(this);
        this.getServer().getPluginManager().registerEvents((Listener)this.guiListener, (Plugin)this);
        this.atmListener = new ATMListener(this);
        this.getServer().getPluginManager().registerEvents((Listener)this.atmListener, (Plugin)this);
        final McBankCommand cmd = new McBankCommand(this);
        this.getCommand("mcbank").setExecutor((CommandExecutor)cmd);
        this.getCommand("mcbank").setTabCompleter((TabCompleter)cmd);
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            this.getLogger().info("PlaceholderAPI hooked!");
        }
        this.accountManager.startIncomeScheduler();
        this.getLogger().info("McUltimateBank v" + this.getDescription().getVersion() + " enabled!");
    }
    
    public void onDisable() {
        if (this.accountManager != null) {
            this.accountManager.saveData();
        }
        if (this.atmManager != null) {
            this.atmManager.save();
        }
        this.getLogger().info("McUltimateBank disabled!");
    }
    
    public void reload() {
        this.configManager.load();
        this.languageManager.load();
        this.bankUpgradeManager.load();
        this.guiManager.reload();
        this.accountManager.stopIncomeScheduler();
        this.accountManager.saveData();
        this.accountManager.load();
        this.accountManager.startIncomeScheduler();
    }
    
    public static McBank getInstance() {
        return McBank.instance;
    }
    
    public ConfigManager getConfigManager() {
        return this.configManager;
    }
    
    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }
    
    public AccountManager getAccountManager() {
        return this.accountManager;
    }
    
    public GUIManager getGuiManager() {
        return this.guiManager;
    }
    
    public BankUpgradeManager getBankUpgradeManager() {
        return this.bankUpgradeManager;
    }
    
    public ATMManager getAtmManager() {
        return this.atmManager;
    }
    
    public VaultHook getVaultHook() {
        return this.vaultHook;
    }
    
    public EssentialsHook getEssentialsHook() {
        return this.essentialsHook;
    }
    
    public CMIHook getCmiHook() {
        return this.cmiHook;
    }
    
    public GUIListener getGuiListener() {
        return this.guiListener;
    }
    
    public ATMListener getAtmListener() {
        return this.atmListener;
    }
    
    public ChatInputListener getChatInputListener() {
        return this.chatInputListener;
    }
    
    public ChatInputListener getChatInput() {
        return this.chatInputListener;
    }
}
