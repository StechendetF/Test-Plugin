package deFelix.testplugin.Commands;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class actionbarTimer implements CommandExecutor {

    private final JavaPlugin plugin;
    private int ticks = 0;
    private boolean running = false;
    private BukkitRunnable timerTask;

    public actionbarTimer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Nur Spieler können diesen Befehl ausführen.");
            return true;
        }

        Player player = (Player) commandSender;

        String prefix = plugin.getConfig().getString("permissions.prefix", "testplugin");
        String permission = prefix + ".actionbarTimer";

        if (!player.hasPermission(permission)) {
            player.sendMessage("§cDu hast keine Berechtigung, um diesen Befehl auszuführen!");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("create")) {
                // Timer starten
                if (!running) {
                    startTimer();
                    player.sendMessage(ChatColor.GREEN + "Timer gestartet!");
                } else {
                    player.sendMessage(ChatColor.RED + "Der Timer läuft bereits!");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("stop")) {
                // Timer stoppen
                if (running) {
                    stopTimer();
                    player.sendMessage(ChatColor.GREEN + "Timer gestoppt!");
                } else {
                    player.sendMessage(ChatColor.RED + "Kein Timer läuft!");
                }
                return true;
            }
        }
        return false;
    }

    private void startTimer() {
        running = true;
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                ticks++;
                int totalSeconds = ticks / 20; // Minecraft-Server läuft mit 20 Ticks pro Sekunde
                int days = totalSeconds / 86400;
                int hours = (totalSeconds % 86400) / 3600;
                int minutes = (totalSeconds % 3600) / 60;
                int seconds = totalSeconds % 60;

                String timeString = String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);

                // Action-Bar an alle online Spieler senden
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.YELLOW + timeString));
                }
            }
        };
        // Timer läuft jede Minecraft-Tick (1/20s)
        timerTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void stopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            running = false;
        }
    }
}
