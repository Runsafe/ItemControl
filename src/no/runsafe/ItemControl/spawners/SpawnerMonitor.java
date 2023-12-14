package no.runsafe.ItemControl.spawners;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.block.ICreatureSpawner;
import no.runsafe.framework.api.event.block.IBlockBreakEvent;
import no.runsafe.framework.api.event.entity.IMobSpawnerPulsed;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.minecraft.RunsafeEntityType;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Enchant;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.entity.RunsafeLivingEntity;
import no.runsafe.framework.minecraft.event.block.RunsafeBlockBreakEvent;
import no.runsafe.framework.minecraft.item.RunsafeItemStack;

import java.util.logging.Level;

public class SpawnerMonitor implements IBlockBreakEvent, IMobSpawnerPulsed
{
	public SpawnerMonitor(IScheduler scheduler, IConsole console, SpawnerHandler handler)
	{
		this.scheduler = scheduler;
		this.console = console;
		this.handler = handler;
	}

	@Override
	public void OnBlockBreakEvent(RunsafeBlockBreakEvent event)
	{
		if (event.isCancelled() || !(event.getBlock() instanceof ICreatureSpawner))
			return;

		final RunsafeBlockBreakEvent blockBreakEvent = event;

		IPlayer thePlayer = event.getPlayer();
		RunsafeItemStack heldItem = thePlayer.getItemInMainHand();
		final IBlock theBlock = event.getBlock();

		if (!Enchant.SilkTouch.isOn(heldItem) || !handler.spawnerIsHarvestable(thePlayer.getWorld()))
			return;

		try
		{
			ICreatureSpawner spawner = (ICreatureSpawner) theBlock;
			final RunsafeEntityType creature = spawner.getCreature();
			if (!handler.spawnerTypeValid(creature, thePlayer))
			{
				console.outputToConsole(
					String.format(
						"%s tried harvesting an invalid %s spawner!",
						thePlayer.getName(),
						creature.getName()
					),
					Level.WARNING
				);
				return;
			}
			scheduler.createSyncTimer(
					() ->
					{
						if (!blockBreakEvent.isCancelled())
							Item.Miscellaneous.MonsterEgg.Get(creature).Drop(theBlock.getLocation(), 1);
					},
				10L
			);
			blockBreakEvent.setXP(0);
		}
		catch (Exception e)
		{
			console.logException(e);
		}
	}

	@Override
	public boolean OnMobSpawnerPulsed(RunsafeLivingEntity entity, ILocation location)
	{
		if (handler.spawnerTypeValid(entity.getEntityType(), null))
			return true;

		console.logInformation(
			"SPAWNER WARNING: &cBlocked invalid spawner of &e%s&c at (%s,%d,%d,%d)",
			entity.getRaw().getType().name(),
			location.getWorld().getName(),
			location.getBlockX(),
			location.getBlockY(),
			location.getBlockZ()
		);
		return false;
	}

	private final IScheduler scheduler;
	private final IConsole console;
	private final SpawnerHandler handler;
}
