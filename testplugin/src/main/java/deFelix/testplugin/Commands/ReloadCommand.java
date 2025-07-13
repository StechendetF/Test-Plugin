package deFelix.testplugin.Commands;

import deFelix.testplugin.Testplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

    private final Testplugin plugin;

    public ReloadCommand(Testplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            String prefix = plugin.getConfig().getString("permissions.prefix", "testplugin");
            String permission = prefix + ".reload";

            if (!player.hasPermission(permission)) {
                player.sendMessage("§cDu hast keine Berechtigung, um das Plugin neu zu laden!");
                return true;
            }

            plugin.getConfigManager().reloadConfig();

            plugin.getCommand("spirale").setExecutor(new SpiraleCommand(plugin));
            plugin.getCommand("testcommand").setExecutor(new testCommand(plugin));
            plugin.getCommand("Discord").setExecutor(new Werbung());
            plugin.getCommand("admin").setExecutor(new AdminitemsCommand(plugin));
            plugin.getCommand("server-restart").setExecutor(new timer());

            player.sendMessage("§aDas Plugin wurde erfolgreich neu geladen!");
        } else {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden!");
        }

        return true;
    }
}
