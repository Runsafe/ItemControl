package no.runsafe.ItemControl;

import no.runsafe.framework.event.player.IPlayerRightClickAir;
import no.runsafe.framework.server.block.RunsafeBlock;
import no.runsafe.framework.server.entity.PassiveEntity;
import no.runsafe.framework.server.entity.RunsafeEntity;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Boat;

public class ItemOnAir implements IPlayerRightClickAir
{
	@Override
	public boolean OnPlayerRightClick(RunsafePlayer player, RunsafeItemStack usingItem, RunsafeBlock targetBlock)
	{
		if (usingItem != null)
		{
			if (usingItem.getItemId() == Material.BLAZE_POWDER.getId())
			{
				if (player.isInsideVehicle())
				{
					RunsafeEntity entity = player.getVehicle();
					if (entity.getEntityType() == PassiveEntity.Boat)
					{
						Boat boat = (Boat) entity.getRaw();
						boat.setMaxSpeed(0.4D * 10);
						usingItem.remove(1);
					}
				}
			}
		}
		return true;
	}
}
