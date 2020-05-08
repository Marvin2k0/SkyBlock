package de.marvin2k0.skyblock.skyblock.listeners;

import de.marvin2k0.skyblock.SkyBlock;
import de.marvin2k0.skyblock.User;
import de.marvin2k0.skyblock.skyblock.Island;
import de.marvin2k0.skyblock.skyblock.IslandManager;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;

public class RankingListener implements Listener
{
    private static HashMap<Material, Integer> blockValues = new HashMap<>();
    private SkyBlock sky = SkyBlock.plugin;

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        User user = null;

        if ((user = User.getUser(event.getPlayer())) == null)
            return;

        user = User.getUser(event.getPlayer());

        if (!IslandManager.getManager().hasIsland(user))
        {
            event.setCancelled(true);
            return;
        }

        Island island = IslandManager.getIsland(user);
        Location spawn = island.getSpawn();

        if (event.getPlayer().getLocation().distance(spawn) >= 3000)
        {
            event.setCancelled(true);
            return;
        }

        if (blockValues.containsKey(event.getBlock().getType()))
        {
            int points = sky.getConfig().getInt(island.getUUID() + ".points");
            System.out.println("points before " + points);
            int levelBefore = (points - (points % 1000)) / 1000 + 1;
            points += blockValues.get(event.getBlock().getType());
            int levelAfter = (points - (points % 1000)) / 1000 + 1;
            sky.getConfig().set(island.getUUID() + ".points", points);
            sky.saveConfig();
            System.out.println("points after " + sky.getConfig().getInt(island.getUUID() + ".points"));

            if (levelAfter > levelBefore)
                user.sendMessage(Text.get("levelup").replace("%level%", levelAfter + ""));
            else if (levelAfter < levelBefore)
                user.sendMessage(Text.get("leveldown").replace("%level%", levelAfter + ""));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        User user = null;

        if ((user = User.getUser(event.getPlayer())) == null)
            return;

        user = User.getUser(event.getPlayer());

        if (!IslandManager.getManager().hasIsland(user))
        {
            event.setCancelled(true);
            return;
        }

        Island island = IslandManager.getIsland(user);
        Location spawn = island.getSpawn();

        if (event.getPlayer().getLocation().getWorld() == spawn.getWorld() && event.getPlayer().getLocation().distance(spawn) >= 3000)
        {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getLocation().distance(spawn.subtract(0.5, 1, 0.5)) <= 0.99)
        {
            event.setCancelled(true);
            return;
        }

        if (blockValues.containsKey(event.getBlock().getType()))
        {
            int points = sky.getConfig().getInt(island.getUUID() + ".points");
            int levelBefore = (points - (points % 1000)) / 1000 + 1;
            points -= blockValues.get(event.getBlock().getType());
            int levelAfter = (points - (points % 1000)) / 1000 + 1;
            sky.getConfig().set(island.getUUID() + ".points", points);
            sky.saveConfig();

            if (levelAfter > levelBefore)
                user.sendMessage(Text.get("levelup").replace("%level%", levelAfter + ""));
            else if (levelAfter < levelBefore)
                user.sendMessage(Text.get("leveldown").replace("%level%", levelAfter + ""));
        }
    }

    public static void initializeBlockValues()
    {
        blockValues.put(Material.GRASS, 1);
        blockValues.put(Material.DIRT, 1);
        blockValues.put(Material.STONE, 2);
        blockValues.put(Material.WOOD, 5);
        blockValues.put(Material.IRON_BLOCK, 30);
        blockValues.put(Material.GOLD_BLOCK, 50);
        blockValues.put(Material.DIAMOND_BLOCK, 100);
        blockValues.put(Material.EMERALD_BLOCK, 150);
    }
}
