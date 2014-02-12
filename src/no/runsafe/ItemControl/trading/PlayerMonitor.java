package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

public class PlayerMonitor implements IPlayerInteractEntityEvent
{
	public PlayerMonitor(TradingHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
	{
		IPlayer player = event.getPlayer();
		player.sendColouredMessage("Entity interact event detected");

		RunsafeEntity entity = event.getRightClicked();
		if (entity.getEntityType() == LivingEntity.Villager && handler.isTrader(entity))
		{
			player.sendColouredMessage("Is villager/trader");
			RunsafeInventory inventory = handler.getTraderInventory(entity);
			for (int col = 0; col < 10; col++)
			{
				RunsafeMeta firstItem = inventory.getItemInSlot(col);
				if (firstItem == null)
				{
					player.sendColouredMessage("First item is null");
					return;
				}

				RunsafeMeta result = inventory.getItemInSlot(col + 9);
				if (result == null)
				{
					player.sendColouredMessage("Result is null");
					return;
				}

				VillagerController controller = new VillagerController(entity);
				controller.clearTrades();

				RunsafeMeta secondItem = inventory.getItemInSlot(col + 18);
				if (secondItem != null)
					controller.addTrade(firstItem, secondItem, result);
				else
					controller.addTrade(firstItem, result);
			}
		}
	}

	private final TradingHandler handler;
}
