package de.marvin2k0.skyblock.enchblocks;

import de.marvin2k0.skyblock.SkyBlock;
import de.marvin2k0.skyblock.skyblock.listeners.RankingListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class EnchantedBlocks implements Listener
{
    File file = new File(SkyBlock.plugin.getDataFolder().getPath() + "/enchblocks.yml");
    YamlConfiguration blockconfig = YamlConfiguration.loadConfiguration(file);

    public static void addRecipies(SkyBlock plugin)
    {
        /* Emerald */
        ItemStack emerald = new ItemStack(Material.EMERALD);
        emerald.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ShapedRecipe emeraldRecipe = new ShapedRecipe(emerald);
        emeraldRecipe.shape("EEE", "EGE", "EEE");
        emeraldRecipe.setIngredient('E', new ItemStack(Material.EMERALD).getData());
        emeraldRecipe.setIngredient('G', new ItemStack(Material.SULPHUR).getData());

        Bukkit.addRecipe(emeraldRecipe);

        /* Gold ingot */
        ItemStack gold = new ItemStack(Material.GOLD_INGOT);
        gold.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ShapedRecipe goldRecipe = new ShapedRecipe(gold);
        goldRecipe.shape("EEE", "EGE", "EEE");
        goldRecipe.setIngredient('E', new ItemStack(Material.GOLD_INGOT).getData());
        goldRecipe.setIngredient('G', new ItemStack(Material.SULPHUR).getData());

        Bukkit.addRecipe(goldRecipe);

        /* Iron ingot */
        ItemStack iron = new ItemStack(Material.IRON_INGOT);
        iron.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ShapedRecipe ironRecipe = new ShapedRecipe(iron);
        ironRecipe.shape("EEE", "EGE", "EEE");
        ironRecipe.setIngredient('E', new ItemStack(Material.IRON_INGOT).getData());
        ironRecipe.setIngredient('G', new ItemStack(Material.SULPHUR).getData());

        Bukkit.addRecipe(ironRecipe);

        /* Redstone dust */
        ItemStack redstone = new ItemStack(Material.REDSTONE);
        redstone.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ShapedRecipe redstoneRecipe = new ShapedRecipe(redstone);
        redstoneRecipe.shape("EEE", "EGE", "EEE");
        redstoneRecipe.setIngredient('E', new ItemStack(Material.REDSTONE).getData());
        redstoneRecipe.setIngredient('G', new ItemStack(Material.SULPHUR).getData());

        Bukkit.addRecipe(redstoneRecipe);

        /* Diamond */
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        diamond.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ShapedRecipe diamondRecipe = new ShapedRecipe(diamond);
        diamondRecipe.shape("EEE", "EGE", "EEE");
        diamondRecipe.setIngredient('E', new ItemStack(Material.DIAMOND).getData());
        diamondRecipe.setIngredient('G', new ItemStack(Material.SULPHUR).getData());

        Bukkit.addRecipe(diamondRecipe);

        /* Gravel */
        ItemStack gravel = new ItemStack(Material.GRAVEL);
        gravel.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ShapedRecipe gravelRecipe = new ShapedRecipe(gravel);
        gravelRecipe.shape("EEE", "EGE", "EEE");
        gravelRecipe.setIngredient('E', new ItemStack(Material.GRAVEL).getData());
        gravelRecipe.setIngredient('G', new ItemStack(Material.FLINT).getData());

        Bukkit.addRecipe(gravelRecipe);
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event)
    {
        if (event.getRecipe().getResult().getType() == Material.EMERALD_BLOCK)
        {
            if (isEnchanted(event.getInventory().getMatrix(), Enchantment.DURABILITY))
            {
                ItemStack emeraldBlock = new ItemStack(Material.EMERALD_BLOCK);
                emeraldBlock.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                event.getInventory().setResult(emeraldBlock);
            }
        }

        else if (event.getRecipe().getResult().getType() == Material.GOLD_BLOCK)
        {
            if (isEnchanted(event.getInventory().getMatrix(), Enchantment.DURABILITY))
            {
                ItemStack goldBlock = new ItemStack(Material.GOLD_BLOCK);
                goldBlock.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                event.getInventory().setResult(goldBlock);
            }
        }

        else if (isLapis(event.getInventory().getMatrix()) && !isEnchanted(event.getInventory().getMatrix(), Enchantment.DURABILITY))
        {
            ItemStack lapis = new ItemStack(Material.getMaterial(351), 1, (short) 4);
            lapis.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

            event.getInventory().setResult(lapis);
        }

        else if (event.getRecipe().getResult().getType() == Material.LAPIS_BLOCK)
        {
            if (isEnchanted(event.getInventory().getMatrix(), Enchantment.DURABILITY))
            {
                ItemStack lapisBlock = new ItemStack(Material.LAPIS_BLOCK);
                lapisBlock.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                event.getInventory().setResult(lapisBlock);
            }
        }

        else if (event.getRecipe().getResult().getType() == Material.IRON_BLOCK)
        {
            if (isEnchanted(event.getInventory().getMatrix(), Enchantment.DURABILITY))
            {
                ItemStack ironBlock = new ItemStack(Material.IRON_BLOCK);
                ironBlock.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                event.getInventory().setResult(ironBlock);
            }
        }

        else if (event.getInventory().getResult().getType() == Material.REDSTONE_BLOCK)
        {
            if (isEnchanted(event.getInventory().getMatrix(), Enchantment.DURABILITY))
            {
                ItemStack redstoneBlock = new ItemStack(Material.REDSTONE_BLOCK);
                redstoneBlock.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                event.getInventory().setResult(redstoneBlock);
            }
        }

        else if (event.getInventory().getResult().getType() == Material.DIAMOND_BLOCK)
        {
            if (isEnchanted(event.getInventory().getMatrix(), Enchantment.DURABILITY))
            {
                ItemStack diamond = new ItemStack(Material.DIAMOND_BLOCK);
                diamond.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

                event.getInventory().setResult(diamond);
            }
        }
    }

    private boolean isLapis(ItemStack[] matrix)
    {
        for (int i = 0; i < 9; i++)
        {
            if (i == 4)
                continue;

            if (matrix[i].getType() != Material.getMaterial(351))
                return false;
        }

        return true;
    }

    private boolean isEnchanted(ItemStack[] matrix, Enchantment ench)
    {
        for (int i = 0; i < 9; i++)
        {
            if (!matrix[i].containsEnchantment(ench))
            {
                return false;
            }
        }

        return true;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event)
    {
        if (event.isCancelled())
            return;

        if (event.getItemInHand().containsEnchantment(Enchantment.DURABILITY))
        {
            Block block = event.getBlock();
            Player player = event.getPlayer();

            if (block.getType() == Material.DIAMOND_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                addPoints(player, Material.DIAMOND_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.GOLD_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                addPoints(player, Material.GOLD_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.EMERALD_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                addPoints(player, Material.EMERALD_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.IRON_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                addPoints(player, Material.IRON_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.LAPIS_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                addPoints(player, Material.LAPIS_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.GRAVEL)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                addPoints(player, Material.GRAVEL, block.getLocation(), points);
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event)
    {
        if (event.isCancelled())
            return;

        if (isBlock(event.getPlayer(), event.getBlock().getLocation()))
        {
            Block block = event.getBlock();
            Player player = event.getPlayer();

            if (block.getType() == Material.DIAMOND_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                removePoints(player, Material.DIAMOND_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.GOLD_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                removePoints(player, Material.GOLD_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.EMERALD_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                removePoints(player, Material.EMERALD_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.IRON_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                removePoints(player, Material.IRON_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.LAPIS_BLOCK)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                removePoints(player, Material.LAPIS_BLOCK, block.getLocation(), points);
            }
            else if (block.getType() == Material.GRAVEL)
            {
                int points = RankingListener.blockValues.get(block.getType()) * 8;
                removePoints(player, Material.GRAVEL, block.getLocation(), points);
            }
        }
    }

    private boolean isBlock(Player player, Location loc)
    {
        if (!blockconfig.isSet(player.getUniqueId().toString()))
        {
            return false;
        }

        Map<String, Object> section = blockconfig.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            String path = player.getUniqueId() + "." + entry.getKey();

            World world = Bukkit.getWorld(blockconfig.getString(path + ".world"));
            double x = blockconfig.getDouble(path + ".x");
            double y = blockconfig.getDouble(path + ".y");
            double z = blockconfig.getDouble(path + ".z");

            Location configLoc = new Location(world, x, y, z);

            if (loc.distance(configLoc) <= 1)
                return true;
        }

        return false;
    }

    private void removePoints(Player player, Material type, Location loc, int points)
    {
        FileConfiguration config = SkyBlock.plugin.getConfig();

        if (!config.isSet(player.getUniqueId() + ".island"))
            return;

        String path = player.getUniqueId() + ".island";
        String islandUUID = config.getString(path);
        int pointsBefor = config.getInt(islandUUID + ".points");
        config.set(islandUUID + ".points", pointsBefor - points);
        SkyBlock.plugin.saveConfig();

        if (!blockconfig.isSet(player.getUniqueId().toString()))
            return;

        Map<String, Object> section = blockconfig.getConfigurationSection(player.getUniqueId().toString()).getValues(false);

        for (Map.Entry<String, Object> entry : section.entrySet())
        {
            String p = player.getUniqueId() + "." + entry.getKey();

            World world = Bukkit.getWorld(blockconfig.getString(p + ".world"));
            double x = blockconfig.getDouble(p + ".x");
            double y = blockconfig.getDouble(p + ".y");
            double z = blockconfig.getDouble(p + ".z");

            Location configLoc = new Location(world, x, y, z);

            if (loc.distance(configLoc) <= 1)
            {
                blockconfig.set(p, null);
                saveConfig();
                return;
            }
        }
    }

    private void addPoints(Player player, Material type, Location loc, int points)
    {
        FileConfiguration config = SkyBlock.plugin.getConfig();

        if (!config.isSet(player.getUniqueId() + ".island"))
            return;

        String path = player.getUniqueId() + ".island";
        String islandUUID = config.getString(path);
        int pointsBefor = config.getInt(islandUUID + ".points");
        config.set(islandUUID + ".points", pointsBefor + points);

        SkyBlock.plugin.saveConfig();

        String uuid = UUID.randomUUID().toString();
        String p = player.getUniqueId() + "." + uuid;

        blockconfig.set(p + ".type", type.toString());
        blockconfig.set(p + ".world", loc.getWorld().getName());
        blockconfig.set(p + ".x", loc.getX());
        blockconfig.set(p + ".y", loc.getY());
        blockconfig.set(p + ".z", loc.getZ());

        saveConfig();
    }

    private void saveConfig()
    {
        try
        {
            blockconfig.save(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
