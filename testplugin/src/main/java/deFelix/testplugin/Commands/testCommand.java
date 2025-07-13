package deFelix.testplugin.Commands;

import deFelix.testplugin.Testplugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class testCommand implements CommandExecutor {

    private final Testplugin plugin;

    public testCommand(Testplugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            String prefix = plugin.getConfig().getString("permissions.prefix", "testplugin");

            String permission = prefix + ".test";

            if (!player.hasPermission(permission)) {
                player.sendMessage("§cDu hast keine Berechtigung, um diesen Befehl auszuführen!");
                return true;
            }
            String message = "das ist ein 2. Test!";
            player.sendMessage(message);

        } else {

            commandSender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden!");
        }

        return true;
    }
}
