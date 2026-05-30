// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.hooks;

import com.chkaduuu.mcbank.McBank;

public class CMIHook
{
    private final McBank plugin;
    private boolean enabled;
    
    public CMIHook(final McBank plugin) {
        this.enabled = false;
        this.plugin = plugin;
    }
    
    public void setup() {
        if (!this.plugin.getConfigManager().isCMIEnabled()) {
            return;
        }
        if (this.plugin.getServer().getPluginManager().getPlugin("CMI") != null) {
            this.enabled = true;
            this.plugin.getLogger().info("CMI hooked!");
        }
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
}
