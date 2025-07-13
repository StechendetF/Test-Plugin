package deFelix.testplugin.Commands;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Boss implements CommandExecutor, TabCompleter, Listener {

    private static final boolean BOSS_ENABLED = false;

    private final JavaPlugin plugin;
    private final Map<String, BossBar> bossBars = new HashMap<>();
    private final File bossesFolder;

    public Boss(JavaPlugin plugin) {
        this.plugin = plugin;
        this.bossesFolder = new File(plugin.getDataFolder(), "bosses");

        if (!bossesFolder.exists()) {
            bossesFolder.mkdirs();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            String prefix = plugin.getConfig().getString("permissions.prefix", "default");

            String permission = prefix + ".boss";

            if (!player.hasPermission(permission)) {
                player.sendMessage("§cDu hast keine Berechtigung, dieses Kit zu erhalten!");
                return true;
            }

            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Verwendung: /boss <create|delete|kill|spawn|drop>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create":
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Verwendung: /boss create <name> <mob_type>");
                        return true;
                    }
                    createBoss(player, args[1], args[2]);
                    break;
                case "delete":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Verwendung: /boss delete <name>");
                        return true;
                    }
                    deleteBoss(player, args[1]);
                    break;
                case "kill":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Verwendung: /boss kill <name>");
                        return true;
                    }
                    killBoss(player, args[1]);
                    break;
                case "spawn":
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Verwendung: /boss spawn <name>");
                        return true;
                    }
                    spawnBoss(player, args[1]);
                    break;
                case "drop":
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Verwendung: /boss drop <name> <item>");
                        return true;
                    }
                    dropItem(player, args[1], args[2]);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unbekannter Befehl.");
            }
            return true;
        }
        return false;
    }

    private void createBoss(Player player, String name, String type) {
        File bossFile = new File(bossesFolder, name + ".yml");
        if (bossFile.exists()) {
            player.sendMessage(ChatColor.RED + "Ein Boss mit diesem Namen existiert bereits!");
            return;
        }

        FileConfiguration config = new YamlConfiguration();
        config.set("name", name);
        config.set("type", type.toUpperCase());
        config.set("health", 200);
        config.set("scale", 2.0);
        config.set("damage", 10);
        config.set("spawn_particles", "SMOKE");
        config.set("death_particles", "EXPLOSION_LARGE");
        config.set("spawn_sound", "ENTITY_WITHER_SPAWN");
        config.set("death_sound", "ENTITY_GENERIC_EXPLODE");

        if (type.equalsIgnoreCase("CREEPER")) {
            config.set("creeper_explosion_radius", 0);
            config.set("creeper_destruction", false);
            config.set("mini_creeper_spawn_interval", 20 * 10);
            config.set("mini_creeper_health", 10);
            config.set("mini_creeper_damage", 5);
            config.set("mini_creeper_explosion", true);
        }

        try {
            config.save(bossFile);
            player.sendMessage(ChatColor.GREEN + "Boss " + name + " erstellt!");
        } catch (IOException e) {
            player.sendMessage(ChatColor.RED + "Fehler beim Speichern der Boss-Datei.");
        }
    }

    private void playSpawnAnimation(LivingEntity boss) {
        World world = boss.getWorld();
        Location location = boss.getLocation();

        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {
                phi += Math.PI / 10;
                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 40) {
                    double x = 1.5 * Math.cos(theta + phi);
                    double y = 0.5 * Math.sin(phi) + 1.5;
                    double z = 1.5 * Math.sin(theta + phi);
                    location.add(x, y, z);
                    world.spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
                    location.subtract(x, y, z);
                }
                if (phi > Math.PI * 10) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 2);

        world.playSound(location, Sound.ENTITY_WITHER_SPAWN, 1, 0.5f);
    }

    private void playDeathAnimation(LivingEntity boss) {
        World world = boss.getWorld();
        Location location = boss.getLocation();

        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_DEATH, 1, 0.5f);
    }

    private void spawnBoss(Player player, String name) {
        File bossFile = new File(bossesFolder, name + ".yml");
        if (!bossFile.exists()) {
            player.sendMessage(ChatColor.RED + "Boss nicht gefunden!");
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(bossFile);
        String type = config.getString("type");
        int health = config.getInt("health", 200);
        double scale = config.getDouble("scale", 2.0);
        int damage = config.getInt("damage", 10);
        String spawnParticles = config.getString("spawn_particles", "SMOKE");
        String deathParticles = config.getString("death_particles", "EXPLOSION_LARGE");
        String spawnSound = config.getString("spawn_sound", "ENTITY_WITHER_SPAWN");
        String deathSound = config.getString("death_sound", "ENTITY_GENERIC_EXPLODE");

        int miniCreeperSpawnInterval = 20 * 10;
        int miniCreeperHealth = 10;
        int miniCreeperDamage = 5;
        boolean miniCreeperExplosion = false;

        if (type.equalsIgnoreCase("CREEPER")) {
            miniCreeperSpawnInterval = config.getInt("mini_creeper_spawn_interval", 20 * 10);
            miniCreeperHealth = config.getInt("mini_creeper_health", 10);
            miniCreeperDamage = config.getInt("mini_creeper_damage", 5);
            miniCreeperExplosion = config.getBoolean("mini_creeper_explosion", true);
        }

        try {
            EntityType entityType = EntityType.valueOf(type.toUpperCase());
            World world = player.getWorld();
            Location location = player.getLocation();

            world.spawnParticle(Particle.valueOf(spawnParticles), location, 100);
            world.playSound(location, Sound.valueOf(spawnSound), 1, 1);

            int finalMiniCreeperSpawnInterval = miniCreeperSpawnInterval;
            int finalMiniCreeperHealth = miniCreeperHealth;
            int finalMiniCreeperDamage = miniCreeperDamage;
            boolean finalMiniCreeperExplosion = miniCreeperExplosion;

            new BukkitRunnable() {
                @Override
                public void run() {
                    LivingEntity boss = (LivingEntity) world.spawnEntity(location, entityType);
                    boss.setRemoveWhenFarAway(false);
                    playSpawnAnimation(boss);

                    if (boss.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                        boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
                    }
                    boss.setHealth(health);
                    boss.setCustomName(ChatColor.RED + name);
                    boss.setCustomNameVisible(true);
                    boss.setPersistent(true);
                    boss.setGlowing(true);


                    AttributeInstance scaleAttribute = boss.getAttribute(Attribute.GENERIC_SCALE);
                    if (scaleAttribute != null) {
                        scaleAttribute.setBaseValue(scale);
                    }

                    BossBar bossBar = Bukkit.createBossBar(ChatColor.RED + name, BarColor.RED, BarStyle.SOLID);
                    bossBar.setVisible(true);
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        bossBar.addPlayer(onlinePlayer);
                    }
                    bossBars.put(name, bossBar);

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (boss.isDead()) {
                                bossBar.removeAll();
                                bossBars.remove(name);
                                playDeathAnimation(boss);
                                cancel();
                            } else {
                                bossBar.setProgress(boss.getHealth() / boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                            }
                        }
                    }.runTaskTimer(plugin, 0, 20);

                    if (boss instanceof Creeper) {
                        Creeper creeper = (Creeper) boss;
                        creeper.setExplosionRadius(0);
                        creeper.setMaxFuseTicks(0);
                        creeper.setCanPickupItems(false);
                        creeper.setInvulnerable(false);
                        creeper.setPowered(false);
                        creeper.setMaxFuseTicks(Integer.MAX_VALUE);
                        ((Mob) creeper).setAI(true);

                        Player target = getRandomPlayer();
                        if (target != null) {
                            creeper.setTarget(target);
                        }

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (creeper.isDead()) {
                                    cancel();
                                    return;
                                }

                                if (creeper.getFuseTicks() > 0) {
                                    creeper.setFuseTicks(0);
                                }

                                Player target = getRandomPlayer();
                                if (target != null) {
                                    target.damage(damage);
                                }

                                LivingEntity miniCreeper = (LivingEntity) creeper.getWorld().spawnEntity(creeper.getLocation(), EntityType.CREEPER);
                                miniCreeper.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(finalMiniCreeperHealth);
                                miniCreeper.setHealth(finalMiniCreeperHealth);
                                miniCreeper.setCustomName(ChatColor.RED + "Mini Creeper");
                                miniCreeper.setCustomNameVisible(true);
                                miniCreeper.setPersistent(true);

                                if (miniCreeper instanceof Creeper) {
                                    Creeper miniC = (Creeper) miniCreeper;
                                    miniC.setExplosionRadius(finalMiniCreeperExplosion ? 3 : 0);
                                    miniC.setCanPickupItems(false);
                                }
                            }
                        }.runTaskTimer(plugin, 0, finalMiniCreeperSpawnInterval);
                    }

                    makeAggressive(boss);

                    player.sendMessage(ChatColor.GREEN + "Boss " + name + " gespawnt!");
                }
            }.runTaskLater(plugin, 100L);

        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Ungültiger Mob-Typ!");
        }
    }

    private void makeAggressive(LivingEntity boss) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead()) {
                    cancel();
                    return;
                }

                Player target = getRandomPlayer();
                if (target != null) {
                    if (boss instanceof Mob) {
                        ((Mob) boss).setTarget(target);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 40);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity.getCustomName() != null && entity.getCustomName().startsWith(ChatColor.RED.toString())) {
                if (event.getTarget() == null || !(event.getTarget() instanceof Player)) {
                    event.setCancelled(true);
                    Player target = getRandomPlayer();
                    if (target != null) {
                        event.setTarget(target);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCreeperExplosion(EntityExplodeEvent event) {
        if (event.getEntity() instanceof Creeper) {
            Creeper creeper = (Creeper) event.getEntity();
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMiniCreeperDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Creeper) {
            Creeper creeper = (Creeper) event.getEntity();

            if (creeper.getCustomName() != null && creeper.getCustomName().equals(ChatColor.RED + "Mini Creeper")) {
                event.getDrops().clear();
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (entity.getCustomName() != null && entity.getCustomName().startsWith(ChatColor.RED.toString())) {
                if (event.getDamager() instanceof Mob) {
                    event.setCancelled(false);
                } else if (entity instanceof Creeper) {
                    Creeper creeper = (Creeper) entity;
                    if (creeper.isCustomNameVisible()) {
                        event.setCancelled(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntity() instanceof Creeper) {
            Creeper creeper = (Creeper) event.getEntity();
            if (creeper.getCustomName() != null && creeper.getCustomName().startsWith(ChatColor.RED.toString())) {
                event.setCancelled(true);
                creeper.setMaxFuseTicks(Integer.MAX_VALUE);
            }
        }
    }

    private void dropItem(Player player, String name, String item) {
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof LivingEntity && entity.getCustomName() != null && entity.getCustomName().equals(ChatColor.RED + name)) {
                World world = entity.getWorld();
                Location location = entity.getLocation();

                Material material = Material.getMaterial(item.toUpperCase());
                if (material == null) {
                    player.sendMessage(ChatColor.RED + "Ungültiger Gegenstandstyp.");
                    return;
                }

                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Boss Drop: " + item);
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                itemStack.setItemMeta(meta);

                world.dropItemNaturally(location, itemStack);
                player.sendMessage(ChatColor.GREEN + "Boss " + name + " hat " + item + " fallen gelassen!");
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "Boss nicht gefunden.");
    }

    private void killBoss(Player player, String name) {
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof LivingEntity && entity.getCustomName() != null && entity.getCustomName().equals(ChatColor.RED + name)) {
                World world = entity.getWorld();
                Location location = entity.getLocation();

                world.strikeLightningEffect(location);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String itemName = "item";
                        Material material = Material.getMaterial(itemName.toUpperCase());
                        if (material != null) {
                            ItemStack itemStack = new ItemStack(material);
                            ItemMeta meta = itemStack.getItemMeta();
                            meta.setDisplayName(ChatColor.GOLD + "Boss Drop");
                            itemStack.setItemMeta(meta);
                            world.dropItemNaturally(location, itemStack);
                        }
                    }
                }.runTaskLater(plugin, 20);

                for (Entity nearbyEntity : entity.getNearbyEntities(10, 10, 10)) {
                    if (nearbyEntity instanceof Creeper && nearbyEntity.getCustomName() != null && nearbyEntity.getCustomName().equals(ChatColor.RED + "Mini Creeper")) {
                        nearbyEntity.remove();
                    }
                }

                entity.remove();
                player.sendMessage(ChatColor.GREEN + "Boss " + name + " getötet!");
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "Boss nicht gefunden und getötet.");
    }

    private void deleteBoss(Player player, String name) {
        File bossFile = new File(bossesFolder, name + ".yml");
        if (!bossFile.exists()) {
            player.sendMessage(ChatColor.RED + "Boss nicht gefunden!");
            return;
        }

        if (bossFile.delete()) {
            player.sendMessage(ChatColor.GREEN + "Boss " + name + " gelöscht!");
        } else {
            player.sendMessage(ChatColor.RED + "Fehler beim Löschen des Bosses.");
        }
    }

    private Player getRandomPlayer() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) return null;
        return players.get(new Random().nextInt(players.size()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("create");
            suggestions.add("delete");
            suggestions.add("kill");
            suggestions.add("spawn");
            suggestions.add("drop");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            suggestions.add("<BossName>");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            suggestions.add("WITHER");
            suggestions.add("ENDER_DRAGON");
            suggestions.add("EVOKER");
            suggestions.add("WARDEN");
            suggestions.add("ZOMBIE");
            suggestions.add("SKELETON");
            suggestions.add("IRON_GOLEM");
            suggestions.add("BLAZE");
            suggestions.add("SPIDER");
            suggestions.add("CREEPER");
            suggestions.add("DROWNED");
            suggestions.add("HUSK");
            suggestions.add("PIGLIN_BRUTE");
            suggestions.add("STRAY");
            suggestions.add("PHANTOM");
            suggestions.add("MAGMA_CUBE");
            suggestions.add("SLIME");
            suggestions.add("SHULKER");
            suggestions.add("PILLAGER");
            suggestions.add("VINDICATOR");
            suggestions.add("ILLUSIONER");
            suggestions.add("RAVAGER");
            suggestions.add("ELDER_GUARDIAN");
            suggestions.add("GUARDIAN");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            File bossesFolder = new File(plugin.getDataFolder(), "bosses");
            if (bossesFolder.exists() && bossesFolder.isDirectory()) {
                for (File file : bossesFolder.listFiles()) {
                    if (file.getName().endsWith(".yml")) {
                        suggestions.add(file.getName().replace(".yml", ""));
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("kill")) {
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity instanceof LivingEntity && entity.getCustomName() != null && entity.getCustomName().startsWith(ChatColor.RED.toString())) {
                        suggestions.add(ChatColor.stripColor(entity.getCustomName()));
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            File bossesFolder = new File(plugin.getDataFolder(), "bosses");
            if (bossesFolder.exists() && bossesFolder.isDirectory()) {
                for (File file : bossesFolder.listFiles()) {
                    if (file.getName().endsWith(".yml")) {
                        suggestions.add(file.getName().replace(".yml", ""));
                    }
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("drop")) {
            File bossesFolder = new File(plugin.getDataFolder(), "bosses");
            if (bossesFolder.exists() && bossesFolder.isDirectory()) {
                for (File file : bossesFolder.listFiles()) {
                    if (file.getName().endsWith(".yml")) {
                        suggestions.add(file.getName().replace(".yml", ""));
                    }
                }
            }
        }

        return suggestions;
    }
}