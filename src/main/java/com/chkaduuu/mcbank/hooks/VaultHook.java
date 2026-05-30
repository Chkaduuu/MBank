// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.hooks;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import com.chkaduuu.mcbank.McBank;

public class VaultHook
{
    private final McBank plugin;
    private Economy economy;
    private boolean enabled;
    
    public VaultHook(final McBank plugin) {
        this.enabled = false;
        this.plugin = plugin;
    }
    
    public void setup() {
        if (!this.plugin.getConfigManager().isVaultEnabled()) {
            return;
        }
        if (this.plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        final RegisteredServiceProvider<Economy> rsp = (RegisteredServiceProvider<Economy>)this.plugin.getServer().getServicesManager().getRegistration((Class)Economy.class);
        if (rsp != null) {
            this.economy = (Economy)rsp.getProvider();
            this.enabled = true;
            this.plugin.getLogger().info("Vault hooked!");
        }
        else {
            this.plugin.getLogger().warning("Vault found but no economy provider registered!");
        }
    }
    
    public boolean isEnabled() {
        return this.enabled && this.economy != null;
    }
    
    public double getBalance(final Player player) {
        if (!this.isEnabled()) {
            return 0.0;
        }
        return this.economy.getBalance((OfflinePlayer)player);
    }
    
    public boolean has(final Player player, final double amount) {
        return this.isEnabled() && this.economy.has((OfflinePlayer)player, amount);
    }
    
    public void withdraw(final Player player, final double amount) {
        if (!this.isEnabled()) {
            return;
        }
        this.economy.withdrawPlayer((OfflinePlayer)player, amount);
    }
    
    public void deposit(final Player player, final double amount) {
        if (!this.isEnabled()) {
            return;
        }
        this.economy.depositPlayer((OfflinePlayer)player, amount);
    }
}
