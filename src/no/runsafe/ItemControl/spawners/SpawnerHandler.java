package no.runsafe.ItemControl.spawners;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.block.ICreatureSpawner;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.minecraft.RunsafeEntityType;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.EntityType;
import no.runsafe.framework.minecraft.item.RunsafeItemStack;

import java.util.ArrayList;
import java.util.List;

public class SpawnerHandler implements IConfigurationChanged
{
	public SpawnerHandler(IConsole console)
	{
		this.console = console;
	}

	public boolean spawnerIsHarvestable(IWorld world)
	{
		return spawnerWorlds.contains("*") || spawnerWorlds.contains(world.getName());
	}

	public boolean createSpawner(IPlayer actor, ILocation location, RunsafeItemStack itemInHand)
	{
		IBlock target = location.getBlock();
		Item inHand = itemInHand.getItemType();
		RunsafeEntityType spawnerType = EntityType.Get(inHand);

		if (target.isAir() && spawnerTypeValid(inHand.getData(), actor))
		{
			Item.Unavailable.MobSpawner.Place(location);
			if (setSpawnerEntityID(location.getBlock(), spawnerType))
				return true;

			Item.Unavailable.Air.Place(location);
		}
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
					actor.getPrettyName(),
					actor.getWorld().getName(),
					actor.getLocation().getBlockX(),
					actor.getLocation().getBlockY(),
					actor.getLocation().getBlockZ()
			);
			return false;
		}

		if (entityType == null || !validSpawner.contains(entityType.getName().toLowerCase()))
		{
			if (actor != null)
				console.logInformation(
						"SPAWNER WARNING: %s tried to create/break an invalid %s spawner [%s,%d,%d,%d]!",
						actor.getPrettyName(),
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

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		spawnerWorlds = config.getConfigValueAsList("spawnerDrop");
		validSpawner = config.getConfigValueAsList("spawner.allow");
	}

	private List<String> spawnerWorlds = new ArrayList<String>(0);
	private List<String> validSpawner = new ArrayList<String>(0);
	private final IConsole console;
}
