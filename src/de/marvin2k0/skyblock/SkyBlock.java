package de.marvin2k0.skyblock;

import de.marvin2k0.skyblock.blockstacks.Blockstack;
import de.marvin2k0.skyblock.minions.MinionsCommand;
import de.marvin2k0.skyblock.skyblock.IslandManager;
import de.marvin2k0.skyblock.skyblock.listeners.RankingListener;
import de.marvin2k0.skyblock.skyblock.world.SkyWorldGenerator;
import de.marvin2k0.skyblock.utils.CountdownTimer;
import de.marvin2k0.skyblock.utils.Locations;
import de.marvin2k0.skyblock.utils.Text;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SkyBlock extends JavaPlugin implements Listener
{
    public static String WORLD_NAME = "skyblockworld";
    private static World skyblockWorld = null;
    public static SkyBlock plugin;
    private HashMap<User, User> invites;
    private ArrayList<Player> warning;

    private File file;
    private FileConfiguration config;

    private PluginDescriptionFile desc;
    private IslandManager is;

    @Override
    public void onEnable()
    {
        desc = getDescription();
        plugin = this;
        is = IslandManager.getManager();
        warning = new ArrayList<>();
        invites = new HashMap<>();

        file = new File(getDataFolder().getPath() + "/minions.yml");
        config = YamlConfiguration.loadConfiguration(file);

        createSkyblockWorld();

        Text.setUp(this);
        Locations.setUp(this);
        IslandManager.setUp(this);
        RankingListener.initializeBlockValues();

        getCommand("island").setExecutor(this);
        getCommand("minion").setExecutor(new MinionsCommand());
        getCommand("setlimit").setExecutor(new MinionsCommand());
        getCommand("blockstacker").setExecutor(new Blockstack());

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new RankingListener(), this);
        getServer().getPluginManager().registerEvents(new MinionsCommand(), this);
        getServer().getPluginManager().registerEvents(new Blockstack(), this);

        initMinions();
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

        if (args[0].equalsIgnoreCase("island") || args[0].equalsIgnoreCase("info"))
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

            if (!warning.contains(player))
            {
                warning.add(player);
                player.sendMessage(Text.get("invitewarning"));
            }
            else
            {
                warning.remove(player);

                invites.put(User.getUser(target), user);
                player.sendMessage(Text.get("sentinvite").replace("%player%", target.getName()));
                target.sendMessage(Text.get("receivedinvite").replace("%player%", player.getName()));

                new CountdownTimer(this, 30,
                        () -> {},
                        () -> player.sendMessage(Text.get("invitetime")),
                        (time) -> {}
                ).scheduleTimer();
            }

            return true;
        }

        else if (args[0].equalsIgnoreCase("home"))
        {
            User user = null;

            if ((user = User.getUser(player)) == null)
            {
                System.out.println("null");
                return true;
            }

            if (!is.hasIsland(user))
            {
                user.sendMessage(Text.get("noisland"));
                return true;
            }

            user.teleportToIsland();
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

    @EventHandler
    public void onInvite(AsyncPlayerChatEvent event)
    {
        User target = User.getUser(event.getPlayer());

        if (invites.containsKey(target) && event.getMessage().equals("CONFIRM"))
        {
            event.setCancelled(true);
            is.invite(invites.get(target), target);
            target.sendMessage(Text.get("inviteconfirm").replace("%player%", invites.get(target).getName()));
            invites.remove(target);
        }
    }

    private void initMinions()
    {
        for (Map.Entry<String, Object> player : config.getConfigurationSection("").getValues(false).entrySet())
        {
            for (Map.Entry<String, Object> location : config.getConfigurationSection(player.getKey()).getValues(false).entrySet())
            {
                if (!config.isSet(player.getKey() + "." + location.getKey() + ".world"))
                    continue;

                World world = Bukkit.getWorld(config.getString(player.getKey() + "." + location.getKey() + ".world"));
                double y = config.getDouble(player.getKey() + "." + location.getKey() + ".y");
                double x = config.getDouble(player.getKey() + "." + location.getKey() + ".x");
                double z = config.getDouble(player.getKey() + "." + location.getKey() + ".z");
                double yaw = config.getDouble(player.getKey() + "." + location.getKey() + ".yaw");
                double pitch = config.getDouble(player.getKey() + "." + location.getKey() + ".pitch");

                Location locationObj = new Location(world, x, y, z, (float) yaw, (float) pitch);

                int scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(SkyBlock.plugin, new Runnable()
                {
                    @Override
                    public void run()
                    {
                        MinionsCommand.miningProcess(player.getKey(), location.getKey(), locationObj);
                    }
                }, 20 * MinionsCommand.getSeconds(Bukkit.getOfflinePlayer(player.getKey()), player.getKey(), false), 20 * MinionsCommand.getSeconds(Bukkit.getOfflinePlayer(player.getKey()), player.getKey(), false));

                MinionsCommand.minions.put(locationObj, scheduler);
            }
        }
    }
}
