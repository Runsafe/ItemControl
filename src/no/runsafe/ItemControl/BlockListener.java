package no.runsafe.ItemControl;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.block.IItemDispensed;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

public class BlockListener implements IItemDispensed
{
	public BlockListener(Globals globals)
	{
		this.globals = globals;
	}

	@Override
	public boolean OnBlockDispense(IBlock block, RunsafeMeta itemStack)
	{
		IWorld blockWorld = block.getWorld();
		ILocation blockLocation = block.getLocation();
		if (globals.itemIsDisabled(block.getWorld(), itemStack.getItemId()))
		{
			blockWorld.createExplosion(blockLocation, 0, true);
			block.breakNaturally();
			return false;
		}
		return true;
	}

	private final Globals globals;
}
