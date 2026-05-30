// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.atm;

import java.io.IOException;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Collection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import com.chkaduuu.mcbank.McBank;

public class ATMManager
{
    private final McBank plugin;
    private File atmFile;
    private FileConfiguration atmConfig;
    private final Set<String> atmLocations;
    
    public ATMManager(final McBank plugin) {
        this.atmLocations = new HashSet<String>();
        this.plugin = plugin;
    }
    
    public void load() {
        this.atmFile = new File(this.plugin.getDataFolder(), "files/atm.yml");
        if (!this.atmFile.exists()) {
            this.plugin.saveResource("files/atm.yml", false);
        }
        this.atmConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(this.atmFile);
        this.loadATMs();
    }
    
    private void loadATMs() {
        this.atmLocations.clear();
        final ConfigurationSection atms = this.atmConfig.getConfigurationSection("atms");
        if (atms == null) {
            return;
        }
        this.atmLocations.addAll(atms.getKeys(false));
    }
    
    public boolean isATM(final Block block) {
        return this.atmLocations.contains(this.blockKey(block));
    }
    
    public void addATM(final Block block, final String placedBy) {
        final String key = this.blockKey(block);
        this.atmLocations.add(key);
        this.atmConfig.set("atms." + key + ".world", (Object)block.getWorld().getName());
        this.atmConfig.set("atms." + key + ".x", (Object)block.getX());
        this.atmConfig.set("atms." + key + ".y", (Object)block.getY());
        this.atmConfig.set("atms." + key + ".z", (Object)block.getZ());
        this.atmConfig.set("atms." + key + ".placed_by", (Object)placedBy);
        this.atmConfig.set("atms." + key + ".placed_at", (Object)System.currentTimeMillis());
        this.save();
    }
    
    public void removeATM(final Block block) {
        final String key = this.blockKey(block);
        this.atmLocations.remove(key);
        this.atmConfig.set("atms." + key, (Object)null);
        this.save();
    }
    
    public void save() {
        try {
            this.atmConfig.save(this.atmFile);
        }
        catch (final IOException e) {
            this.plugin.getLogger().severe("Failed to save atm.yml: " + e.getMessage());
        }
    }
    
    private String blockKey(final Block block) {
        return block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ();
    }
}
