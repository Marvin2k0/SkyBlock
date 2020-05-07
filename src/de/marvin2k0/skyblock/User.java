package de.marvin2k0.skyblock;

import de.marvin2k0.skyblock.skyblock.Island;
import de.marvin2k0.skyblock.skyblock.IslandManager;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class User
{
    public static HashMap<Player, User> users = new HashMap<>();
    private static IslandManager is = IslandManager.getManager();

    private Player player;
    private Island island;

    public User(Player player)
    {
        this.player = player;
    }

    public void teleportToIsland()
    {
        if (island != null)
            player.teleport(island.getSpawn());
    }

    public void setIsland(Island island)
    {
        if (this.island == null)
            this.island = island;
    }

    public void sendMessage(String msg)
    {
        getPlayer().sendMessage(msg);
    }

    public Player getPlayer()
    {
        return player;
    }

    public String getName()
    {
        return getPlayer().getName();
    }

    public static User getUser(Player player)
    {
        if (!users.containsKey(player))
            users.put(player, new User(player));

        return users.get(player);
    }
}
