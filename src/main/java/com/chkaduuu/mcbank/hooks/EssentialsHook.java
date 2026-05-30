// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.hooks;

import com.chkaduuu.mcbank.McBank;

public class EssentialsHook
{
    private final McBank plugin;
    private boolean enabled;
    
    public EssentialsHook(final McBank plugin) {
        this.enabled = false;
        this.plugin = plugin;
    }
    
    public void setup() {
        if (!this.plugin.getConfigManager().isEssentialsEnabled()) {
            return;
        }
        if (this.plugin.getServer().getPluginManager().getPlugin("EssentialsX") != null) {
            this.enabled = true;
            this.plugin.getLogger().info("EssentialsX hooked!");
        }
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
}
