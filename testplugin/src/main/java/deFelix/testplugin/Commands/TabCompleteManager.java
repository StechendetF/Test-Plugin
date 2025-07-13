package deFelix.testplugin.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class TabCompleteManager extends JavaPlugin implements TabCompleter {

    private Set<String> allowedCommands;
    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        loadConfig();
        CommandMap commandMap = getCommandMap();

        if (commandMap != null) {
            Map<String, Command> knownCommands = getKnownCommands(commandMap);
            for (Command command : knownCommands.values()) {
                if (command instanceof PluginCommand) {
                    ((PluginCommand) command).setTabCompleter(this);
                }
            }
        } else {
            getLogger().severe("Konnte die CommandMap nicht ermitteln!");
        }

        getLogger().info("TabCompleteManager erfolgreich aktiviert!");
    }

    private void loadConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.contains("allowed-commands")) {
            config.set("allowed-commands", Arrays.asList("warp", "home", "msg"));
            saveConfigFile();
        }

        allowedCommands = new HashSet<>(config.getStringList("allowed-commands"));
    }

    private void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CommandMap getCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, Command> getKnownCommands(CommandMap commandMap) {
        try {
            Field field = commandMap.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            return (Map<String, Command>) field.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        if (player.hasPermission("tabcomplete.all") || player.isOp()) {
            return null;
        }

        if (!allowedCommands.contains(command.getName())) {
            return Collections.emptyList();
        }

        return null;
    }
}
