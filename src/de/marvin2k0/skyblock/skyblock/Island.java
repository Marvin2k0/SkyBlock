package de.marvin2k0.skyblock.skyblock;

import de.marvin2k0.skyblock.SkyBlock;
import de.marvin2k0.skyblock.User;
import de.marvin2k0.skyblock.utils.Locations;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.UUID;

public class Island
{
    private static IslandManager is = IslandManager.getManager();
    private static SkyBlock sky = SkyBlock.plugin;

    private Location spawn;
    private User owner;
    private UUID uuid;

    public Island(User owner, Location spawn, UUID uuid)
    {
        this.owner = owner;
        this.spawn = spawn;
        this.uuid = uuid;
    }

    public Island(User owner, Location spawn)
    {
        this.owner = owner;
        this.uuid = sky.getConfig().getString(owner.getPlayer().getUniqueId() + ".island") == null ? UUID.randomUUID() : UUID.fromString(sky.getConfig().getString(owner.getPlayer().getUniqueId() + ".island"));
        this.spawn = spawn;
        saveData();
    }

    public Island(User owner)
    {
        this(owner, Locations.get("nextlocation").add(0.5, 0, 0.5));

        Locations.setLocation("nextlocation", Locations.get("nextlocation").add(10000, 0, 0));

        for (int x = -2; x < 3; x++)
        {
            for (int y = -4; y < 0; y++)
            {
                for (int z = -2; z < 3; z++)
                {
                    spawn.getWorld().getBlockAt(spawn.getBlockX() + x, spawn.getBlockY() + y, spawn.getBlockZ() + z).setType(y == -1 ? Material.GRASS : Material.DIRT);
                }
            }
        }

        spawn.subtract(0, 1, 0).getBlock().setType(Material.GOLD_BLOCK);
    }

    public Location getSpawn()
    {
        return this.spawn;
    }

    public UUID getUUID()
    {
        return this.uuid;
    }

    private void saveData()
    {
        Locations.setLocation(owner.getPlayer().getUniqueId() + ".spawn", spawn);
    }

    @Override
    public String toString()
    {
        return owner.getName() + "'s island";
    }
}
