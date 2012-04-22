package no.runsafe.ItemControl;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IPluginEnabled;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.block.RunsafeBlockState;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.block.CraftCreatureSpawner;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Globals implements IPluginEnabled
{
    public Globals(IConfiguration config, IOutput output)
    {
        this.output = output;
        this.config = config;
    }

    @Override
    public void OnPluginEnabled()
    {
        this.disabledItems = this.loadConfigurationIdList("disabledItems");
        this.worldBlockDrops = this.loadConfigurationIdList("blockDrops");
    }

    public Boolean itemIsDisabled(RunsafeWorld world, int itemID)
    {
        if(this.disabledItems.containsKey("*") && this.disabledItems.get("*").contains(itemID))
            return true;

        if(this.disabledItems.containsKey(world.getName()) && this.disabledItems.get(world.getName()).contains(itemID))
            return true;

        return false;
    }

    public Boolean blockShouldDrop(RunsafeWorld world, Integer blockId)
    {
        if(this.worldBlockDrops.containsKey("*") && this.worldBlockDrops.get("*").contains(blockId))
            return true;

        if(this.worldBlockDrops.containsKey(world.getName()) && this.worldBlockDrops.get(world.getName()).contains(blockId))
            return true;

        return false;
    }

    public void setSpawnerEntityID(RunsafeBlock block, short entityID)
    {
        try
        {
            Field mobIDField = net.minecraft.server.TileEntityMobSpawner.class.getDeclaredField("mobName");
            mobIDField.setAccessible(true);

            Field tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
            tileField.setAccessible(true);


            BlockState blockState = block.getBlockState().getRaw();
            if (!(blockState instanceof CreatureSpawner))
            {
                throw new IllegalArgumentException("setSpawnerEntityID called on non-spawner block: " + block);
            }

            CraftCreatureSpawner spawner = ((CraftCreatureSpawner) blockState);

            if (tileField != null && mobIDField != null)
            {
                try
                {
                    String mobID = EntityType.fromId(entityID).getName();

                    net.minecraft.server.TileEntityMobSpawner tile = (net.minecraft.server.TileEntityMobSpawner)tileField.get(spawner);

                    tile.a(mobID);
                    return;
                }
                catch (Exception e)
                {
                    //diddums
                }
            }

            // Fallback to wrapper
            EntityType ct = EntityType.fromId(entityID);
            if (ct == null)
            {
                throw new IllegalArgumentException("Failed to find creature type for "+entityID);
            }

            spawner.setSpawnedType(ct);
            spawner.update();
            blockState.update();
        }
        catch (Exception e)
        {
            //diddums
        }
    }

    private HashMap<String, List<Integer>> loadConfigurationIdList(String configurationValue)
    {
        HashMap<String, List<Integer>> returnMap = new HashMap<String, List<Integer>>();
        ConfigurationSection disabledItems = this.config.getSection(configurationValue);

        if(disabledItems == null)
            return null;

        Set<String> keys = disabledItems.getKeys(true);
        if(keys == null)
            return null;

        for(String key : keys)
        {
            if(!returnMap.containsKey(key))
                returnMap.put(key, disabledItems.getIntegerList(key));
        }

        return returnMap;
    }

    private HashMap<String, List<Integer>> worldBlockDrops = new HashMap<String, List<Integer>>();
    private HashMap<String, List<Integer>> disabledItems = new HashMap<String, List<Integer>>();
    private IConfiguration config;
    private IOutput output;
}
