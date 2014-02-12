package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;

public class EditMonitor extends Monitor
{
	public EditMonitor(TradingHandler handler)
	{
		this.handler = handler;
	}

	@Override
	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
	{
		IPlayer player = event.getPlayer();

		if (isTrackingPlayer(player))
		{
			RunsafeEntity entity = event.getRightClicked();
			if (entity.getEntityType() == LivingEntity.Villager)
			{
				if (handler.isTrader(entity))
					handler.openTraderEditor(player, entity);
				else
					player.sendColouredMessage("&cThat is not a trader.");
			}
			else
			{
				player.sendColouredMessage("&cThat is not a villager.");
			}
			stopTracking(player);
			event.cancel();
		}
	}

	private final TradingHandler handler;
}
