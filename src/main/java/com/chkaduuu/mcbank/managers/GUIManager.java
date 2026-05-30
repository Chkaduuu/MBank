// 
// Decompiled by Procyon v0.6.0
// 

package com.chkaduuu.mcbank.managers;

import org.bukkit.profile.PlayerProfile;
import java.net.URL;
import java.util.Base64;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.enchantments.Enchantment;
import java.util.ArrayList;
import org.bukkit.Material;
import java.util.Iterator;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import com.chkaduuu.mcbank.models.BankAccount;
import com.chkaduuu.mcbank.models.PlayerData;
import java.util.UUID;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;
import com.chkaduuu.mcbank.McBank;

public class GUIManager
{
    private final McBank plugin;
    private FileConfiguration mainGuiConfig;
    private FileConfiguration atmGuiConfig;
    private String mainMenuTitle;
    private int mainMenuRows;
    private String atmMenuTitle;
    private int atmMenuRows;
    
    public GUIManager(final McBank plugin) {
        this.plugin = plugin;
        this.reload();
    }
    
    public void reload() {
        final File mainFile = new File(this.plugin.getDataFolder(), "files/gui_main.yml");
        if (!mainFile.exists()) {
            this.plugin.saveResource("files/gui_main.yml", false);
        }
        this.mainGuiConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(mainFile);
        final File atmFile = new File(this.plugin.getDataFolder(), "files/gui_atm.yml");
        if (!atmFile.exists()) {
            this.plugin.saveResource("files/gui_atm.yml", false);
        }
        this.atmGuiConfig = (FileConfiguration)YamlConfiguration.loadConfiguration(atmFile);
        this.mainMenuTitle = LanguageManager.colorize(this.mainGuiConfig.getString("main_menu.title", "&0&lPersonal Bank"));
        this.mainMenuRows = this.mainGuiConfig.getInt("main_menu.rows", 6);
        this.atmMenuTitle = LanguageManager.colorize(this.atmGuiConfig.getString("atm_menu.title", "&0&lBank ATM"));
        this.atmMenuRows = this.atmGuiConfig.getInt("atm_menu.rows", 3);
    }
    
    public boolean isMainMenu(final String title) {
        return title.equals(this.mainMenuTitle);
    }
    
    public boolean isATMMenu(final String title) {
        return title.equals(this.atmMenuTitle);
    }
    
    public boolean isPINMenu(final String title) {
        final String pinTitle = LanguageManager.colorize(this.atmGuiConfig.getString("pin_menu.title", "&0&lEnter PIN"));
        return title.equals(pinTitle);
    }
    
    public int getAccountInfoSlot() {
        return this.mainGuiConfig.getInt("main_menu.account_info.slot", 22);
    }
    
    public int getDepositSlot() {
        return this.mainGuiConfig.getInt("main_menu.deposit.slot", 20);
    }
    
    public int getWithdrawSlot() {
        return this.mainGuiConfig.getInt("main_menu.withdraw.slot", 24);
    }
    
    public int getCashoutSlot() {
        return this.mainGuiConfig.getInt("main_menu.cashout.slot", 29);
    }
    
    public int getTransferSlot() {
        return this.mainGuiConfig.getInt("main_menu.transfer.slot", 31);
    }
    
    public int getLoanSlot() {
        return this.mainGuiConfig.getInt("main_menu.loan.slot", 33);
    }
    
    public int getUpgradeSlot() {
        return this.mainGuiConfig.getInt("main_menu.upgrade.slot", 38);
    }
    
    public int getATMDepositSlot() {
        return this.atmGuiConfig.getInt("atm_menu.deposit.slot", 11);
    }
    
    public int getATMWithdrawSlot() {
        return this.atmGuiConfig.getInt("atm_menu.withdraw.slot", 15);
    }
    
    public void openMainMenu(final Player player) {
        final UUID uuid = player.getUniqueId();
        final PlayerData pd = this.plugin.getAccountManager().getPlayerData(uuid);
        final BankAccount acc = (pd != null) ? pd.getPrimaryAccount() : null;
        final int rows = this.mainMenuRows;
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, rows * 9, this.mainMenuTitle);
        final ConfigurationSection filler = this.mainGuiConfig.getConfigurationSection("main_menu.filler");
        if (filler != null && filler.getBoolean("enabled", true)) {
            final ItemStack fillerItem = this.buildSimpleItem(filler.getString("item", "GRAY_STAINED_GLASS_PANE"), filler.getString("name", " "));
            for (int i = 0; i < rows * 9; ++i) {
                inv.setItem(i, fillerItem);
            }
        }
        final ConfigurationSection border = this.mainGuiConfig.getConfigurationSection("main_menu.border");
        if (border != null && border.getBoolean("enabled", true)) {
            final ItemStack borderItem = this.buildSimpleItem(border.getString("item", "BLACK_STAINED_GLASS_PANE"), border.getString("name", " "));
            final List<Integer> slots = border.getIntegerList("slots");
            for (final int slot : slots) {
                if (slot < rows * 9) {
                    inv.setItem(slot, borderItem);
                }
            }
        }
        final String bankNumber = (acc != null) ? acc.getAccountNumber() : "N/A";
        final String balance = (acc != null) ? String.format("%.2f", acc.getBalance()) : "0.00";
        final String limit = String.format("%.2f", this.plugin.getAccountManager().getBankLimit(uuid));
        final String bankLevel = (pd != null) ? String.valueOf(pd.getBankLevel()) : "1";
        final String loan = (acc != null) ? String.format("%.2f", acc.getLoan()) : "0.00";
        final String loanLimit = String.format("%.2f", this.plugin.getAccountManager().getLoanLimit(uuid));
        final String interestPct = String.valueOf(this.plugin.getConfigManager().getInterestPercent());
        final String minDeposit = String.format("%.2f", this.plugin.getConfigManager().getMinDeposit());
        final String minWithdraw = String.format("%.2f", this.plugin.getConfigManager().getMinWithdraw());
        final String minTransfer = String.format("%.2f", this.plugin.getConfigManager().getMinTransfer());
        final int nextBankLevel = (pd != null) ? (pd.getBankLevel() + 1) : 2;
        final double upgradePrice = this.plugin.getBankUpgradeManager().getUpgradePrice(nextBankLevel);
        final ConfigurationSection accInfo = this.mainGuiConfig.getConfigurationSection("main_menu.account_info");
        if (accInfo != null) {
            final int slot2 = accInfo.getInt("slot", 22);
            final String itemStr = accInfo.getString("item", "PLAYER_HEAD");
            ItemStack item;
            if (itemStr.equalsIgnoreCase("basehead-%player_name%")) {
                item = this.buildPlayerHead(player);
            }
            else {
                item = this.buildSimpleItem(itemStr, "");
            }
            this.applyMeta(item, accInfo, player.getName(), bankNumber, balance, limit, bankLevel, loan, loanLimit, interestPct, minDeposit, minWithdraw, minTransfer, String.format("%.2f", upgradePrice));
            inv.setItem(slot2, item);
        }
        this.setButton(inv, this.mainGuiConfig.getConfigurationSection("main_menu.deposit"), player.getName(), bankNumber, balance, limit, bankLevel, loan, loanLimit, interestPct, minDeposit, minWithdraw, minTransfer, String.format("%.2f", upgradePrice));
        this.setButton(inv, this.mainGuiConfig.getConfigurationSection("main_menu.withdraw"), player.getName(), bankNumber, balance, limit, bankLevel, loan, loanLimit, interestPct, minDeposit, minWithdraw, minTransfer, String.format("%.2f", upgradePrice));
        this.setButton(inv, this.mainGuiConfig.getConfigurationSection("main_menu.cashout"), player.getName(), bankNumber, balance, limit, bankLevel, loan, loanLimit, interestPct, minDeposit, minWithdraw, minTransfer, String.format("%.2f", upgradePrice));
        this.setButton(inv, this.mainGuiConfig.getConfigurationSection("main_menu.transfer"), player.getName(), bankNumber, balance, limit, bankLevel, loan, loanLimit, interestPct, minDeposit, minWithdraw, minTransfer, String.format("%.2f", upgradePrice));
        this.setButton(inv, this.mainGuiConfig.getConfigurationSection("main_menu.loan"), player.getName(), bankNumber, balance, limit, bankLevel, loan, loanLimit, interestPct, minDeposit, minWithdraw, minTransfer, String.format("%.2f", upgradePrice));
        this.setButton(inv, this.mainGuiConfig.getConfigurationSection("main_menu.upgrade"), player.getName(), bankNumber, balance, limit, bankLevel, loan, loanLimit, interestPct, minDeposit, minWithdraw, minTransfer, String.format("%.2f", upgradePrice));
        player.openInventory(inv);
    }
    
    private void setButton(final Inventory inv, final ConfigurationSection sec, final String... replacements) {
        if (sec == null) {
            return;
        }
        final int slot = sec.getInt("slot", 0);
        final String itemStr = sec.getString("item", "STONE");
        final ItemStack item = this.buildItemFromConfig(itemStr);
        this.applyMeta(item, sec, replacements);
        if (slot < inv.getSize()) {
            inv.setItem(slot, item);
        }
    }
    
    private ItemStack buildItemFromConfig(final String itemStr) {
        if (itemStr == null) {
            return new ItemStack(Material.STONE);
        }
        if (itemStr.startsWith("basehead-")) {
            final String data = itemStr.substring(9);
            return this.buildCustomSkull(data);
        }
        try {
            final Material mat = Material.valueOf(itemStr.toUpperCase());
            return new ItemStack(mat);
        }
        catch (final IllegalArgumentException e) {
            return new ItemStack(Material.STONE);
        }
    }
    
    private void applyMeta(final ItemStack item, final ConfigurationSection sec, final String... replacements) {
        if (item == null || sec == null) {
            return;
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        String name = sec.getString("name", "");
        name = this.applyReplacements(name, replacements);
        meta.setDisplayName(LanguageManager.colorize(name));
        final List<String> lore = sec.getStringList("lore");
        final List<String> coloredLore = new ArrayList<String>();
        for (final String line : lore) {
            coloredLore.add(LanguageManager.colorize(this.applyReplacements(line, replacements)));
        }
        if (!lore.isEmpty()) {
            meta.setLore((List)coloredLore);
        }
        final boolean glow = sec.getBoolean("glow", false);
        if (glow) {
            meta.addEnchant(Enchantment.INFINITY, 1, true);
            meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
        }
        meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
        item.setItemMeta(meta);
    }
    
    private String applyReplacements(String text, final String... replacements) {
        if (replacements == null || replacements.length < 2) {
            return text;
        }
        if (replacements.length >= 12) {
            text = text.replace("%player_name%", replacements[0]);
            text = text.replace("%mcbank_bank_number%", replacements[1]);
            text = text.replace("%mcbank_bank_balance%", replacements[2]);
            text = text.replace("%mcbank_bank_limit%", replacements[3]);
            text = text.replace("%mcbank_bank_level%", replacements[4]);
            text = text.replace("%mcbank_bank_loan%", replacements[5]);
            text = text.replace("%mcbank_bank_loan_limit%", replacements[6]);
            text = text.replace("%mcbank_loan_percent%", replacements[7]);
            text = text.replace("%min_deposit%", replacements[8]);
            text = text.replace("%min_withdraw%", replacements[9]);
            text = text.replace("%min_transfer%", replacements[10]);
            text = text.replace("%bank_upgrade_price%", replacements[11]);
        }
        return text;
    }
    
    public void openATMMenu(final Player player) {
        final UUID uuid = player.getUniqueId();
        final int rows = this.atmMenuRows;
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, rows * 9, this.atmMenuTitle);
        final String limit = String.format("%.2f", this.plugin.getAccountManager().getBankLimit(uuid));
        final String minDeposit = String.format("%.2f", this.plugin.getConfigManager().getMinDeposit());
        final String minWithdraw = String.format("%.2f", this.plugin.getConfigManager().getMinWithdraw());
        final ConfigurationSection filler = this.atmGuiConfig.getConfigurationSection("atm_menu.filler");
        if (filler != null && filler.getBoolean("enabled", true)) {
            final ItemStack fillerItem = this.buildSimpleItem(filler.getString("item", "GRAY_STAINED_GLASS_PANE"), filler.getString("name", " "));
            for (int i = 0; i < rows * 9; ++i) {
                inv.setItem(i, fillerItem);
            }
        }
        final ConfigurationSection border = this.atmGuiConfig.getConfigurationSection("atm_menu.border");
        if (border != null && border.getBoolean("enabled", true)) {
            final ItemStack borderItem = this.buildSimpleItem(border.getString("item", "BLACK_STAINED_GLASS_PANE"), border.getString("name", " "));
            for (final int slot : border.getIntegerList("slots")) {
                if (slot < rows * 9) {
                    inv.setItem(slot, borderItem);
                }
            }
        }
        this.setATMButton(inv, this.atmGuiConfig.getConfigurationSection("atm_menu.deposit"), limit, minDeposit, minWithdraw);
        this.setATMButton(inv, this.atmGuiConfig.getConfigurationSection("atm_menu.withdraw"), limit, minDeposit, minWithdraw);
        player.openInventory(inv);
    }
    
    private void setATMButton(final Inventory inv, final ConfigurationSection sec, final String limit, final String minDeposit, final String minWithdraw) {
        if (sec == null) {
            return;
        }
        final int slot = sec.getInt("slot", 0);
        final String itemStr = sec.getString("item", "STONE");
        final ItemStack item = this.buildItemFromConfig(itemStr);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = sec.getString("name", "");
            name = name.replace("%mcbank_bank_limit%", limit).replace("%min_deposit%", minDeposit).replace("%min_withdraw%", minWithdraw);
            meta.setDisplayName(LanguageManager.colorize(name));
            final List<String> lore = sec.getStringList("lore");
            if (!lore.isEmpty()) {
                final List<String> coloredLore = new ArrayList<String>();
                for (String line : lore) {
                    line = line.replace("%mcbank_bank_limit%", limit).replace("%min_deposit%", minDeposit).replace("%min_withdraw%", minWithdraw);
                    coloredLore.add(LanguageManager.colorize(line));
                }
                meta.setLore((List)coloredLore);
            }
            final boolean glow = sec.getBoolean("glow", false);
            if (glow) {
                meta.addEnchant(Enchantment.INFINITY, 1, true);
                meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ENCHANTS });
            }
            meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
            item.setItemMeta(meta);
        }
        if (slot < inv.getSize()) {
            inv.setItem(slot, item);
        }
    }
    
    public void openPINMenu(final Player player, final String currentPin) {
        final String pinTitle = LanguageManager.colorize(this.atmGuiConfig.getString("pin_menu.title", "&0&lEnter PIN"));
        final Inventory inv = Bukkit.createInventory((InventoryHolder)null, 27, pinTitle);
        final ItemStack filler = this.buildSimpleItem("GRAY_STAINED_GLASS_PANE", " ");
        for (int i = 0; i < 27; ++i) {
            inv.setItem(i, filler);
        }
        final ConfigurationSection buttons = this.atmGuiConfig.getConfigurationSection("pin_menu.buttons");
        if (buttons != null) {
            for (String digit : buttons.getKeys(false)) {
                final ConfigurationSection btn = buttons.getConfigurationSection(digit);
                if (btn == null) {
                    continue;
                }
                final int slot = btn.getInt("slot", 0);
                final String itemStr = btn.getString("item", "IRON_TRAPDOOR");
                final ItemStack item = this.buildSimpleItem(itemStr, "&f" + digit);
                if (slot >= 27) {
                    continue;
                }
                inv.setItem(slot, item);
            }
        }
        final int clearSlot = this.atmGuiConfig.getInt("pin_menu.clear_slot", 3);
        final String clearItem = this.atmGuiConfig.getString("pin_menu.clear_item", "BARRIER");
        final String clearName = LanguageManager.colorize(this.atmGuiConfig.getString("pin_menu.clear_name", "&cClear"));
        inv.setItem(clearSlot, this.buildSimpleItemColored(clearItem, clearName));
        final int confirmSlot = this.atmGuiConfig.getInt("pin_menu.confirm_slot", 8);
        final String confirmItem = this.atmGuiConfig.getString("pin_menu.confirm_item", "LIME_DYE");
        final String confirmName = LanguageManager.colorize(this.atmGuiConfig.getString("pin_menu.confirm_name", "&aConfirm"));
        inv.setItem(confirmSlot, this.buildSimpleItemColored(confirmItem, confirmName));
        final int displaySlot = this.atmGuiConfig.getInt("pin_menu.display_slot", 4);
        final String displayItem = this.atmGuiConfig.getString("pin_menu.display_item", "PAPER");
        final String displayNameTemplate = this.atmGuiConfig.getString("pin_menu.display_name", "&eEntered: %pin_display%");
        final String displayName = LanguageManager.colorize(displayNameTemplate.replace("%pin_display%", currentPin));
        inv.setItem(displaySlot, this.buildSimpleItemColored(displayItem, displayName));
        player.openInventory(inv);
    }
    
    public boolean isNumberButton(final int slot) {
        final ConfigurationSection buttons = this.atmGuiConfig.getConfigurationSection("pin_menu.buttons");
        if (buttons == null) {
            return false;
        }
        for (final String digit : buttons.getKeys(false)) {
            final ConfigurationSection btn = buttons.getConfigurationSection(digit);
            if (btn != null && btn.getInt("slot", -1) == slot) {
                return true;
            }
        }
        return false;
    }
    
    public String getDigitForSlot(final int slot) {
        final ConfigurationSection buttons = this.atmGuiConfig.getConfigurationSection("pin_menu.buttons");
        if (buttons == null) {
            return null;
        }
        for (final String digit : buttons.getKeys(false)) {
            final ConfigurationSection btn = buttons.getConfigurationSection(digit);
            if (btn != null && btn.getInt("slot", -1) == slot) {
                return digit;
            }
        }
        return null;
    }
    
    public int getPINClearSlot() {
        return this.atmGuiConfig.getInt("pin_menu.clear_slot", 3);
    }
    
    public int getPINConfirmSlot() {
        return this.atmGuiConfig.getInt("pin_menu.confirm_slot", 8);
    }
    
    public int getPINDisplaySlot() {
        return this.atmGuiConfig.getInt("pin_menu.display_slot", 4);
    }
    
    private ItemStack buildSimpleItem(final String materialStr, final String name) {
        Material mat;
        try {
            mat = Material.valueOf(materialStr.toUpperCase());
        }
        catch (final IllegalArgumentException e) {
            mat = Material.STONE;
        }
        final ItemStack item = new ItemStack(mat);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(LanguageManager.colorize(name));
            meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack buildSimpleItemColored(final String materialStr, final String coloredName) {
        Material mat;
        try {
            mat = Material.valueOf(materialStr.toUpperCase());
        }
        catch (final IllegalArgumentException e) {
            mat = Material.STONE;
        }
        final ItemStack item = new ItemStack(mat);
        final ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(coloredName);
            meta.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_ATTRIBUTES });
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack buildPlayerHead(final Player player) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        final SkullMeta meta = (SkullMeta)item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer((OfflinePlayer)player);
            item.setItemMeta((ItemMeta)meta);
        }
        return item;
    }
    
    private ItemStack buildCustomSkull(final String base64) {
        final ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        try {
            final SkullMeta meta = (SkullMeta)item.getItemMeta();
            if (meta == null) {
                return item;
            }
            final PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            String textureUrl;
            try {
                final byte[] decoded = Base64.getDecoder().decode(base64);
                final String json = new String(decoded);
                final int urlStart = json.indexOf("\"url\":\"") + 7;
                final int urlEnd = json.indexOf("\"", urlStart);
                textureUrl = json.substring(urlStart, urlEnd);
            }
            catch (final Exception e) {
                return item;
            }
            try {
                final URL url = new URL(textureUrl);
                profile.getTextures().setSkin(url);
                meta.setOwnerProfile(profile);
            }
            catch (final Exception e) {
                return item;
            }
            item.setItemMeta((ItemMeta)meta);
        }
        catch (final Exception e2) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
        return item;
    }
}
