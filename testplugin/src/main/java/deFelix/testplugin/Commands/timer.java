package deFelix.testplugin.Commands;

import deFelix.testplugin.Testplugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class timer implements CommandExecutor {
    private int time;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            String prefix = Testplugin.getPlugin().getConfig().getString("permissions.prefix", "testplugin");
            String permission = prefix + ".reload";

            if (!player.hasPermission(permission)) {
                player.sendMessage("§cDu hast keine Berechtigung, um das Plugin neu zu laden!");
                return true;
            }
        }

        time = 15;

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                switch (time) {
                    case 15:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle("§eNoch " + time + " Sekunden bis zum Restart", "", 10, 40, 10);
                        }
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_PLAY, 1.0f, 1.0f);
                        });
                        break;
                    case 3:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle("§eNoch " + time + " Sekunden bis zum Restart", "", 10, 40, 10);
                        }
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_PLAY, 1.0f, 1.0f);
                        });
                        break;
                    case 2:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle("§eNoch " + time + " Sekunden bis zum Restart", "", 10, 40, 10);
                        }
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_PLAY, 1.0f, 1.0f);
                        });
                        break;
                    case 1:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.sendTitle("§eNoch " + time + " Sekunde bis zum Restart", "", 10, 40, 10);
                        }
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_PLAY, 1.0f, 1.0f);
                        });
                        break;
                }

                // Zeigt die ActionBar für alle Spieler an
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText("§eNoch " + time + " Sekunden bis zum Restart"));
                }

                if (time == 0) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendTitle("§c§lServer Restartet", "", 10, 70, 20);

                        // ActionBar für den Restart anzeigen
                        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                net.md_5.bungee.api.chat.TextComponent.fromLegacyText("§c§lServer Restartet"));
                    }

                    new BukkitRunnable() {
                        private int countdown = 10;

                        @Override
                        public void run() {
                            if (countdown > 0) {
                                for (Player player : Bukkit.getOnlinePlayers()) {
                                    player.sendTitle("§c§lServer Restartet", "", 10, 70, 20);

                                    // ActionBar weiterhin für den Restart anzeigen
                                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                                            net.md_5.bungee.api.chat.TextComponent.fromLegacyText("§c§lServer Restartet"));
                                }
                                countdown--;
                            } else {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
                                cancel();
                            }
                        }
                    }.runTaskTimer(Testplugin.getPlugin(), 0, 20); // Alle 20 Ticks (1 Sekunde) wiederholen

                    cancel();
                    return;
                }
                time--;
            }
        };
        runnable.runTaskTimer(Testplugin.getPlugin(), 0, 20);

        return true;
    }
}
