package no.runsafe.ItemControl;

import no.runsafe.framework.event.player.IPlayerRightClick;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;

public class PlayerListener implements IPlayerRightClick
{
	public PlayerListener(Globals globals, IOutput output)
	{
		this.globals = globals;
		this.output = output;
	}

	@Override
	public boolean OnPlayerRightClick(RunsafePlayer player, RunsafeItemStack usingItem, RunsafeBlock targetBlock)
	{
		RunsafeWorld world = player.getWorld();
		RunsafeItemStack item = player.getItemInHand();

		String playerName = player.getName();

		if (globals.itemIsDisabled(world, item.getItemId()))
		{
			this.output.fine(String.format("%s tried to use disabled item %s", playerName, item.getItemId()));
			if (globals.blockedItemShouldBeRemoved())
				player.removeItem(usingItem.getItemType());

			return false;
		}

		if (!player.canBuildNow() || targetBlock == null)
			return true;

		if (item.is(Item.Miscellaneous.MonsterEgg.Any) && this.globals.blockShouldDrop(world, Item.Unavailable.MobSpawner.getTypeID()))
		{
			this.output.fine("Monster Egg placement detected by " + playerName);

			// If the block has an interface or is interact block, don't let them place a spawner
			if (targetBlock.hasInterface() || targetBlock.isInteractBlock())
				return true;

			if (this.globals.createSpawner(player, targetBlock.getLocation(), item))
				player.removeItem(item.getItemType(), 1);

			return false;
		}
		return true;
	}

	private final Globals globals;
	private IOutput output;
}
