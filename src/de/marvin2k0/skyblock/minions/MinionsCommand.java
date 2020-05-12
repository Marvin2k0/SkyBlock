package de.marvin2k0.skyblock.minions;

import de.marvin2k0.skyblock.SkyBlock;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.*;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MinionsCommand implements CommandExecutor, Listener
{
    static File file = new File(SkyBlock.plugin.getDataFolder().getPath() + "/minions.yml");
    static FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    public static HashMap<Location, Integer> minions = new HashMap<>();
    public static HashMap<Player, Location> looking = new HashMap<>();

    private static Random random = new Random();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Text.get("noplayer"));
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("minion"))
        {
            Location location = player.getLocation().getBlock().getLocation();

            if (can(player))
            {
                spawnArmorstand(player, UUID.randomUUID().toString(), location);
            }
            else
            {
                player.sendMessage(Text.get("minionsreached"));
            }
        }

        else if (label.equalsIgnoreCase("setlimit"))
        {
            if (args.length != 2)
            {
                player.sendMessage("§cUsage:/setlimit <player> <limit>");
                return true;
            }

            if (!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore())
            {
                player.sendMessage(Text.get("playerhasnoisland"));
                return true;
            }

            if (!SkyBlock.plugin.getConfig().isSet(player.getUniqueId() + ".island"))
            {
                player.sendMessage(Text.get("playerhasnoisland"));
                return true;
            }

            String islandUUID = SkyBlock.plugin.getConfig().getString(player.getUniqueId() + ".island");
            int limit = 2;

            try
            {
                limit = Integer.valueOf(args[1]);
                SkyBlock.plugin.getConfig().set(islandUUID + ".maxminions", limit);
                SkyBlock.plugin.saveConfig();
                player.sendMessage("§aIsland data changed!");
                return true;
            }
            catch(Exception e)
            {
                player.sendMessage("§cPlease only enter integers!");
                return true;
            }
        }

        return true;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getType() == Material.SKULL)
        {
            if (((Skull) event.getBlock().getState()).getSkullType() != SkullType.PLAYER)
            {
                return;
            }

            if (can(event.getPlayer()))
            {
                Skull skull = (Skull) event.getBlock().getState();
                Player player = event.getPlayer();
                Location blockLocation = event.getBlock().getLocation();

                if (!skull.getOwner().equals(player.getName()))
                    return;

                event.setCancelled(true);

                spawnArmorstand(Bukkit.getOfflinePlayer(player.getUniqueId()), UUID.randomUUID().toString(), blockLocation);

                player.setItemInHand(null);
            }
            else
            {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Text.get("minionsreached"));
                return;
            }
        }
    }

    @EventHandler
    public void onBreak(EntityDamageByEntityEvent event)
    {
        if (event.getEntityType() == EntityType.ARMOR_STAND && event.getDamager() instanceof Player)
        {
            event.setCancelled(true);
            Player player = (Player) event.getDamager();

            if (!isMinion(player, event.getEntity().getLocation()))
            {
                return;
            }

            ArmorStand armorStand = (ArmorStand) event.getEntity();
            Location armorStandLocation = armorStand.getLocation();
            armorStand.remove();

            Bukkit.getScheduler().cancelTask(minions.get(armorStandLocation));
            minions.remove(event.getEntity().getLocation());

            ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwner(player.getName());
            meta.setDisplayName("§6" + player.getName() + "'s minion");
            head.setItemMeta(meta);

            player.getInventory().addItem(head);

            removeArmorStandFromConfig(player, armorStandLocation);
        }
    }

    private boolean isMinion(Player player, Location loc)
    {
        if (!config.isSet(player.getUniqueId().toString()))
            return false;

        for (Map.Entry<String, Object> location : config.getConfigurationSection(player.getUniqueId().toString()).getValues(false).entrySet())
        {
            String path = player.getUniqueId() + "." + location.getKey();

            if (!config.isSet(path + ".world"))
                continue;

            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double y = config.getDouble(path + ".y");
            double x = config.getDouble(path + ".x");
            double z = config.getDouble(path + ".z");
            double yaw = config.getDouble(path + ".yaw");
            double pitch = config.getDouble(path + ".pitch");

            Location locationObj = new Location(world, x, y, z, (float) yaw, (float) pitch);

            if (loc.distance(locationObj) <= 1)
            {
                return true;
            }
        }

        return false;
    }

    private void removeArmorStandFromConfig(Player player, Location loc)
    {
        reloadConfig();

        String islandUUID = SkyBlock.plugin.getConfig().getString(player.getUniqueId() + ".island");
        int amount = SkyBlock.plugin.getConfig().getInt(islandUUID + ".minions");

        SkyBlock.plugin.getConfig().set(islandUUID + ".minions", amount - 1);
        SkyBlock.plugin.saveConfig();

        for (Map.Entry<String, Object> location : config.getConfigurationSection(player.getUniqueId().toString()).getValues(false).entrySet())
        {
            String path = player.getUniqueId() + "." + location.getKey();

            if (!config.isSet(path + ".world"))
                continue;

            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double y = config.getDouble(path + ".y");
            double x = config.getDouble(path + ".x");
            double z = config.getDouble(path + ".z");
            double yaw = config.getDouble(path + ".yaw");
            double pitch = config.getDouble(path + ".pitch");

            Location locationObj = new Location(world, x, y, z, (float) yaw, (float) pitch);

            if (loc.distance(locationObj) <= 0.99)
            {
                config.set(path, null);
                saveConfig();
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event)
    {
        if (event.getRightClicked().getType() == EntityType.ARMOR_STAND)
        {
            if (!isMinion(event.getPlayer(), event.getRightClicked().getLocation()))
            {
                return;
            }

            event.setCancelled(true);
            Inventory inv = getMinionInventory(event.getPlayer(), event.getRightClicked().getLocation()) == null ? Bukkit.createInventory(null, 27, "") : getMinionInventory(event.getPlayer(), event.getRightClicked().getLocation());

            event.getPlayer().openInventory(inv);
            looking.put(event.getPlayer(), event.getRightClicked().getLocation());
        }
    }

    public Inventory getMinionInventory(Player player, Location loc)
    {
        if (!config.isSet(player.getUniqueId().toString()))
            return null;

        Map<String, Object> section = config.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            String path = player.getUniqueId() + "." + entry.getKey();

            String world = config.getString(path + ".world");
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");
            double yaw = config.getDouble(path + ".yaw");
            double pitch = config.getDouble(path + ".pitch");

            Location config = new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);


            if (loc.distance(config) <= 1)
            {
                return getInventory(player, path);
            }
        }

        return null;
    }

    private Inventory getInventory(OfflinePlayer player, String path)
    {
        if (!config.isSet(path))
            return null;
        path = path + ".inventory";

        Map<String, Object> section = config.getConfigurationSection(path).getValues(false);
        Inventory inv = Bukkit.createInventory(null, 36, player.getName());

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            int amount = config.getInt(path + "." + entry.getKey() + ".amount");
            String type = entry.getKey();

            if (Material.getMaterial(type) != null)
            {
                ItemStack item = new ItemStack(Material.getMaterial(type));
                item.setAmount(amount);

                inv.addItem(item);
            }
        }

        return inv;
    }

    private boolean can(Player player)
    {
        if (!SkyBlock.plugin.getConfig().isSet(player.getUniqueId().toString() + ".island"))
            return true;

        String islandUUID = SkyBlock.plugin.getConfig().getString(player.getUniqueId() + ".island");
        int amountAllowed = SkyBlock.plugin.getConfig().getInt(islandUUID + ".maxminions");

        int amount = 0;

        if (!SkyBlock.plugin.getConfig().isSet(islandUUID + ".minions"))
        {
            SkyBlock.plugin.getConfig().set(islandUUID + ".minions", 0);
        }
        else
        {
            amount = SkyBlock.plugin.getConfig().getInt(islandUUID + ".minions");
        }

        if (amount < amountAllowed)
        {
            SkyBlock.plugin.getConfig().set(islandUUID + ".minions", amount + 1);
        }

        SkyBlock.plugin.saveConfig();

        return amount < amountAllowed;
    }

    private void saveArmorStand(OfflinePlayer player, String uuid, Location location)
    {
        String path = player.getUniqueId() + "." + uuid;

        config.set(path, "");
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());

        saveConfig();
    }

    private void spawnArmorstand(OfflinePlayer player, String uuid, Location location)
    {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location.add(0.5, 0, 0.5), EntityType.ARMOR_STAND);
        armorStand.setVisible(true);
        armorStand.setArms(true);
        armorStand.setSmall(true);
        armorStand.setGravity(false);

        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(player.getName());
        head.setItemMeta(meta);

        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        leatherArmorMeta.setColor(Color.GRAY);
        chestplate.setItemMeta(leatherArmorMeta);

        ItemStack leggins = new ItemStack(Material.LEATHER_LEGGINGS);
        leatherArmorMeta = (LeatherArmorMeta) leggins.getItemMeta();
        leatherArmorMeta.setColor(Color.GRAY);
        leggins.setItemMeta(leatherArmorMeta);

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        leatherArmorMeta = (LeatherArmorMeta) boots.getItemMeta();
        leatherArmorMeta.setColor(Color.GRAY);
        boots.setItemMeta(leatherArmorMeta);

        armorStand.setHelmet(head);
        armorStand.setChestplate(chestplate);
        armorStand.setLeggings(leggins);
        armorStand.setBoots(boots);

        saveArmorStand(Bukkit.getOfflinePlayer(player.getUniqueId()), uuid, location);

        int scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(SkyBlock.plugin, new Runnable()
        {
            @Override
            public void run()
            {
                miningProcess(player.getUniqueId().toString(), uuid, location);
            }
        }, 20 * getSeconds(player, player.getUniqueId().toString(), false), 20 * getSeconds(player, player.getUniqueId().toString(), true));

        minions.put(armorStand.getLocation(), scheduler);
    }

    public static int getSeconds(OfflinePlayer player, String playerUUID, boolean msg)
    {
        SkyBlock.plugin.reloadConfig();

        if (!SkyBlock.plugin.getConfig().isSet(playerUUID + ".island"))
        {
            if (player.isOnline())
                ((Player) player).sendMessage(Text.get("playerhasnoisland"));
            return Integer.MAX_VALUE;
        }

        String islandUUID = SkyBlock.plugin.getConfig().getString(playerUUID + ".island");
        int points = SkyBlock.plugin.getConfig().getInt(islandUUID + ".points");
        int level = (points - (points % 1000)) / 1000 + 1;
        int seconds = 60;

        if (level <= 5)
        {
            seconds = 100 / level;
        }
        else
        {
            seconds = 100 / level / 2;
        }

        if (seconds < 1)
            seconds = 1;

        if (player.isOnline() && msg)
            ((Player) player).sendMessage(Text.get("mininginfo").replace("%seconds%", seconds + "").replace("%level%", level + ""));

        return seconds;
    }

    public static void miningProcess(String uuidPlayer, String uuid, Location loc)
    {
        int x = random.nextInt(5) - 2;
        int z = random.nextInt(5) - 2;

        while (x == 0 && z == 0)
        {
            x = random.nextInt(5) - 2;
            z = random.nextInt(5) - 2;
        }

        int x2 = random.nextInt(5) - 2;
        int z2 = random.nextInt(5) - 2;

        while (x2 == 0 && z2 == 0)
        {
            x2 = random.nextInt(5) - 2;
            z2 = random.nextInt(5) - 2;
        }

        loc.add(x2, -1, z2).getBlock().setType(Material.COBBLESTONE);
        loc.subtract(x2, -1, z2);

        int x3 = random.nextInt(5) - 2;
        int z3 = random.nextInt(5) - 2;

        while (x3 == 0 && z3 == 0)
        {
            x3 = random.nextInt(5) - 2;
            z3 = random.nextInt(5) - 2;
        }

        loc.add(x3, -1, z3).getBlock().setType(Material.COBBLESTONE);
        loc.subtract(x3, -1, z3);

        String material = loc.getWorld().getBlockAt((int) loc.getX() + x, (int) loc.getY() - 1, (int) loc.getZ() + z).getType().toString();

        addItem(uuidPlayer, uuid, material);

        loc.add(x, -1, z).getBlock().setType(Material.AIR);
        loc.subtract(x, -1, z);
    }

    private static void addItem(String uuidPlayer, String uuid, String material)
    {
        if (material.equals("AIR"))
            return;

        String path = uuidPlayer + "." + uuid;

        if (!config.isSet(path + ".inventory"))
        {
            config.set(path + ".inventory." + material + ".amount", 1);
        }

        int amount = 0;

        if (config.isSet(path + ".inventory." + material))
            amount = config.getInt(path + ".inventory." + material + ".amount");

        config.set(path + ".inventory." + material + ".amount", ++amount);
        saveConfig();
    }

    private void openBlockInventory(OfflinePlayer player)
    {
        Inventory inv = Bukkit.createInventory(null, 36, "Choose block to farm");

        inv.addItem(new ItemStack(Material.COBBLESTONE));
        inv.addItem(new ItemStack(Material.GRASS));
        inv.addItem(new ItemStack(Material.DIRT));
        inv.addItem(new ItemStack(Material.SEEDS));

        ((Player) player).openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if (event.getInventory() != null && event.getInventory().getName().equals("Choose block to farm"))
        {
            event.setCancelled(true);
        }

        if (event.getInventory() != null && event.getCurrentItem() != null && event.getInventory().getName().equals(event.getWhoClicked().getName()))
        {
            Player player = (Player) event.getWhoClicked();

            if (!looking.containsKey(player))
            {
                event.setCancelled(true);
                return;
            }

            Location loc = looking.get(player);

            for (Map.Entry<String, Object> entry : config.getConfigurationSection(player.getUniqueId().toString()).getValues(false).entrySet())
            {
                String path = player.getUniqueId() + "." + entry.getKey();

                String world = config.getString(path + ".world");
                double x = config.getDouble(path + ".x");
                double y = config.getDouble(path + ".y");
                double z = config.getDouble(path + ".z");
                double yaw = config.getDouble(path + ".yaw");
                double pitch = config.getDouble(path + ".pitch");

                Location configLoc = new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);


                if (loc.distance(configLoc) <= 1)
                {
                    ItemStack item = event.getCurrentItem();

                    if (event.isShiftClick() || event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PLACE_SOME)
                    {
                        event.setCancelled(true);
                        int amount = config.getInt(path + ".inventory." + event.getCurrentItem().getType() + ".amount");
                        item.setAmount(amount);

                        config.set(path + ".inventory." + event.getCurrentItem().getType() + ".amount", 0);
                        config.set(path + ".inventory." + event.getCurrentItem().getType(), null);
                        saveConfig();

                        event.getInventory().remove(item);

                        player.getInventory().addItem(item);
                    }
                }
            }
        }
    }

    private static void reloadConfig()
    {
        try
        {
            config.load(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e)
        {
            e.printStackTrace();
        }
    }

    private static void saveConfig()
    {
        try
        {
            config.save(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}