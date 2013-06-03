package no.runsafe.ItemControl;

import no.runsafe.framework.event.player.IPlayerInteractEvent;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;

public class PlayerListener implements IPlayerInteractEvent
{
	public PlayerListener(Globals globals, IOutput output)
	{
		this.globals = globals;
		this.output = output;
	}

	@Override
	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		RunsafePlayer player = event.getPlayer();
		RunsafeWorld world = player.getWorld();
		RunsafeItemStack item = player.getItemInHand();

		String playerName = player.getName();

		if (globals.itemIsDisabled(world, item.getItemId()))
		{
			this.output.fine(String.format("%s tried to use disabled item %s", playerName, item.getItemId()));
			if (globals.blockedItemShouldBeRemoved())
				event.removeItemStack();

			event.setCancelled(true);
			return;
		}

		if (!player.canBuildNow() || event.getBlock() == null)
			return;

		this.output.fine("Checking for Monster Egg");
		this.output.fine("Egg ID: " + Item.Miscellaneous.MonsterEgg.Any.getTypeID());
		this.output.fine("Any Data: " + Item.Miscellaneous.MonsterEgg.Any.getDataByte());
		this.output.fine("Item ID: " + item.getItemId());
		this.output.fine("Item Data: " + item.getData().getData());

		if (item.is(Item.Miscellaneous.MonsterEgg.Any) && this.globals.blockShouldDrop(world, Item.Miscellaneous.MonsterEgg.Any.getTypeID()))
		{
			this.output.fine("Monster Egg placement detected by " + playerName);
			RunsafeBlock block = event.getBlock();

			// If the block has an interface or is interact block, don't let them place a spawner
			if (block.hasInterface() || block.isInteractBlock())
				return;

			if (this.globals.createSpawner(player, world, event.getTargetBlock(), item))
				player.removeItem(item.getItemType(), 1);

			event.setCancelled(true);
		}
	}

	private final Globals globals;
	private IOutput output;
}
