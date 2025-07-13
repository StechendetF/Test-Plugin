package deFelix.testplugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class bossbartimer implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final Map<String, BukkitRunnable> timers = new HashMap<>();
    private final Map<String, BossBar> bossBars = new HashMap<>();
    private final File timersFolder;

    public bossbartimer(JavaPlugin plugin) {
        this.plugin = plugin;
        this.timersFolder = new File(plugin.getDataFolder(), "timers");
        if (!timersFolder.exists()) timersFolder.mkdirs();
        // Listener, um BossBars bei Spielerbeitritt anzuzeigen
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Nur Spieler können diesen Befehl ausführen.");
            return true;
        }
        Player player = (Player) commandSender;

        String prefix = plugin.getConfig().getString("permissions.prefix", "testplugin");
        String permission = prefix + ".bossbartimer";

        if (!player.hasPermission(permission)) {
            player.sendMessage("§cDu hast keine Berechtigung, um diesen Befehl auszuführen!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Verwendung: /timer <create/delete> <name>");
            return true;
        }

        String action = args[0].toLowerCase();
        String timerName = args[1];

        if (action.equals("create")) {
            if (timers.containsKey(timerName)) {
                player.sendMessage(ChatColor.RED + "Ein Timer mit diesem Namen existiert bereits.");
                return true;
            }

            if (args.length < 3) {
                player.sendMessage(ChatColor.RED + "Verwendung: /timer create <name> <countdown/timer-normal> [world] [days] [hours] [minutes] [seconds]");
                return true;
            }

            String mode = args[2].toLowerCase();

            if (!mode.equals("countdown") && !mode.equals("timer-normal")) {
                player.sendMessage(ChatColor.RED + "Modus muss entweder 'countdown' oder 'timer-normal' sein.");
                return true;
            }

            World world = Bukkit.getWorld(args[3]);
            if (world == null) {
                player.sendMessage(ChatColor.RED + "Welt nicht gefunden.");
                return true;
            }

            long time = 0;
            if (mode.equals("countdown")) {
                if (args.length < 8) {
                    player.sendMessage(ChatColor.RED + "Bitte gib die Zeit für den Countdown an: <Tage> <Stunden> <Minuten> <Sekunden>");
                    return true;
                }
                try {
                    time = Integer.parseInt(args[4]) * 86400L + Integer.parseInt(args[5]) * 3600L + Integer.parseInt(args[6]) * 60L + Integer.parseInt(args[7]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Ungültiges Zahlenformat.");
                    return true;
                }
            }

            startTimer(timerName, mode, world, time);
            saveTimerConfig(timerName, mode, world, time);
            player.sendMessage(ChatColor.GREEN + "Timer " + timerName + " gestartet!");

        } else if (action.equals("delete")) {
            stopTimer(timerName);
            player.sendMessage(ChatColor.GREEN + "Timer " + timerName + " gelöscht!");
        }
        return true;
    }

    private void startTimer(String name, String mode, World world, long time) {
        BossBar bossBar = Bukkit.createBossBar(formatTime(time), BarColor.YELLOW, BarStyle.SOLID);
        bossBars.put(name, bossBar);

        BukkitRunnable task = new BukkitRunnable() {
            long counter = time;
            boolean finished = false;

            @Override
            public void run() {
                if (finished) {
                    // Sobald der Timer abgelaufen ist, füllt sich die BossBar langsam auf
                    double progress = bossBar.getProgress();
                    if (progress < 1.0) {
                        bossBar.setProgress(Math.min(progress + 0.05, 1.0));
                    }
                    return;
                }

                bossBar.setTitle(formatTime(counter));
                bossBar.setProgress(Math.max((double) counter / time, 0.0));

                // Alle Spieler, die auf dem Server sind, erhalten die BossBar
                for (Player p : world.getPlayers()) {
                    bossBar.addPlayer(p);
                }

                if (counter <= 0) {
                    bossBar.setColor(BarColor.RED);
                    bossBar.setTitle(ChatColor.RED + "Timer abgelaufen!");
                    finished = true;
                } else {
                    if (mode.equals("countdown")) {
                        counter--;
                    } else {
                        counter++;
                    }
                }
            }
        };
        timers.put(name, task);
        task.runTaskTimer(plugin, 20, 20);
    }

    private void stopTimer(String name) {
        if (timers.containsKey(name)) {
            timers.get(name).cancel();
            timers.remove(name);
            File file = new File(timersFolder, name + ".yml");
            if (file.exists()) file.delete();
        }
        if (bossBars.containsKey(name)) {
            bossBars.get(name).removeAll();
            bossBars.remove(name);
        }
    }

    private void saveTimerConfig(String name, String mode, World world, long time) {
        File file = new File(timersFolder, name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("mode", mode);
        config.set("world", world.getName());
        config.set("time", time);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatTime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, secs);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return suggestions;
        }

        if (args.length == 1) {
            return List.of("create", "delete");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            return new ArrayList<>(timers.keySet());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return List.of("<name>");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            return List.of("countdown", "timer-normal");
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            List<String> worlds = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                worlds.add(world.getName());
            }
            return worlds;
        }

        if (args.length == 5 && args[0].equalsIgnoreCase("create") && args[2].equalsIgnoreCase("countdown")) {
            return List.of("<Tage>");
        }

        if (args.length == 6 && args[0].equalsIgnoreCase("create") && args[2].equalsIgnoreCase("countdown")) {
            return List.of("<Stunden>");
        }

        if (args.length == 7 && args[0].equalsIgnoreCase("create") && args[2].equalsIgnoreCase("countdown")) {
            return List.of("<Minuten>");
        }

        if (args.length == 8 && args[0].equalsIgnoreCase("create") && args[2].equalsIgnoreCase("countdown")) {
            return List.of("<Sekunden>");
        }


        return suggestions;
    }

    // Event-Listener zum Hinzufügen der BossBars bei Spielerbeitritt
    private class PlayerJoinListener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            for (BossBar bar : bossBars.values()) {
                bar.addPlayer(event.getPlayer());
            }
        }
    }
}
