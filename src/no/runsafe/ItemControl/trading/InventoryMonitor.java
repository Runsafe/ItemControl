package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.event.inventory.IInventoryClick;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;

public class InventoryMonitor implements IInventoryClick
{
	@Override
	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
	{
		RunsafeInventory inventory = event.getInventory();
		if (inventory.getType() == RunsafeInventoryType.MERCHANT)
		{
			IPlayer player = event.getWhoClicked();
			player.sendColouredMessage("You clicked slot: " + event.getSlot());
		}
	}
}
