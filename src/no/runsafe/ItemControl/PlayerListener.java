package no.runsafe.ItemControl;

import no.runsafe.framework.event.player.IPlayerInteractEvent;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;

public class PlayerListener implements IPlayerInteractEvent
{
	public PlayerListener(Globals globals)
	{
		this.globals = globals;
	}

	@Override
	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		RunsafePlayer player = event.getPlayer();
		RunsafeWorld world = player.getWorld();
		RunsafeItemStack item = player.getItemInHand();

		if (globals.itemIsDisabled(world, item.getItemId()))
		{
			if (globals.blockedItemShouldBeRemoved())
				event.removeItemStack();

			event.setCancelled(true);
			return;
		}

		if (!player.canBuildNow() || event.getBlock() == null)
			return;

		if (item.is(Item.Miscellaneous.MonsterEgg.Any) && this.globals.blockShouldDrop(world, Item.Miscellaneous.MonsterEgg.Any.getTypeID()))
		{
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
}
