package deFelix.testplugin.Commands;

import deFelix.testplugin.Testplugin;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Crafting implements CommandExecutor, Listener {

    private final Map<UUID, String> editing = new HashMap<>();

    private FileConfiguration getConfigFile() {
        File file = new File(Testplugin.getPlugin().getDataFolder(), "craftingrezepte.yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    private File getConfigPath() {
        return new File(Testplugin.getPlugin().getDataFolder(), "craftingrezepte.yml");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Nur Spieler können das nutzen.");
            return true;
        }

        if (cmdLabel.equalsIgnoreCase("craftingmenu")) {
            if (args.length != 1) {
                p.sendMessage("§cBenutze /craftingmenu <name>");
                return true;
            }

            String name = args[0];
            FileConfiguration config = getConfigFile();

            if (!config.contains(name)) {
                p.sendMessage("§cRezept '" + name + "' nicht gefunden.");
                return true;
            }

            Inventory inv = Bukkit.createInventory(null, InventoryType.WORKBENCH, "§7Rezept: §f" + name);
            for (int i = 0; i < 9; i++) {
                ItemStack item = config.getItemStack(name + ".input." + i);
                inv.setItem(i + 1, item); // Input-Slots: 1–9
            }
            inv.setItem(0, config.getItemStack(name + ".output")); // Output-Slot
            p.openInventory(inv);
            return true;
        }

        if (cmdLabel.equalsIgnoreCase("crafting")) {
            if (args.length < 2) {
                p.sendMessage("§c/crafting hinzufügen <name>");
                p.sendMessage("§c/crafting remove <name>");
                return true;
            }

            String sub = args[0];
            String name = args[1].toLowerCase();
            FileConfiguration config = getConfigFile();
            File file = getConfigPath();

            if (sub.equalsIgnoreCase("hinzufügen")) {
                Inventory inv = Bukkit.createInventory(p, InventoryType.WORKBENCH, "§aBearbeite: " + name);
                p.openInventory(inv);
                editing.put(p.getUniqueId(), name);

                new BukkitRunnable() {
                    public void run() {
                        if (!editing.containsKey(p.getUniqueId())) return;

                        Inventory inv = p.getOpenInventory().getTopInventory();
                        for (int i = 1; i <= 9; i++) {
                            config.set(name + ".input." + (i - 1), inv.getItem(i));
                        }
                        config.set(name + ".output", inv.getItem(0));

                        try {
                            config.save(file);
                            p.sendMessage("§aRezept '" + name + "' gespeichert.");
                        } catch (IOException e) {
                            p.sendMessage("§cFehler beim Speichern.");
                        }

                        editing.remove(p.getUniqueId());
                    }
                }.runTaskLater(Testplugin.getPlugin(), 20 * 10);
                return true;
            }

            if (sub.equalsIgnoreCase("remove")) {
                if (config.contains(name)) {
                    config.set(name, null);
                    try {
                        config.save(file);
                        p.sendMessage("§aRezept '" + name + "' gelöscht.");
                    } catch (IOException e) {
                        p.sendMessage("§cFehler beim Löschen.");
                    }
                } else {
                    p.sendMessage("§cRezept nicht gefunden.");
                }
                return true;
            }
        }

        return false;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().startsWith("§7Rezept:")) {
            e.setCancelled(true); // verhindert Herausnehmen
        }
    }
}
