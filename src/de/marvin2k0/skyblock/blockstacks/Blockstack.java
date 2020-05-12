package de.marvin2k0.skyblock.blockstacks;

import de.marvin2k0.skyblock.SkyBlock;
import de.marvin2k0.skyblock.skyblock.listeners.RankingListener;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Blockstack implements CommandExecutor, Listener
{
    File file = new File(SkyBlock.plugin.getDataFolder().getPath() + "/blockstacks.yml");
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Text.get("noplayer"));
            return true;
        }

        Player player = (Player) sender;

        if (player.getInventory().firstEmpty() == -1)
        {
            player.sendMessage(Text.get("inventoryfull"));
            return true;
        }

        player.getInventory().addItem(getSkull());
        player.sendMessage(Text.get("skull"));

        return true;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled())
            return;

        if (inRadiusOfStacker(event.getPlayer(), event.getBlock().getLocation()) && event.getBlock().getType() != Material.SKULL)
        {
            Player player = event.getPlayer();
            Location stacker = getNearestStacker(event.getPlayer(), event.getBlock().getLocation());

            if (stacker == null)
                return;

            ArmorStand armorstand = null;
            String uuid = getStackerUUID(player, stacker);

            reloadConfig();

            for (Entity e : stacker.getWorld().getNearbyEntities(stacker, 1, 1, 1))
            {
                if (e instanceof ArmorStand)
                {
                    armorstand = (ArmorStand) e;
                    break;
                }
            }

            if (armorstand == null)
            {
                armorstand = (ArmorStand) stacker.getWorld().spawnEntity(stacker.add(0.5, -0.75, 0.5), EntityType.ARMOR_STAND);
                stacker.subtract(0.5, -0.75, 0.5);
            }

            armorstand.setVisible(false);
            armorstand.setGravity(false);
            armorstand.setSmall(false);
            armorstand.setCustomNameVisible(true);

            Material mat = null;

            System.out.println(event.getBlock().getType());

            if (config.getInt(player.getUniqueId() + "." + uuid + ".amount") == 0)
            {
                config.set(player.getUniqueId() + "." + uuid + ".type", event.getBlock().getType().toString());
                config.set(player.getUniqueId() + "." + uuid + ".amount", 1);

                saveConfig();

                event.getBlock().setType(Material.AIR);

                mat = Material.getMaterial(config.getString(player.getUniqueId() + "." + uuid + ".type"));
                int amount = config.getInt(player.getUniqueId() + "." + uuid + ".amount");
                String name = Text.get("stackertitle", false).replace("%amount%", amount + "").replace("%material%", mat.toString());

                System.out.println(name);
                stacker.getBlock().setType(mat);

                armorstand.setCustomName(name);
            }
            else
            {
                mat = Material.getMaterial(config.getString(player.getUniqueId() + "." + uuid + ".type"));

                if (event.getBlock().getType() == mat)
                {
                    System.out.println("gleicher blck");
                    event.getBlock().setType(Material.AIR);

                    int amount = config.getInt(player.getUniqueId() + "." + uuid + ".amount");

                    config.set(player.getUniqueId() + "." + uuid + ".amount", amount += 1);
                    saveConfig();

                    String name = Text.get("stackertitle", false).replace("%amount%", amount + "").replace("%material%", mat.toString());

                    System.out.println(name);

                    armorstand.setCustomName(name);
                }
            }
            return;
        }

        if (event.getItemInHand().getType() == Material.SKULL_ITEM && event.getItemInHand().hasItemMeta())
        {
            ItemStack skull = event.getItemInHand();

            if (skull.getItemMeta().getDisplayName().equalsIgnoreCase("§7Empty Blockstacker"))
            {
                addSkull(event.getPlayer(), event.getBlock().getLocation());
            }
            else if (skull.getItemMeta().getDisplayName().equalsIgnoreCase("§6Blockstacker"))
            {
                if (!skull.getItemMeta().hasLore())
                    return;

                List<String> lore = skull.getItemMeta().getLore();

                int amount = Integer.valueOf(lore.get(0).split(" ")[0]);
                Material material = Material.getMaterial(lore.get(0).split(" ")[1]);

                addSkull(event.getPlayer(), event.getBlock().getLocation(), amount, material);

                event.getPlayer().setItemInHand(null);
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event)
    {
        if (event.isCancelled())
            return;

        if (isSkull(event.getPlayer(), event.getBlock().getLocation()))
        {
            event.setCancelled(true);
            removeSkull(event.getPlayer(), event.getBlock().getLocation());
            event.getBlock().setType(Material.AIR);
        }
    }

    private String getStackerUUID(Player player, Location loc)
    {
        if (!config.isSet(player.getUniqueId().toString()))
            return null;

        Map<String, Object> section = config.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        reloadConfig();

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            String path = player.getUniqueId() + "." + entry.getKey();

            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");

            Location config = new Location(world, x, y, z);

            if (loc.distance(config) <= Double.valueOf(Text.get("blockstackerradius", false)))
                return entry.getKey();
        }

        return null;
    }

    private Location getNearestStacker(Player player, Location loc)
    {
        reloadConfig();
        if (!config.isSet(player.getUniqueId().toString()))
        {
            return null;
        }

        Map<String, Object> section = config.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        Location[] stacker = new Location[section.entrySet().size()];
        double[] distance = new double[section.entrySet().size()];

        int i = 0;

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            String path = player.getUniqueId() + "." + entry.getKey();

            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");

            Location configLocation = new Location(world, x, y, z);

            if (loc.distance(configLocation) <= Double.valueOf(Text.get("blockstackerradius", false)))
            {
                stacker[i] = configLocation;
                distance[i] = loc.distance(configLocation);
                i++;
            }
        }

        return stacker[getMin(distance)];
    }

    private int getMin(double[] arr)
    {
        double min = arr[0];
        int minIndex = 0;

        for (int i = 0; i < arr.length; i++)
        {
            if (arr[i] < min)
            {
                min = arr[i];
                minIndex = i;
            }
        }

        return minIndex;
    }

    private void removeSkull(Player player, Location loc)
    {
        if (!config.isSet(player.getUniqueId().toString()))
            return;

        Map<String, Object> section = config.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            String path = player.getUniqueId() + "." + entry.getKey();

            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");

            Location configLocation = new Location(world, x, y, z);

            if (loc.distance(configLocation) <= 1)
            {
                String material = config.getString(path + ".type");
                int amount = config.getInt(path + ".amount");

                ItemStack item = new ItemStack(Material.getMaterial(material));
                item.setAmount(amount);

                boolean drop = true;

                decreaseLevel(player, item);

                if (drop)
                {
                    configLocation.getWorld().dropItem(configLocation, item);
                }
                else
                {
                    ItemStack skull = new ItemStack(Material.SKULL_ITEM);
                    ItemMeta meta = skull.getItemMeta();
                    meta.setDisplayName("§6Blockstacker");
                    ArrayList<String> lore = new ArrayList<>();
                    lore.add(amount + " " + material);
                    meta.setLore(lore);
                    skull.setItemMeta(meta);
                    player.getInventory().addItem(skull);
                }

                for (Entity e : configLocation.getWorld().getNearbyEntities(configLocation, 1, 1, 1))
                {
                    if (e instanceof ArmorStand)
                        e.remove();
                }

                config.set(path, null);
                saveConfig();

                return;
            }
        }
    }

    private void increaseLevel(Player player, ItemStack item)
    {
        SkyBlock.plugin.reloadConfig();

        String islandUUID = SkyBlock.plugin.getConfig().getString(player.getUniqueId() + ".island");

        int amount = RankingListener.blockValues.get(item.getType()) * (item.getAmount());
        int points = SkyBlock.plugin.getConfig().getInt(islandUUID + ".points");

        System.out.println(points + " - " + amount);

        SkyBlock.plugin.getConfig().set(islandUUID + ".points", (points + amount));
        SkyBlock.plugin.saveConfig();

        points = SkyBlock.plugin.getConfig().getInt(islandUUID + ".points");

        int level = (points - (points % 1000)) / 1000 + 1;

        player.sendMessage(Text.get("levelup").replace("%level%", level + ""));
    }

    private void decreaseLevel(Player player, ItemStack item)
    {
        SkyBlock.plugin.reloadConfig();

        String islandUUID = SkyBlock.plugin.getConfig().getString(player.getUniqueId() + ".island");

        int amount = RankingListener.blockValues.get(item.getType()) * (item.getAmount() - 1);
        int points = SkyBlock.plugin.getConfig().getInt(islandUUID + ".points");

        System.out.println(points + " - " + amount);

        SkyBlock.plugin.getConfig().set(islandUUID + ".points", (points - amount - 10));
        SkyBlock.plugin.saveConfig();

        points = SkyBlock.plugin.getConfig().getInt(islandUUID + ".points");

        int level = (points - (points % 1000)) / 1000 + 1;

        player.sendMessage(Text.get("leveldown").replace("%level%", level + ""));
    }

    private boolean isSkull(Player player, Location loc)
    {
        if (!config.isSet(player.getUniqueId().toString()))
            return false;

        Map<String, Object> section = config.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            String path = player.getUniqueId() + "." + entry.getKey();

            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");

            Location config = new Location(world, x, y, z);

            if (loc.distance(config) <= 1)
                return true;
        }

        return false;
    }

    private boolean inRadiusOfStacker(Player player, Location loc)
    {
        if (!config.isSet(player.getUniqueId().toString()))
            return false;

        Map<String, Object> section = config.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            String path = player.getUniqueId() + "." + entry.getKey();

            World world = Bukkit.getWorld(config.getString(path + ".world"));
            double x = config.getDouble(path + ".x");
            double y = config.getDouble(path + ".y");
            double z = config.getDouble(path + ".z");

            Location config = new Location(world, x, y, z);

            if (loc.distance(config) <= Double.valueOf(Text.get("blockstackerradius", false)))
                return true;
        }

        return false;
    }

    private void addSkull(Player player, Location location, int amount, Material material)
    {
        UUID uuid = UUID.randomUUID();

        //TODO:
        config.set(player.getUniqueId() + "." + uuid + ".type", material.toString());
        config.set(player.getUniqueId() + "." + uuid + ".amount", amount);
        config.set(player.getUniqueId() + "." + uuid + ".world", location.getWorld().getName());
        config.set(player.getUniqueId() + "." + uuid + ".x", location.getX());
        config.set(player.getUniqueId() + "." + uuid + ".y", location.getY());
        config.set(player.getUniqueId() + "." + uuid + ".z", location.getZ());

        saveConfig();

        ArmorStand armorstand = (ArmorStand) location.getWorld().spawnEntity(location.add(0.5, -0.75, 0.5), EntityType.ARMOR_STAND);
        armorstand.setVisible(false);
        armorstand.setGravity(false);
        armorstand.setSmall(false);
        armorstand.setCustomNameVisible(true);
        armorstand.setCustomName(Text.get("stackertitle", false).replace("%amount%", amount + "").replace("%material%", material.toString()));

        location.add(0, 1, 0).getBlock().setType(material);
        ItemStack item = new ItemStack(material);
        item.setAmount(amount);

        increaseLevel(player, item);
    }

    private void addSkull(Player player, Location location)
    {
        UUID uuid = UUID.randomUUID();

        config.set(player.getUniqueId() + "." + uuid + ".type", 0);
        config.set(player.getUniqueId() + "." + uuid + ".amount", 0);
        config.set(player.getUniqueId() + "." + uuid + ".world", location.getWorld().getName());
        config.set(player.getUniqueId() + "." + uuid + ".x", location.getX());
        config.set(player.getUniqueId() + "." + uuid + ".y", location.getY());
        config.set(player.getUniqueId() + "." + uuid + ".z", location.getZ());

        saveConfig();
    }

    private ItemStack getSkull()
    {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM);
        ItemMeta meta = skull.getItemMeta();
        meta.setDisplayName("§7Empty Blockstacker");
        skull.setItemMeta(meta);

        return skull;
    }

    private void reloadConfig()
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

    private void saveConfig()
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
