package no.runsafe.ItemControl;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.block.ICreatureSpawner;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.minecraft.RunsafeEntityType;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.EntityType;
import no.runsafe.framework.minecraft.item.RunsafeItemStack;
import no.runsafe.framework.text.ConsoleColour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Globals implements IConfigurationChanged
{
	public Globals(IConsole output, IDebug debugger)
	{
		console = output;
		this.debugger = debugger;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		this.disabledItems.clear();
		this.spawnerHarvestWorlds.clear();
		this.validSpawners.clear();
		this.disabledItems.putAll(config.getConfigSectionsAsIntegerList("disabledItems"));
		this.spawnerHarvestWorlds.addAll(config.getConfigValueAsList("spawnerDrop"));
		this.validSpawners.addAll(config.getConfigValueAsList("spawner.allow"));
		this.removeBlocked = config.getConfigValueAsBoolean("remove.disabledItems");
	}

	public Boolean itemIsDisabled(IWorld world, int itemID)
	{
		return (this.disabledItems.containsKey("*") && this.disabledItems.get("*").contains(itemID))
			|| (this.disabledItems.containsKey(world.getName()) && this.disabledItems.get(world.getName()).contains(itemID));
	}

	public boolean spawnerIsHarvestable(IWorld world)
	{
		return this.spawnerHarvestWorlds.contains("*") || this.spawnerHarvestWorlds.contains(world.getName());
	}

	public boolean createSpawner(IPlayer actor, ILocation location, RunsafeItemStack itemInHand)
	{
		IBlock target = location.getBlock();
		Item inHand = itemInHand.getItemType();
		RunsafeEntityType spawnerType = EntityType.Get(inHand);
		if (spawnerType == null)
			debugger.debugFine("Null entity type");
		else
			debugger.debugFine("Going to check %s spawners for validity", spawnerType.getName());
		if (target.isAir() && spawnerTypeValid(inHand.getData(), actor))
		{
			debugger.debugFine("Creating spawner");
			Item.Unavailable.MobSpawner.Place(location);
			if (setSpawnerEntityID(location.getBlock(), spawnerType))
				return true;
			debugger.debugFine("Failed creating spawner");
			Item.Unavailable.Air.Place(location);
		}
		else if (!target.isAir())
			debugger.debugFine("Target block is not air.");
		else
			debugger.debugFine("Spawner type is invalid.");
		return false;
	}

	private boolean spawnerTypeValid(byte data, IPlayer actor)
	{
		return spawnerTypeValid(EntityType.Get(data), actor);
	}

	public boolean spawnerTypeValid(RunsafeEntityType entityType, IPlayer actor)
	{
		if (entityType == null && actor != null)
		{
			console.logInformation(
				"SPAWNER WARNING: %s tried to create/break a NULL spawner [%s,%d,%d,%d]!",
				ConsoleColour.FromMinecraft(actor.getPrettyName()),
				actor.getWorld().getName(),
				actor.getLocation().getBlockX(),
				actor.getLocation().getBlockY(),
				actor.getLocation().getBlockZ()
			);
			return false;
		}

		if (entityType == null || !validSpawners.contains(entityType.getName().toLowerCase()))
		{
			if (actor != null)
				console.logInformation(
					"SPAWNER WARNING: %s tried to create/break an invalid %s spawner [%s,%d,%d,%d]!",
					ConsoleColour.FromMinecraft(actor.getPrettyName()),
					entityType,
					actor.getWorld().getName(),
					actor.getLocation().getBlockX(),
					actor.getLocation().getBlockY(),
					actor.getLocation().getBlockZ()
				);
			return false;
		}
		return true;
	}

	public boolean blockedItemShouldBeRemoved()
	{
		return removeBlocked;
	}

	private boolean setSpawnerEntityID(IBlock block, RunsafeEntityType entityType)
	{
		if (block == null || block.isAir())
			return false;

		if (!(block instanceof ICreatureSpawner))
			return false;

		ICreatureSpawner spawner = (ICreatureSpawner) block;
		spawner.setCreature(entityType);
		spawner.update(true);
		return true;
	}

	private final List<String> spawnerHarvestWorlds = new ArrayList<String>();
	private final Map<String, List<Integer>> disabledItems = new HashMap<String, List<Integer>>();
	private final List<String> validSpawners = new ArrayList<String>();
	private final IConsole console;
	private final IDebug debugger;
	private boolean removeBlocked;
}
