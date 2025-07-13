package deFelix.testplugin.Commands;

import deFelix.testplugin.Testplugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminitemsCommand implements CommandExecutor {

    private final Testplugin plugin;

    public AdminitemsCommand(Testplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            String prefix = plugin.getConfig().getString("permissions.prefix", "default");

            String permission = prefix + ".admin";

            if (!player.hasPermission(permission)) {
                player.sendMessage("§cDu hast keine Berechtigung, dieses Kit zu erhalten!");
                return true;
            }

            if (args.length == 0) {
                Inventory inventory = Bukkit.createInventory(null, 9 * 3, "§6§lKit-Inventar");

                ItemStack item = new ItemStack(Material.BOW);
                ItemMeta itemMeta = item.getItemMeta();
                itemMeta.setDisplayName("§a§l God Bogen");
                itemMeta.addEnchant(Enchantment.POWER, 5, false);
                item.setItemMeta(itemMeta);

                inventory.addItem(item);
                item.setAmount(2);
                inventory.setItem(14, item);

                player.openInventory(inventory);
                player.sendMessage("§aDir wurde das Kit-Menü geöffnet!");
            } else {
                player.sendMessage("§cBitte benutze §6/kit§c!");
            }

        } else {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden!");
        }

        return true;
    }
}
