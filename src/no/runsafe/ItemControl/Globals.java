package no.runsafe.ItemControl;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.IPluginEnabled;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Globals implements IConfigurationChanged
{
	public Globals()
	{
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		this.disabledItems = this.loadConfigurationIdList(config, "disabledItems");
		this.worldBlockDrops = this.loadConfigurationIdList(config, "blockDrops");
	}

	public Boolean itemIsDisabled(RunsafeWorld world, int itemID)
	{
		return (this.disabledItems.containsKey("*") && this.disabledItems.get("*").contains(itemID))
			|| (this.disabledItems.containsKey(world.getName()) && this.disabledItems.get(world.getName()).contains(itemID));
	}

	public Boolean blockShouldDrop(RunsafeWorld world, Integer blockId)
	{
		return (this.worldBlockDrops.containsKey("*") && this.worldBlockDrops.get("*").contains(blockId))
			|| (this.worldBlockDrops.containsKey(world.getName()) && this.worldBlockDrops.get(world.getName()).contains(blockId));
	}

	public void setSpawnerEntityID(RunsafeBlock block, short entityID)
	{
		Block bukkitBlock = block.getRaw();
		BlockState state = bukkitBlock.getState();
		if (!(state instanceof CreatureSpawner)) return;
		CreatureSpawner bukkit = (CreatureSpawner) state;
		bukkit.setSpawnedType(EntityType.fromId(entityID));
		bukkit.setDelay(1);
	}

	public boolean createSpawner(RunsafeWorld world, RunsafeLocation location, RunsafeItemStack itemInHand)
	{
		Block target = world.getRaw().getChunkAt(location.getRaw()).getBlock(
			location.getBlockX(),
			location.getBlockY(),
			location.getBlockZ()
		);
		if (target.isEmpty())
		{
			target.setType(Material.MOB_SPAWNER);
			CreatureSpawner spawner = (CreatureSpawner) target.getState();
			spawner.setSpawnedType(EntityType.fromId(itemInHand.getRaw().getData().getData()));
			spawner.update(true);
			return true;
		}
		return false;
	}

	private HashMap<String, List<Integer>> loadConfigurationIdList(IConfiguration config, String configurationValue)
	{
		HashMap<String, List<Integer>> returnMap = new HashMap<String, List<Integer>>();
		ConfigurationSection disabledItems = config.getSection(configurationValue);

		if (disabledItems == null)
			return null;

		Set<String> keys = disabledItems.getKeys(true);
		if (keys == null)
			return null;

		for (String key : keys)
		{
			if (!returnMap.containsKey(key))
				returnMap.put(key, disabledItems.getIntegerList(key));
		}

		return returnMap;
	}

	private HashMap<String, List<Integer>> worldBlockDrops = new HashMap<String, List<Integer>>();
	private HashMap<String, List<Integer>> disabledItems = new HashMap<String, List<Integer>>();
}
