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
import no.runsafe.framework.minecraft.item.meta.RunsafeSpawnEgg;

import java.util.ArrayList;
import java.util.List;

public class SpawnerHandler implements IConfigurationChanged
{
	public SpawnerHandler(IConsole console)
	{
		this.console = console;
	}

	public boolean spawnerIsNotHarvestable(IWorld world)
	{
		return !spawnerWorlds.contains("*") && !spawnerWorlds.contains(world.getName());
	}

	public boolean createSpawner(IPlayer actor, ILocation location, RunsafeSpawnEgg spawnEgg)
	{
		IBlock target = location.getBlock();

		RunsafeEntityType spawnerType = spawnEgg.getEntityType();

		if (!target.isAir() || !spawnerTypeValid(spawnerType, actor))
			return false;

		Item.Unavailable.MobSpawner.Place(location);
		if (setSpawnerEntityID(location.getBlock(), spawnerType))
			return true;

		Item.Unavailable.Air.Place(location);
		return false;
	}

	public boolean spawnerTypeValid(RunsafeEntityType entityType, IPlayer actor)
	{
		if (entityType == null && actor != null)
		{
			LogSpawnerManipulation(null, actor);
		}

		if (entityType != null && validSpawner.contains(entityType.getName().toLowerCase()))
			return true;

		if (actor == null)
			return false;

		LogSpawnerManipulation(entityType, actor);
		return false;
	}

	private void LogSpawnerManipulation(RunsafeEntityType entityType, IPlayer actor)
	{
		ILocation location = actor.getLocation();
		if (location == null)
		{
			console.logWarning(
				"SPAWNER WARNING: NULL tried to create/break an invalid %s spawner",
				entityType == null ? "NULL" : entityType
			);
			return;
		}
		console.logInformation(
			"SPAWNER WARNING: %s tried to create/break an invalid %s spawner [%s,%d,%d,%d]!",
			actor.getPrettyName(),
			entityType == null ? "NULL" : entityType,
			location.getWorld().getName(),
			location.getBlockX(),
			location.getBlockY(),
			location.getBlockZ()
		);
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

	private List<String> spawnerWorlds = new ArrayList<>(0);
	private List<String> validSpawner = new ArrayList<>(0);
	private final IConsole console;
}
