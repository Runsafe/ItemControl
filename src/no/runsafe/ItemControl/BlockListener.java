package no.runsafe.ItemControl;

import no.runsafe.framework.event.block.IBlockBreakEvent;
import no.runsafe.framework.event.block.IBlockDispense;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.block.RunsafeSpawner;
import no.runsafe.framework.server.enchantment.RunsafeEnchantment;
import no.runsafe.framework.server.enchantment.RunsafeEnchantmentType;
import no.runsafe.framework.server.event.block.RunsafeBlockBreakEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.bukkit.Material;

import java.util.logging.Level;

public class BlockListener implements IBlockBreakEvent, IBlockDispense
{
	public BlockListener(Globals globals, IScheduler scheduler, IOutput output)
	{
		this.output = output;
		this.globals = globals;
		this.scheduler = scheduler;
	}

	@Override
	public void OnBlockBreakEvent(RunsafeBlockBreakEvent event)
	{
		if (event.getCancelled())
			return;

		final RunsafeBlockBreakEvent blockBreakEvent = event;

		RunsafePlayer thePlayer = event.getPlayer();
		RunsafeItemStack heldItem = thePlayer.getItemInHand();
		final RunsafeBlock theBlock = event.getBlock();

		if (this.globals.blockShouldDrop(thePlayer.getWorld(), theBlock.getTypeId()) && heldItem.containsEnchantment(RunsafeEnchantmentType.SILK_TOUCH))
		{
			try
			{
				RunsafeSpawner spawner = (RunsafeSpawner) theBlock;
				final RunsafeWorld theBlockWorld = theBlock.getWorld();
				final int itemId = Material.MONSTER_EGG.getId();
				final byte monsterType = (byte) spawner.getCreatureId();
				if (!globals.spawnerTypeValid(spawner.getCreatureType(), thePlayer))
				{
					output.outputToConsole(
						String.format(
							"%s tried harvesting an invalid %s spawner!",
							thePlayer.getName(),
							spawner.getCreatureType()
						),
						Level.WARNING
					);
					return;
				}
				scheduler.createSyncTimer(
					new Runnable()
					{
						@Override
						public void run()
						{
							if (blockBreakEvent.getCancelled())
								return;
							output.fine(String.format("Dropping a spawn egg [%d:%d]", itemId, monsterType));
							RunsafeItemStack itemToDrop = new RunsafeItemStack(itemId, 1, (short) monsterType);
							theBlockWorld.dropItem(theBlock.getLocation(), itemToDrop);
						}
					},
					10L
				);
				blockBreakEvent.setXP(0);
			}
			catch (Exception e)
			{
				output.logException(e);
			}
		}
	}

	@Override
	public boolean OnBlockDispense(RunsafeBlock block, RunsafeItemStack itemStack)
	{
		RunsafeWorld blockWorld = block.getWorld();
		RunsafeLocation blockLocation = block.getLocation();
		if (this.globals.itemIsDisabled(block.getWorld(), itemStack.getItemId()))
		{
			blockWorld.createExplosion(blockLocation, 0, true);
			block.breakNaturally();
			return false;
		}
		return true;
	}

	private final IOutput output;
	private final Globals globals;
	private final IScheduler scheduler;
}
