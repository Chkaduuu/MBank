// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.managers;

import java.util.regex.Matcher;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.FileConfiguration;
import com.chkaduuu.mcbank.McBank;

public class LanguageManager
{
    private final McBank plugin;
    private FileConfiguration lang;
    private static final Pattern HEX_PATTERN;
    
    public LanguageManager(final McBank plugin) {
        this.plugin = plugin;
    }
    
    public void load() {
        final String language = this.plugin.getConfigManager().getLanguage();
        final File langFile = new File(this.plugin.getDataFolder(), "languages/" + language + ".yml");
        if (!langFile.exists()) {
            this.plugin.saveResource("languages/" + language + ".yml", false);
        }
        this.lang = (FileConfiguration)YamlConfiguration.loadConfiguration(langFile);
    }
    
    public String getRaw(final String key) {
        return this.lang.getString("messages." + key, "&c[Missing message: " + key);
    }
    
    public String get(final String key) {
        return colorize(this.getRaw(key));
    }
    
    public String getFormatted(final String key, final Object... replacements) {
        String msg = this.getRaw(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
        }
        return colorize(msg);
    }
    
    public void send(final Player player, final String key, final Object... replacements) {
        player.sendMessage(this.getFormatted(key, replacements));
    }
    
    public static String colorize(final String text) {
        if (text == null) {
            return "";
        }
        final Matcher matcher = LanguageManager.HEX_PATTERN.matcher(text);
        final StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            final String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
    
    static {
        HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    }
}
