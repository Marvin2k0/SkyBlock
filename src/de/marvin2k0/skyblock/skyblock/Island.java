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

    public Island(User owner, Location spawn)
    {
        this.owner = owner;
        this.uuid = UUID.randomUUID();
        this.spawn = spawn.add(0.5, 0, 0.5);
        saveData();

        spawn.subtract(0, 1, 0).getBlock().setType(Material.GOLD_BLOCK);

        Locations.setLocation("nextlocation", Locations.get("nextlocation").add(10000, 0, 0));
    }

    public Island(User owner)
    {
        this(owner, Locations.get("nextlocation"));
    }

    public Location getSpawn()
    {
        return this.spawn;
    }

    private void saveData()
    {
        Locations.setLocation(owner.getPlayer().getUniqueId() + ".spawn", spawn);
    }
}
