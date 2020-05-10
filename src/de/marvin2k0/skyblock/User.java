package de.marvin2k0.skyblock;

import de.marvin2k0.skyblock.skyblock.Island;
import de.marvin2k0.skyblock.skyblock.IslandManager;
import de.marvin2k0.skyblock.utils.Locations;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class User
{
    public static HashMap<OfflinePlayer, User> users = new HashMap<>();
    private static IslandManager is = IslandManager.getManager();

    private OfflinePlayer player;
    private Island island;

    public User(OfflinePlayer player)
    {
        this.player = player;
    }

    public void teleportToIsland()
    {
        if (player.isOnline())
        {
            ((Player) player).teleport(Locations.get(player.getUniqueId() + ".spawn"));
        }
    }

    public void setIsland(Island island)
    {
        if (this.island == null)
            this.island = island;
    }

    public void sendMessage(String msg)
    {
        if (player.isOnline())
            ((Player) player).sendMessage(msg);
    }

    public OfflinePlayer getPlayer()
    {
        return player;
    }

    public String getName()
    {
        return getPlayer().getName();
    }

    public static User getUser(OfflinePlayer player)
    {
        if (!users.containsKey(player))
            users.put(player, new User(player));

        return users.get(player);
    }
}
