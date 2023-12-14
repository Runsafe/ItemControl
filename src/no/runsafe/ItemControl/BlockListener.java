package no.runsafe.ItemControl;

import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.block.IItemDispensed;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

public class BlockListener implements IItemDispensed
{
	@Override
	public boolean OnBlockDispense(IBlock block, RunsafeMeta itemStack)
	{
		IWorld blockWorld = block.getWorld();
		if (Globals.itemIsDisabled(blockWorld, itemStack))
		{
			block.getLocation().playSound(Sound.Creature.Cat.Hiss);
			return false;
		}
		return true;
	}
}
