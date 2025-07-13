package deFelix.testplugin;


import deFelix.testplugin.Commands.*;


import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Testplugin extends JavaPlugin {


    private static Testplugin plugin;
    private ConfigManager configManager;
    private flyschuhe flyschuheCommand;

    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("Test Plugin wird aktiviert...");

        // Werbung-Command (falls du Nachrichten ausspielen willst)
        Werbung werbung = new Werbung();

        // Config Manager
        configManager = new ConfigManager(this);

        // Ordner für Spiralen
        File spiralenFolder = new File(getDataFolder(), "spiralen");
        if (!spiralenFolder.exists()) {
            spiralenFolder.mkdir();
        }

        // Command-Registrierung
        getCommand("spirale").setExecutor(new SpiraleCommand(this));
        getCommand("testcommand").setExecutor(new testCommand(this));
        getCommand("Discord").setExecutor(werbung);
        getCommand("admin").setExecutor(new AdminitemsCommand(this));
        getCommand("server-restart").setExecutor(new timer());
        getCommand("test").setExecutor(new ReloadCommand(this));
        getCommand("bossbartimer").setExecutor(new bossbartimer(this));
        getCommand("timer").setExecutor(new actionbarTimer(this));
        flyschuheCommand = new flyschuhe(this);
        getCommand("flyschuhe").setExecutor(flyschuheCommand);

        // Crafting Listener und Commands
        Crafting crafting = new Crafting();
        getCommand("crafting").setExecutor(crafting);
        getCommand("craftingmenu").setExecutor(crafting);
        getServer().getPluginManager().registerEvents(crafting, this);

        // Boss-Command, falls du es nutzen willst
        Boss bossCommand = new Boss(this);
        getCommand("boss").setExecutor(bossCommand);
        getCommand("boss").setTabCompleter(bossCommand);


        getLogger().info("Test Plugin wurde Erfolgreich geladen");
    }

    @Override
    public void onDisable() {
        getLogger().info("Test Plugin wird deaktiviert...");
    }

    public static Testplugin getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}