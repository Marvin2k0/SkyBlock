package de.marvin2k0.skyblock.skyblock.listeners;

import de.marvin2k0.skyblock.SkyBlock;
import de.marvin2k0.skyblock.User;
import de.marvin2k0.skyblock.skyblock.Island;
import de.marvin2k0.skyblock.skyblock.IslandManager;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RankingListener implements Listener
{
    private static HashMap<Material, Integer> blockValues = new HashMap<>();
    private static SkyBlock sky = SkyBlock.plugin;

    private static File file = new File(sky.getDataFolder().getPath() + "values.yml");
    private static FileConfiguration blockValuesConfig = YamlConfiguration.loadConfiguration(file);

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
            int levelBefore = (points - (points % 1000)) / 1000 + 1;
            points += blockValues.get(event.getBlock().getType());
            int levelAfter = (points - (points % 1000)) / 1000 + 1;
            sky.getConfig().set(island.getUUID() + ".points", points);
            sky.saveConfig();

            if (levelAfter > levelBefore)
            {
                user.sendMessage(Text.get("levelup").replace("%level%", levelAfter + ""));
                sort();
            }
            else if (levelAfter < levelBefore)
            {
                user.sendMessage(Text.get("leveldown").replace("%level%", levelAfter + ""));
                sort();
            }
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
            {
                user.sendMessage(Text.get("levelup").replace("%level%", levelAfter + ""));
                sort();
            }
            else if (levelAfter < levelBefore)
            {
                user.sendMessage(Text.get("leveldown").replace("%level%", levelAfter + ""));
                sort();
            }
        }
    }

    private void sort()
    {
        HashMap<String, Integer> islands = new HashMap<>();
        HashMap<Integer, String> islandPoints = new HashMap<>();
        Map<String, Object> section = sky.getConfig().getConfigurationSection("").getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            if (sky.getConfig().isSet(entry.getKey() + ".rank"))
            {
                islands.put(entry.getKey(), sky.getConfig().getInt(entry.getKey() + ".rank"));
                islandPoints.put(sky.getConfig().getInt(entry.getKey() + ".points"), entry.getKey());
            }
        }

        int nextHigh = -99, nextLow = -99;
        int last = -99;
        int lastIndex = -99;

        String[] sorted = new String[islands.size() * 2 + 2];

        for (Map.Entry<String, Integer> entry : islands.entrySet())
        {
            if (last == -99)
            {
                sorted[sorted.length / 2 - 1] = entry.getKey();
                lastIndex = sorted.length / 2 - 1;
            }
            else
            {
                int now = sky.getConfig().getInt(entry.getKey() + ".points");

                if (now <= last)
                {
                    sorted[nextLow] = entry.getKey();
                    lastIndex = nextLow;
                }
                else
                {
                    sorted[nextHigh] = entry.getKey();
                    lastIndex = nextHigh;
                }
            }

            last = sky.getConfig().getInt(entry.getKey() + ".points");
            nextHigh = sorted.length - lastIndex / 2 - 1;
            nextLow = lastIndex / 2 - 1;

            System.out.println(last + " at index " + lastIndex);
        }

        String[] fSorted = new String[islands.size()];
        int index = 0;

        for (int i = sorted.length - 1; i >= 0; i--)
        {
            if (sorted[i] != null)
            {
                System.out.println(sorted[i]);
                fSorted[index] = sorted[i];
                index++;
            }
        }

        for (int i = 0; i < fSorted.length; i++)
        {
            int rank = i + 1;
            System.out.println(fSorted[i] + " ist platz " + rank);
            sky.getConfig().set(fSorted[i] + ".rank", rank);
        }

        sky.saveConfig();
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


        Map<String, Object> section = sky.getConfig().getConfigurationSection("blocks").getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            blockValues.put(Material.getMaterial(entry.getKey()), Integer.valueOf(entry.getValue().toString()));
        }
    }
}
