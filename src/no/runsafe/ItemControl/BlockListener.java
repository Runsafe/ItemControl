package no.runsafe.ItemControl;

import no.runsafe.framework.event.block.IBlockBreakEvent;
import no.runsafe.framework.event.block.IBlockDispenseEvent;
import no.runsafe.framework.event.block.IBlockPlaceEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.block.RunsafeBlockState;
import no.runsafe.framework.server.enchantment.RunsafeEnchantmentWrapper;
import no.runsafe.framework.server.event.block.RunsafeBlockBreakEvent;
import no.runsafe.framework.server.event.block.RunsafeBlockDispenseEvent;
import no.runsafe.framework.server.event.block.RunsafeBlockPlaceEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftCreatureSpawner;

import java.lang.reflect.Field;
import java.util.logging.Level;

public class BlockListener implements IBlockPlaceEvent, IBlockBreakEvent, IBlockDispenseEvent
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
		RunsafePlayer thePlayer = event.getPlayer();
		RunsafeItemStack heldItem = thePlayer.getItemInHand();
		RunsafeBlock theBlock = event.getBlock();

		if (this.globals.blockShouldDrop(thePlayer.getWorld(), theBlock.getTypeId()) && heldItem.containsEnchantment(new RunsafeEnchantmentWrapper(33)))
		{
			RunsafeWorld theBlockWorld = theBlock.getWorld();

			try
			{
				Field tileField = CraftCreatureSpawner.class.getDeclaredField("spawner");
				tileField.setAccessible(true);

				RunsafeBlockState blockState = theBlock.getBlockState();
				CraftCreatureSpawner spawner = (CraftCreatureSpawner) blockState.getRaw();

				int itemId = Material.MONSTER_EGG.getId();
				switch (spawner.getSpawnedType())
				{
					case DROPPED_ITEM:
					case EXPERIENCE_ORB:
					case PAINTING:
					case ARROW:
					case SNOWBALL:
					case FIREBALL:
					case SMALL_FIREBALL:
					case ENDER_PEARL:
					case ENDER_SIGNAL:
					case THROWN_EXP_BOTTLE:
					case PRIMED_TNT:
					case FALLING_BLOCK:
					case MINECART:
					case BOAT:
					case CREEPER:
					case GIANT:
					case SLIME:
					case GHAST:
					case PIG_ZOMBIE:
					case ENDERMAN:
					case SILVERFISH:
					case MAGMA_CUBE:
					case ENDER_DRAGON:
					case PIG:
					case SHEEP:
					case COW:
					case CHICKEN:
					case SQUID:
					case WOLF:
					case MUSHROOM_COW:
					case SNOWMAN:
					case OCELOT:
					case IRON_GOLEM:
					case VILLAGER:
					case ENDER_CRYSTAL:
					case SPLASH_POTION:
					case EGG:
					case FISHING_HOOK:
					case LIGHTNING:
					case WEATHER:
					case PLAYER:
					case COMPLEX_PART:
					case UNKNOWN:
						// Invalid spawners, do naught
						output.outputToConsole(
							String.format(
								"%s tried harvesting an invalid %s spawner!",
								thePlayer.getName(),
								spawner.getSpawnedType().getName()
							),
							Level.WARNING
						);
						itemId = 0;
						break;

					case SKELETON:
					case SPIDER:
					case ZOMBIE:
					case CAVE_SPIDER:
					case BLAZE:
						// Valid spawners, drop one.
						break;
				}
				if (itemId > 0)
				{
					RunsafeItemStack itemToDrop = new RunsafeItemStack(itemId, 1, (short) 0, (byte) spawner.getSpawnedType().getTypeId());
					theBlockWorld.dropItem(theBlock.getLocation(), itemToDrop);
				}
			}
			catch (Exception e)
			{
				//Diddums
			}
		}
	}

	@Override
	public void OnBlockPlaceEvent(RunsafeBlockPlaceEvent event)
	{
		final RunsafePlayer thePlayer = event.getPlayer();
		RunsafeItemStack heldItem = thePlayer.getItemInHand();

		final RunsafeBlock theBlock = event.getBlock();

		if (theBlock.getMaterialType().getMaterialId() == Material.MOB_SPAWNER.getId())
		{
			final short mobID = heldItem.getDurability();

			this.scheduler.startSyncTask(new Runnable()
			{
				@Override
				public void run()
				{
					globals.setSpawnerEntityID(theBlock, mobID);
					thePlayer.sendBlockChange(theBlock, (byte) 0);
				}
			}, 1);

		}
	}

	@Override
	public void OnBlockDispenseEvent(RunsafeBlockDispenseEvent event)
	{
		RunsafeBlock block = event.getBlock();
		RunsafeWorld blockWorld = block.getWorld();
		RunsafeItemStack itemStack = event.getItem();
		RunsafeLocation blockLocation = block.getLocation();

		if (this.globals.itemIsDisabled(block.getWorld(), itemStack.getItemId()))
		{
			blockWorld.createExplosion(blockLocation, 0, true);
			block.breakNaturally();
			event.setCancelled(true);
		}
	}

	private IOutput output;
	private Globals globals;
	private IScheduler scheduler;

}
