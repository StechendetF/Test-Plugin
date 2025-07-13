package deFelix.testplugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpiraleCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final HashMap<String, BukkitRunnable> activeSpirals = new HashMap<>();

    public SpiraleCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        loadAllSpirals();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cNur Spieler können diesen Befehl nutzen!");
            return true;
        }

        Player player = (Player) sender;

        String prefix = plugin.getConfig().getString("permissions.prefix", "default");
        String permission = prefix + ".spirale";
        if (!player.hasPermission(permission)) {
            player.sendMessage("§cDu hast keine Berechtigung, um Spiralen zu erstellen oder zu löschen!");
            return true;
        }


        if (args.length < 1) {
            player.sendMessage("§cBenutze: /spirale set <Name> <Höhe> <Breite> <Partikel> oder /spirale delete <Name>");
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 5) {
                player.sendMessage("§cBenutze: /spirale set <Name> <Höhe> <Breite> <Partikel>");
                return true;
            }

            String name = args[1];
            int hoehe, breite;
            try {
                hoehe = Math.max(1, Integer.parseInt(args[2]));
                breite = Math.max(1, Integer.parseInt(args[3]));
            } catch (NumberFormatException e) {
                player.sendMessage("§cBitte gib eine gültige Zahl für Höhe und Breite ein!");
                return true;
            }

            Particle partikel;
            try {
                partikel = Particle.valueOf(args[4].toUpperCase());
            } catch (IllegalArgumentException e) {
                player.sendMessage("§cUngültiger Partikeltyp! Bitte wähle einen gültigen Partikel.");
                return true;
            }

            Location location = player.getLocation();
            startSpiralEffect(name, location, hoehe, breite, partikel);
            saveSpiral(name, location, hoehe, breite, partikel);

            player.sendMessage("§aSpirale '" + name + "' mit Höhe " + hoehe + " und Breite " + breite + " erstellt!");
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                player.sendMessage("§cBitte gib den Namen der Spirale an, die du löschen möchtest!");
                return true;
            }

            String name = args[1];
            if (deleteSpiral(name)) {
                player.sendMessage("§cSpirale '" + name + "' wurde gelöscht!");
            } else {
                player.sendMessage("§cKeine Spirale mit diesem Namen gefunden!");
            }
            return true;
        }

        return false;
    }

    private void startSpiralEffect(String name, Location location, int hoehe, int breite, Particle partikel) {
        if (activeSpirals.containsKey(name)) {
            activeSpirals.get(name).cancel();
        }

        BukkitRunnable task = new BukkitRunnable() {
            double t = 0;
            final double maxHeight = hoehe;
            final double scale = Math.PI / (hoehe * 4.0);

            @Override
            public void run() {
                t += scale;
                if (t > Math.PI * 2) t = 0;

                double x = Math.cos(t) * breite;
                double y = (t / (Math.PI * 2)) * maxHeight;
                double z = Math.sin(t) * breite;

                Location spiralLoc = location.clone().add(x, y, z);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spawnParticle(partikel, spiralLoc, 1, 0, 0, 0, 0);
                }
            }
        };

        task.runTaskTimer(plugin, 0, 2);
        activeSpirals.put(name, task);
    }

    private void saveSpiral(String name, Location location, int hoehe, int breite, Particle partikel) {
        File spiralenFolder = new File(plugin.getDataFolder(), "spiralen");
        if (!spiralenFolder.exists()) {
            spiralenFolder.mkdir();
        }

        File spiralFile = new File(spiralenFolder, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(spiralFile);

        config.set("location.x", location.getX());
        config.set("location.y", location.getY());
        config.set("location.z", location.getZ());
        config.set("location.world", location.getWorld().getName());
        config.set("hoehe", hoehe);
        config.set("breite", breite);
        config.set("partikel", partikel.name());

        try {
            config.save(spiralFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Fehler beim Speichern der Spirale: " + e.getMessage());
        }
    }

    private boolean deleteSpiral(String name) {
        File spiralenFolder = new File(plugin.getDataFolder(), "spiralen");
        File spiralFile = new File(spiralenFolder, name + ".yml");

        if (spiralFile.exists()) {
            spiralFile.delete();
            if (activeSpirals.containsKey(name)) {
                activeSpirals.get(name).cancel();
                activeSpirals.remove(name);
            }
            return true;
        }
        return false;
    }

    private void loadAllSpirals() {
        File spiralenFolder = new File(plugin.getDataFolder(), "spiralen");
        if (!spiralenFolder.exists()) {
            return;
        }

        for (File file : spiralenFolder.listFiles()) {
            if (file.getName().endsWith(".yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = file.getName().replace(".yml", "");

                double x = config.getDouble("location.x");
                double y = config.getDouble("location.y");
                double z = config.getDouble("location.z");
                String worldName = config.getString("location.world");
                int hoehe = config.getInt("hoehe");
                int breite = config.getInt("breite");
                Particle partikel = Particle.valueOf(config.getString("partikel"));

                if (Bukkit.getWorld(worldName) == null) continue;

                Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                startSpiralEffect(name, location, hoehe, breite, partikel);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("set");
            suggestions.add("delete");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            File spiralenFolder = new File(plugin.getDataFolder(), "spiralen");
            if (spiralenFolder.exists()) {
                for (File file : spiralenFolder.listFiles()) {
                    if (file.getName().endsWith(".yml")) {
                        suggestions.add(file.getName().replace(".yml", ""));
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            suggestions.add("<Name>");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            suggestions.add("<höhe>");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("set")) {
            suggestions.add("<breite>");
        } else if (args.length == 5 && args[0].equalsIgnoreCase("set")) {
            suggestions.add("AMBIENT_ENTITY_EFFECT");
            suggestions.add("ANGRY_VILLAGER");
            suggestions.add("BARRIER");
            suggestions.add("BLOCK");
            suggestions.add("BUBBLE");
            suggestions.add("BUBBLE_COLUMN_UP");
            suggestions.add("BUBBLE_POP");
            suggestions.add("CAMPFIRE_COSY_SMOKE");
            suggestions.add("CAMPFIRE_SIGNAL_SMOKE");
            suggestions.add("CLOUD");
            suggestions.add("COMPOSTER");
            suggestions.add("CRIMSON_SPORE");
            suggestions.add("CRIT");
            suggestions.add("CURRENT_DOWN");
            suggestions.add("DAMAGE_INDICATOR");
            suggestions.add("DOLPHIN");
            suggestions.add("DRAGON_BREATH");
            suggestions.add("DRIPPING_DRIPSTONE_LAVA");
            suggestions.add("DRIPPING_DRIPSTONE_WATER");
            suggestions.add("DRIPPING_HONEY");
            suggestions.add("DRIPPING_LAVA");
            suggestions.add("DRIPPING_OBSIDIAN_TEAR");
            suggestions.add("DRIPPING_WATER");
            suggestions.add("DUST");
            suggestions.add("DUST_COLOR_TRANSITION");
            suggestions.add("ELECTRIC_SPARK");
            suggestions.add("ENCHANT");
            suggestions.add("ENCHANTED_HIT");
            suggestions.add("END_ROD");
            suggestions.add("ENTITY_EFFECT");
            suggestions.add("EXPLOSION");
            suggestions.add("EXPLOSION_EMITTER");
            suggestions.add("FALLING_DUST");
            suggestions.add("FIREWORKS");
            suggestions.add("FISHING");
            suggestions.add("FURNACE");
            suggestions.add("HAPPY_VILLAGER");
            suggestions.add("HEART");
            suggestions.add("ITEM");
            suggestions.add("ITEM_CRACK");
            suggestions.add("ITEM_TAKE");
            suggestions.add("LAVA");
            suggestions.add("LANDMARK");
            suggestions.add("LARGE_EXPLOSION");
            suggestions.add("LARGE_SMOKE");
            suggestions.add("LEASH");
            suggestions.add("MINECART");
            suggestions.add("MOOSHROOM");
            suggestions.add("NOTE");
            suggestions.add("PAINTING");
            suggestions.add("PILLAR_DRIP");
            suggestions.add("PORTAL");
            suggestions.add("RAIN");
            suggestions.add("SMOKE");
            suggestions.add("SPARK");
            suggestions.add("SPLASH");
            suggestions.add("SWEET_SPRAY");
            suggestions.add("TOTEM");
            suggestions.add("VILLAGER_HAPPY");
            suggestions.add("VILLAGER_SAD");
            suggestions.add("WATER_BUBBLE");
            suggestions.add("WATER_DROP");
            suggestions.add("WATER_SPLASH");
            suggestions.add("WATER_WAKE");
            suggestions.add("WITHER");
            suggestions.add("WITHER_SKELETON");
            suggestions.add("WITCH");
            suggestions.add("ZOMBIE");
            suggestions.add("ZOMBIE_HEAD");
            suggestions.add("ZOMBIE_VILLAGER");
            suggestions.add("ZOMBIE_VILLAGER_HEAD");
            suggestions.add("FALLING_SPORE_BLOSSOM");
            suggestions.add("DRIPPING_SPORE_BLOSSOM");
            suggestions.add("SPLASH_POTION");
            suggestions.add("LARGE_SMOKE");
            suggestions.add("ENTITY_EFFECT");
            suggestions.add("GLASS_BREAK");
            suggestions.add("EXPLOSION_EMITTER");
            suggestions.add("CONDUIT");
            suggestions.add("VIBRATION");
            suggestions.add("LARGEBLACKBUBBLE");
            suggestions.add("FALLING_RENSTONE");
            suggestions.add("BUBBLE_WATER");

        }

        return suggestions;
    }
}
