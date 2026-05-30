package com.chkaduuu.mcbank.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import com.chkaduuu.mcbank.atm.ATMManager;
import com.chkaduuu.mcbank.models.PlayerData;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.HashMap;
import org.bukkit.block.Block;
import java.util.UUID;
import java.util.Map;
import com.chkaduuu.mcbank.McBank;
import org.bukkit.event.Listener;

public class ATMListener implements Listener
{
    private final McBank plugin;
    private final Map<UUID, String> pinInput;
    private final Map<UUID, Block> atmSessionBlock;

    public ATMListener(final McBank plugin) {
        this.pinInput = new HashMap<>();
        this.atmSessionBlock = new HashMap<>();
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final Block block = event.getClickedBlock();
        if (block == null) return;

        final ATMManager atmManager = this.plugin.getAtmManager();
        if (!atmManager.isATM(block)) return;

        event.setCancelled(true);
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        if (!this.plugin.getAccountManager().hasAccount(uuid)) {
            this.plugin.getLanguageManager().send(player, "no_account");
            return;
        }

        // Проверка лимита операций в день
        final PlayerData pd = this.plugin.getAccountManager().getPlayerData(uuid);
        if (pd != null) {
            final int maxOps = this.plugin.getConfigManager().getAtmDailyLimit();
            if (maxOps > 0 && pd.getAtmOperationsToday() >= maxOps) {
                this.plugin.getLanguageManager().send(player, "atm_daily_limit_reached",
                    "%limit%", String.valueOf(maxOps));
                return;
            }
        }

        if (this.plugin.getConfigManager().isPinRequired()) {
            final String pin = this.plugin.getAccountManager().getPin(uuid);
            if (pin == null) {
                this.plugin.getLanguageManager().send(player, "pin_required");
                return;
            }

            // Проверка блокировки PIN
            if (pd != null && pd.isPinLocked()) {
                this.plugin.getLanguageManager().send(player, "pin_locked");
                return;
            }

            this.pinInput.put(uuid, "");
            this.atmSessionBlock.put(uuid, block);
            this.plugin.getGuiManager().openPINMenu(player, "");
        } else {
            this.atmSessionBlock.put(uuid, block);
            this.plugin.getGuiManager().openATMMenu(player);
        }
    }

    /**
     * Вызывается из GUIListener после ввода PIN.
     * Возвращает true если PIN верный, false если нет.
     */
    public boolean validatePin(final Player player, final String enteredPin) {
        final UUID uuid = player.getUniqueId();
        final PlayerData pd = this.plugin.getAccountManager().getPlayerData(uuid);
        final String correctPin = this.plugin.getAccountManager().getPin(uuid);

        if (pd == null || correctPin == null) return false;

        if (correctPin.equals(enteredPin)) {
            pd.resetPinFailed();
            // Засчитать операцию
            pd.incrementAtmOperations();
            this.plugin.getAccountManager().saveData();
            return true;
        } else {
            pd.incrementPinFailed();
            this.plugin.getAccountManager().saveData();

            if (pd.isPinLocked()) {
                this.plugin.getLanguageManager().send(player, "pin_locked");
            } else {
                final int remaining = 3 - pd.getPinFailedAttempts();
                this.plugin.getLanguageManager().send(player, "pin_incorrect_attempts",
                    "%remaining%", String.valueOf(remaining));
            }
            return false;
        }
    }

    public Map<UUID, String> getPinInput() { return this.pinInput; }

    public Block getAtmSessionBlock(final UUID uuid) { return this.atmSessionBlock.get(uuid); }

    public void setAtmSessionBlock(final UUID uuid, final Block block) {
        this.atmSessionBlock.put(uuid, block);
    }

    public void removeSession(final UUID uuid) {
        this.pinInput.remove(uuid);
        this.atmSessionBlock.remove(uuid);
    }
}
