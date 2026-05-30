// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.listeners;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import com.chkaduuu.mcbank.McBank;
import org.bukkit.event.Listener;

public class ChatInputListener implements Listener
{
    private final McBank plugin;
    private final Map<UUID, PendingInput> pendingInputs;
    
    public ChatInputListener(final McBank plugin) {
        this.pendingInputs = new HashMap<UUID, PendingInput>();
        this.plugin = plugin;
    }
    
    public void awaitInput(final Player player, final InputType type, final Consumer<String> callback) {
        final PendingInput pending = new PendingInput(type, callback);
        this.pendingInputs.put(player.getUniqueId(), pending);
        player.sendMessage(this.plugin.getLanguageManager().get("chat_input_type"));
        this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> {
            if (this.pendingInputs.containsKey(player.getUniqueId())) {
                this.pendingInputs.remove(player.getUniqueId());
                this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> player.sendMessage(this.plugin.getLanguageManager().get("chat_input_timeout")));
            }
        }, 600L);
    }
    
    public boolean hasPendingInput(final UUID uuid) {
        return this.pendingInputs.containsKey(uuid);
    }
    
    public PendingInput getPending(final UUID uuid) {
        return this.pendingInputs.get(uuid);
    }
    
    public void setPendingExtra(final UUID uuid, final String extra) {
        final PendingInput pending = this.pendingInputs.get(uuid);
        if (pending != null) {
            pending.extra = extra;
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (!this.pendingInputs.containsKey(uuid)) {
            return;
        }
        event.setCancelled(true);
        final String input = event.getMessage().trim();
        if (input.equalsIgnoreCase("cancel")) {
            this.pendingInputs.remove(uuid);
            this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> player.sendMessage(this.plugin.getLanguageManager().get("transfer_cancelled")));
            return;
        }
        final PendingInput pending = this.pendingInputs.remove(uuid);
        this.plugin.getServer().getScheduler().runTask((Plugin)this.plugin, () -> {
            if (pending != null) {
                pending.callback.accept(input);
            }
        });
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.pendingInputs.remove(event.getPlayer().getUniqueId());
    }
    
    public enum InputType
    {
        DEPOSIT, 
        WITHDRAW, 
        CASHOUT, 
        TRANSFER_PLAYER, 
        TRANSFER_AMOUNT, 
        LOAN_TAKE, 
        LOAN_REPAY;
    }
    
    public static class PendingInput
    {
        public final InputType type;
        public final Consumer<String> callback;
        public String extra;
        
        public PendingInput(final InputType type, final Consumer<String> callback) {
            this.type = type;
            this.callback = callback;
        }
    }
}
