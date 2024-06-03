package me.polishkrowa.enchantextractor;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public final class EnchantExtractor extends JavaPlugin implements CommandExecutor, TabExecutor {

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("extract-enchant").setExecutor(this);
        this.getCommand("extract-enchant").setTabCompleter(this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args != null && args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.GREEN + "EnchantExtractor v1.2");
            sender.sendMessage(getColoredMessage("messages.description"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(getColoredMessage("messages.not_player"));
            return true;
        }
        Player player = (Player) sender;

        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
            player.sendMessage(getColoredMessage("messages.no_mainhand"));
            return true;
        }

        if (player.getInventory().getItemInOffHand() == null || !player.getInventory().getItemInOffHand().getType().equals(Material.BOOK)) {
            player.sendMessage(getColoredMessage("messages.no_book"));
            return true;
        }

        ItemStack mainItem = player.getInventory().getItemInMainHand();

        boolean isOneBook = (player.getInventory().getItemInOffHand().getAmount() == 1);
        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();

        if (mainItem.getEnchantments().isEmpty()) {
            player.sendMessage(getColoredMessage("messages.no_enchants"));
            return true;
        }

        mainItem.getEnchantments().forEach((id, lvl) -> {
            bookMeta.addStoredEnchant(id, lvl, true);
            mainItem.removeEnchantment(id);
        });
        enchantedBook.setItemMeta(bookMeta);

        player.getInventory().setItemInMainHand(mainItem);

        if (isOneBook) {
            player.getInventory().setItemInOffHand(enchantedBook);
        } else {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            offHand.setAmount(offHand.getAmount() - 1);
            player.getInventory().setItemInOffHand(offHand);

            Location playerLoc = player.getLocation().clone();
            playerLoc.add(-0.5, -0.3, -0.5);
            Item itemEntity = player.getWorld().dropItemNaturally(playerLoc, enchantedBook);
            itemEntity.setOwner(player.getUniqueId());
            itemEntity.setPickupDelay(-1);
        }

        player.sendMessage(getColoredMessage("messages.success"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Arrays.asList("help", "extract");
    }

    private String getColoredMessage(String config_path) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString(config_path));
    }
}
