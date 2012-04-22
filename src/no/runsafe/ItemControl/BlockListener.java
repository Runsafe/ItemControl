package no.runsafe.ItemControl;

import java.lang.reflect.Field;

import no.runsafe.framework.event.block.IBlockBreakEvent;
import no.runsafe.framework.event.block.IBlockDispenseEvent;
import no.runsafe.framework.event.block.IBlockPlaceEvent;
import no.runsafe.framework.event.block.IBlockRedstoneEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.block.RunsafeBlockState;
import no.runsafe.framework.server.enchantment.RunsafeEnchantmentWrapper;
import no.runsafe.framework.server.event.block.RunsafeBlockBreakEvent;
import no.runsafe.framework.server.event.block.RunsafeBlockDispenseEvent;
import no.runsafe.framework.server.event.block.RunsafeBlockPlaceEvent;
import no.runsafe.framework.server.event.block.RunsafeBlockRedstoneEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.bukkit.Material;
import org.bukkit.craftbukkit.block.CraftCreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

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

                RunsafeItemStack itemToDrop = new RunsafeItemStack(theBlock.getTypeId(), 1, spawner.getSpawnedType().getTypeId());
                theBlockWorld.dropItem(theBlock.getLocation(), itemToDrop);
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

            this.scheduler.startSyncTask(new Runnable() {

                @Override
                public void run() {
                    globals.setSpawnerEntityID(theBlock, mobID);

                } }, 1);

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
            blockWorld.strikeLightning(blockLocation);
            blockWorld.createExplosion(blockLocation, 0, true);
            block.breakNaturally();
            event.setCancelled(true);
        }
    }

    private IOutput output;
    private Globals globals;
    private IScheduler scheduler;

}
