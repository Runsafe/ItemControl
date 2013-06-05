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
		if (!event.isRightClick())
			return;

		RunsafePlayer player = event.getPlayer();
		RunsafeWorld world = player.getWorld();
		RunsafeItemStack usingItem = player.getItemInHand();

		String playerName = player.getName();

		if (globals.itemIsDisabled(world, usingItem.getItemId()))
		{
			this.output.fine(String.format("%s tried to use disabled item %s", playerName, usingItem.getItemId()));
			if (globals.blockedItemShouldBeRemoved())
				player.removeItem(usingItem.getItemType());

			event.setCancelled(true);
		}

		RunsafeBlock targetBlock = event.getTargetBlock().getBlock();
		if (!player.canBuildNow() || targetBlock == null)
			return;

		if (usingItem.is(Item.Miscellaneous.MonsterEgg.Any)
			&& this.globals.blockShouldDrop(world, Item.Unavailable.MobSpawner.getTypeID()))
		{
			this.output.fine("Monster Egg placement detected by " + playerName);

			// If the block has an interface or is interact block, don't let them place a spawner
			if (targetBlock.hasInterface() || targetBlock.isInteractBlock())
				return;

			if (this.globals.createSpawner(player, targetBlock.getLocation(), usingItem))
				player.removeItem(usingItem.getItemType(), 1);

			event.setCancelled(true);
			return;
		}
	}

	private final Globals globals;
	private IOutput output;
}
