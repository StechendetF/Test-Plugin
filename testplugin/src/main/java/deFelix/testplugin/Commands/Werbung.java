package deFelix.testplugin.Commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Werbung implements CommandExecutor {
    private File configFile;
    private FileConfiguration config;

    public Werbung() {
        loadConfig(); // Nur hier wird sie korrekt aufgerufen
    }

    private void loadConfig() {
        configFile = new File(Bukkit.getPluginManager().getPlugin("nightsmp").getDataFolder(), "discord.yml");

        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();

                config = YamlConfiguration.loadConfiguration(configFile);
                config.set("discord-messages", List.of("§a§lNight Discord", "§bKlicke unten für mehr Infos!"));
                config.set("discord-link", "https://discord.gg/4bZMBejuRk");
                config.set("hover-text", "Besuche den Discord Server von uns");

                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            List<String> messages = config.getStringList("discord-messages");
            String link = config.getString("discord-link", "https://discord.gg/4bZMBejuRk");
            String hover = config.getString("hover-text", "Besuche den Discord Server von uns");

            for (String message : messages) {
                TextComponent textComponent = new TextComponent(message);
                player.spigot().sendMessage(textComponent);
            }

            TextComponent clickMe = new TextComponent("§a§l Klicke hier für Discord ");
            clickMe.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
            clickMe.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));

            player.spigot().sendMessage(clickMe);

            return true;
        }

        sender.sendMessage("§cDiesen Befehl kannst du nur als Spieler ausführen!");
        return false;
    }
}
