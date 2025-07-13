package deFelix.testplugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class flyschuhe implements CommandExecutor {

    private final JavaPlugin plugin;
    private final String loreTag = ChatColor.GOLD + "[Flugschuhe]";

    public flyschuhe(JavaPlugin plugin) {
        this.plugin = plugin;
        startFlightChecker();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl ausführen.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cVerwendung: /flyschuhe <markieren|unmarkieren>");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§cHalte ein Item in der Hand.");
            return true;
        }

        if (!item.getType().toString().endsWith("_BOOTS")) {
            player.sendMessage("§cNur Schuhe können markiert werden.");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta != null && meta.hasLore() ? meta.getLore() : new ArrayList<>();

        switch (args[0].toLowerCase()) {
            case "markieren":
                if (lore.contains(loreTag)) {
                    player.sendMessage("§eDiese Schuhe sind bereits markiert.");
                } else {
                    lore.add(loreTag);
                    if (meta != null) {
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        player.sendMessage("§aSchuhe wurden als Flugschuhe markiert.");
                        // Sofortiges Fliegen, falls getragen
                        ItemStack boots = player.getInventory().getBoots();
                        if (isFlyingBoots(boots)) {
                            enableFlight(player);
                        }
                    }
                }
                break;

            case "unmarkieren":
                if (lore.contains(loreTag)) {
                    lore.remove(loreTag);
                    if (meta != null) {
                        meta.setLore(lore);
                        item.setItemMeta(meta);
                        player.sendMessage("§aFlugschuhe wurden entfernt.");
                        // Flugfähigkeit entfernen, falls gerade getragen
                        ItemStack boots = player.getInventory().getBoots();
                        if (!isFlyingBoots(boots)) {
                            disableFlight(player);
                        }
                    }
                } else {
                    player.sendMessage("§cDiese Schuhe sind nicht markiert.");
                }
                break;

            default:
                player.sendMessage("§cUnbekannter Unterbefehl.");
                break;
        }

        return true;
    }

    private void startFlightChecker() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    GameMode mode = player.getGameMode();

                    // Immer Flug erlaubt in Creative & Spectator
                    if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) {
                        player.setAllowFlight(true);
                        continue;
                    }

                    ItemStack boots = player.getInventory().getBoots();
                    if (isFlyingBoots(boots)) {
                        enableFlight(player);
                        // Flammeneffekt als Deko
                        if (player.isFlying()) {
                            player.getWorld().playEffect(player.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
                        }
                    } else {
                        disableFlight(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Alle 0.5 Sekunden prüfen
    }

    private boolean isFlyingBoots(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        if (!item.getType().toString().endsWith("_BOOTS")) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        List<String> lore = meta.getLore();
        return lore != null && lore.contains(loreTag);
    }

    private void enableFlight(Player player) {
        if (!player.getAllowFlight()) player.setAllowFlight(true);
    }

    private void disableFlight(Player player) {
        if (player.getAllowFlight()) player.setAllowFlight(false);
        if (player.isFlying()) player.setFlying(false);
    }
}