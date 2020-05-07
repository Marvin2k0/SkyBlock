package de.marvin2k0.skyblock;

import de.marvin2k0.skyblock.skyblock.IslandManager;
import de.marvin2k0.skyblock.skyblock.world.SkyWorldGenerator;
import de.marvin2k0.skyblock.utils.Locations;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyBlock extends JavaPlugin implements Listener
{
    public static String WORLD_NAME = "skyblockworld";
    private static World skyblockWorld = null;
    public static SkyBlock plugin;

    private PluginDescriptionFile desc;
    private IslandManager is;

    @Override
    public void onEnable()
    {
        createSkyblockWorld();

        Text.setUp(this);
        Locations.setUp(this);
        IslandManager.setUp(this);

        desc = getDescription();
        plugin = this;
        is = IslandManager.getManager();

        getCommand("island").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Text.get("noplayer"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0)
        {
            player.sendMessage("§7Plugin by §a" + desc.getAuthors().get(0) + " §7version §a" + desc.getVersion());
            return true;
        }

        if (args[0].equals("world") && player.hasPermission("sky.is.admin"))
        {
            if (Bukkit.getWorld(WORLD_NAME) == null)
            {
                player.sendMessage(Text.get("loading"));
                createSkyblockWorld();

                player.sendMessage(Text.get("worldcreated"));
                player.teleport(skyblockWorld.getSpawnLocation());
            }
            else
            {
                player.teleport(skyblockWorld.getSpawnLocation());
                player.sendMessage(Text.get("welcome"));
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("create"))
        {
            User user = User.getUser(player);

            if (!is.hasIsland(user))
            {
                is.createNewIsland(user);
                user.teleportToIsland();
            }
            else
            {
                user.sendMessage("du hast schon eine insel!");
            }

            return true;
        }

        return false;
    }

    private void createSkyblockWorld()
    {
        if (Bukkit.getWorld(WORLD_NAME) == null)
        {
            WorldCreator worldCreator = new WorldCreator(WORLD_NAME);
            worldCreator.generateStructures(false);
            worldCreator.generator(new SkyWorldGenerator());
            Bukkit.createWorld(worldCreator);
        }

        skyblockWorld = Bukkit.getWorld(WORLD_NAME);
        skyblockWorld.setSpawnLocation(0, 75, 0);
    }

    public static World getWorld()
    {
        return skyblockWorld;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        User user = User.getUser(event.getPlayer());
        System.out.println(is.hasIsland(user));
    }
}
