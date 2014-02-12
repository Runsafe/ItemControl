package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;

public class EditTrader extends PlayerCommand implements IPlayerInteractEntityEvent
{
	public EditTrader(TradingHandler handler)
	{
		super("edit", "Edit a trader", "runsafe.traders.edit");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		String playerName = executor.getName();
		if (!interactTrack.contains(playerName))
			interactTrack.add(playerName);

		return "&eRight-click on a trader to edit it.";
	}

	@Override
	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
	{
		IPlayer player = event.getPlayer();
		String playerName = player.getName();

		if (interactTrack.contains(playerName))
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
			interactTrack.remove(playerName);
			event.cancel();
		}
	}

	private List<String> interactTrack = new ArrayList<String>(0);
	private final TradingHandler handler;
}
