package de.marvin2k0.skyblock.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Text
{
    static FileConfiguration config;
    static Plugin plugin;

    public static String get(String path)
    {
        return path.equalsIgnoreCase("prefix") ? get(path, false) : get(path, true);
    }

    public static String get(String path, boolean prefix)
    {
        return ChatColor.translateAlternateColorCodes('&', prefix ? config.getString("prefix") + " " + config.getString(path) : config.getString(path));
    }

    public static void setUp(Plugin plugin)
    {
        Text.plugin = plugin;
        Text.config = plugin.getConfig();

        config.options().copyDefaults(true);
        config.addDefault("prefix", "&8[&eWesterfox MC&8]&f");
        config.addDefault("noplayer", "&cOnly Players &7can execute this command!");
        config.addDefault("loading", "&7Loading...");
        config.addDefault("worldcreated", "&aSkyBlock world has been created!");
        config.addDefault("welcome", "&7Welcome to &aSkyBlock");
        config.addDefault("alreadyownsisland", "&7You already own an island!");
        config.addDefault("playernotfound", "&cThis player does not exist!");
        config.addDefault("playerhasnoisland", "&cThis player has no island!");
        config.addDefault("noisland", "&7You have &cno island");
        config.addDefault("notonline", "&7Player is not online!");
        config.addDefault("islandinfo", "&7%player%'s island is rank &a#%rank% (%points%)");
        config.addDefault("levelup", "&7Congratulations, your island is now level &a%level%");
        config.addDefault("leveldown", "&7Your island is now level &c%level%");
        config.addDefault("invitewarning", "&4WARNING: &cThis player will have full access to your island if you do that! &7Invite them again to confirm!");
        config.addDefault("sentinvite", "&7You have sent an invite to &a%player%");
        config.addDefault("receivedinvite", "&7You have received an invite from &a%player% &4WARNING: &cif you accept you'll lose your island! Type 'CONFIRM' to confirm");
        config.addDefault("inviteconfirm", "&7You are now part of the island of %player%");
        config.addDefault("invitetime", "&7Invitation was not accepted");
        config.addDefault("maxminions", 2);
        config.addDefault("minionsreached", "&7You can't create another minion!");

        saveConfig();
    }

    private static void saveConfig()
    {
        plugin.saveConfig();
    }
}
