package de.marvin2k0.skyblock.minions;

import com.avaje.ebeaninternal.server.text.csv.CsvUtilReader;
import de.marvin2k0.skyblock.SkyBlock;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MinionsCommand implements CommandExecutor, Listener
{
    File file = new File(SkyBlock.plugin.getDataFolder().getPath() + "/minions.yml");
    FileConfiguration config = YamlConfiguration.loadConfiguration(file);

    public static HashMap<Location, Integer> minions = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Text.get("noplayer"));
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        if (can(player))
        {
            spawnArmorstand(player, location);
            saveArmorStand(player, location);
        }
        else
        {
            player.sendMessage(Text.get("minionsreached"));
        }

        return true;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event)
    {
        if (event.getBlock().getType() == Material.SKULL)
        {
            Skull skull = (Skull) event.getBlock().getState();
            Player player = event.getPlayer();
            Location blockLocation = event.getBlock().getLocation();

            if (!skull.getOwner().equals(player.getName()))
                return;

            event.setCancelled(true);

            spawnArmorstand(player, blockLocation);
            saveArmorStand(player, blockLocation);

            player.setItemInHand(null);
        }
    }

    @EventHandler
    public void onBreak(EntityDamageByEntityEvent event)
    {
        if (event.getEntityType() == EntityType.ARMOR_STAND && event.getDamager() instanceof Player)
        {
            event.setCancelled(true);

            Player player = (Player) event.getDamager();
            ArmorStand armorStand = (ArmorStand) event.getEntity();
            Location armorStandLocation = armorStand.getLocation();
            armorStand.remove();

            Bukkit.getScheduler().cancelTask(minions.get(event.getEntity().getLocation()));
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

    private void removeArmorStandFromConfig(Player player, Location loc)
    {
        reloadConfig();

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
            else
            {
                System.out.println("weiter weg");
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event)
    {
        if (event.getRightClicked().getType() == EntityType.ARMOR_STAND)
        {
            event.setCancelled(true);
            Inventory inv = getMinionInventory(event.getPlayer(), event.getRightClicked().getLocation()) == null ? Bukkit.createInventory(null, 27, "") : getMinionInventory(event.getPlayer(), event.getRightClicked().getLocation());

            event.getPlayer().openInventory(inv);
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
                return getInventory(path);
            }
        }

        return null;
    }

    private Inventory getInventory(String path)
    {
        Map<String, Object> section = config.getConfigurationSection(path).getValues(false);
        Inventory inv = Bukkit.createInventory(null, 36, "");

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            int amount = config.getInt(path + entry.getKey() + ".amount");
            String type = config.getString(path + entry.getKey() + ".type");

            if (Material.getMaterial(type) != null)
            {
                ItemStack item = new ItemStack(Material.getMaterial(type));
                item.setAmount(amount);

                inv.setItem(Integer.valueOf(entry.getKey()), item);
            }
        }

        return inv;
    }

    private boolean can(Player player)
    {
        if (!config.isSet(player.getUniqueId().toString()))
            return true;

        Map<String, Object> section = config.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        int amount = 0;

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            amount++;
        }

        return amount < Integer.valueOf(Text.get("maxminions", false));
    }

    private void saveArmorStand(Player player, Location location)
    {
        String path = player.getUniqueId() + "." + UUID.randomUUID();
        Location loc = player.getLocation();

        config.set(path, "");
        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());

        saveConfig();
    }

    private void spawnArmorstand(Player player, Location location)
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

        armorStand.setHelmet(head);
        armorStand.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
        armorStand.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
        armorStand.setBoots(new ItemStack(Material.LEATHER_BOOTS));

        openBlockInventory(player);

        int scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(SkyBlock.plugin, new Runnable()
        {
            @Override
            public void run()
            {
                miningProcess(location);
            }
        }, 0, 20 * 1);

        minions.put(location, scheduler);
    }

    public static void miningProcess(Location loc)
    {
        System.out.println("test");
        //TODO: blöcke im radius von 3 scann, ob typ, dann abbauen


    }

    private void openBlockInventory(Player player)
    {
        Inventory inv = Bukkit.createInventory(null, 36, "Choose block to farm");

        inv.addItem(new ItemStack(Material.COBBLESTONE));
        inv.addItem(new ItemStack(Material.GRASS));
        inv.addItem(new ItemStack(Material.DIRT));
        inv.addItem(new ItemStack(Material.SEEDS));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        if (event.getInventory() != null && event.getInventory().getName().equals("Choose block to farm"))
        {
            event.setCancelled(true);
        }
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
