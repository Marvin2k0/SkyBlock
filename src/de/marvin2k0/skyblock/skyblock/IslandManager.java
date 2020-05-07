package de.marvin2k0.skyblock.skyblock;

import de.marvin2k0.skyblock.SkyBlock;
import de.marvin2k0.skyblock.User;
import de.marvin2k0.skyblock.utils.Locations;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class IslandManager
{
    private static FileConfiguration config;
    private static SkyBlock plugin;
    private static int amount;

    public ArrayList<Island> islands = new ArrayList<>();

    public Island createNewIsland(User user)
    {
        if (getIsland(user) != null)
        {
            user.sendMessage(Text.get("alreadyownsisland"));

            return getIsland(user);
        }

        addIsland();

        Island island = new Island(user);
        user.setIsland(island);
        return island;
    }

    private void addIsland()
    {
        amount++;
        config.set("islands", amount);
        saveConfig();
    }

    public static void saveConfig()
    {
        plugin.saveConfig();
    }

    public static void setUp(SkyBlock plugin)
    {
        IslandManager.plugin = plugin;
        config = plugin.getConfig();

        if (!config.isSet("islands"))
            config.set("islands", 0);

        saveConfig();
        amount = config.getInt("islands");

        if (!config.isSet("nextlocation"))
        {
            Locations.setLocation("nextlocation", new Location(SkyBlock.getWorld(), 0, 75, 0));
        }

        Location nextLocation = Locations.get("nextlocation");
    }

    public boolean hasIsland(User user)
    {
        return config.isSet(user.getPlayer().getUniqueId().toString());
    }

    public static Island getIsland(User user)
    {
        if (config.isSet(user.getPlayer().getUniqueId().toString()))
            return new Island(user, Locations.get(user.getName() + ".spawn"));

        return null;
    }

    public static IslandManager getManager()
    {
        return new IslandManager();
    }

    private IslandManager()
    {
    }
}
