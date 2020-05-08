package de.marvin2k0.skyblock;

import de.marvin2k0.skyblock.skyblock.IslandManager;
import de.marvin2k0.skyblock.skyblock.listeners.RankingListener;
import de.marvin2k0.skyblock.skyblock.world.SkyWorldGenerator;
import de.marvin2k0.skyblock.utils.Locations;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

import java.util.ArrayList;

public class SkyBlock extends JavaPlugin implements Listener
{
    public static String WORLD_NAME = "skyblockworld";
    private static World skyblockWorld = null;
    public static SkyBlock plugin;
    private ArrayList<Player> warning;

    private PluginDescriptionFile desc;
    private IslandManager is;

    @Override
    public void onEnable()
    {
        desc = getDescription();
        plugin = this;
        is = IslandManager.getManager();
        warning = new ArrayList<>();

        createSkyblockWorld();

        Text.setUp(this);
        Locations.setUp(this);
        IslandManager.setUp(this);
        RankingListener.initializeBlockValues();

        getCommand("island").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new RankingListener(), this);
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

        if (args[0].equalsIgnoreCase("create"))
        {
            User user = User.getUser(player);

            if (!is.hasIsland(user))
            {
                is.createNewIsland(user);
                user.teleportToIsland();
            }
            else
            {
                user.sendMessage(Text.get("alreadyownsisland"));
            }

            return true;
        }

        if (args[0].equalsIgnoreCase("island"))
        {
            if (args.length < 2)
            {
                player.sendMessage("§cUsage: /" + label + " island <player>");
                return true;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

            if (!offlinePlayer.hasPlayedBefore())
            {
                player.sendMessage(Text.get("playernotfound"));
                return true;
            }

            if (getConfig().isSet(offlinePlayer.getUniqueId().toString()))
            {
                String island = getConfig().getString(offlinePlayer.getUniqueId() + ".island");
                String rank = getConfig().getString(island + ".rank");
                String points = getConfig().getString(island + ".points");

                player.sendMessage(Text.get("islandinfo").replace("%player%", offlinePlayer.getName()).replace("%rank%", rank).replace("%points%", points));
            }
            else
            {
                player.sendMessage(Text.get("playerhasnoisland"));
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("invite"))
        {
            if (args.length < 2)
            {
                player.sendMessage("§cUsage: /" + label + " <invite> <player>");
                return true;
            }

            User user = User.getUser(player);

            if (!IslandManager.getManager().hasIsland(user))
            {
                user.sendMessage(Text.get("noisland"));
                return true;
            }

            Player target = null;

            if ((target = Bukkit.getPlayer(args[1])) == null || target.getName().equals(player.getName()))
            {
                user.sendMessage(Text.get("notonline"));
                return true;
            }

            //TODO: invites
            if (!warning.contains(player))
            {
                warning.add(player);
                player.sendMessage(Text.get("invitewarning"));
            }
            else
            {
                warning.remove(player);
                player.sendMessage("You invited " + target.getName() + " to your island");
            }

            return true;
        }

        player.sendMessage(Text.get("prefix") + " §cInvalid command!");
        return true;
    }

    private void createSkyblockWorld()
    {
        if (Bukkit.getWorld(WORLD_NAME) == null)
        {
            WorldCreator worldCreator = new WorldCreator(WORLD_NAME);
            worldCreator.generateStructures(false);
            worldCreator.generator(new SkyWorldGenerator());
            Bukkit.getServer().createWorld(worldCreator);
        }

        skyblockWorld = Bukkit.getWorld(WORLD_NAME);
        skyblockWorld.setAutoSave(true);
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
