package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.TradeNode;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.entity.LivingEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEntityEvent;

import java.util.HashMap;

public class CreateTrader extends PlayerCommand implements IPlayerInteractEntityEvent
{
	public CreateTrader(TradingHandler handler)
	{
		super("createtrader", "Create a trader!", "runsafe.itemcontrol.traders.create", new RequiredArgument("trade"));
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		String playerName = executor.getName();
		if (!interactTrack.containsKey(playerName))
			interactTrack.put(playerName, parameters.get("trade"));

		return "&eRight-click on a villager to make it a trader!";
	}

	@Override
	public void OnPlayerInteractEntityEvent(RunsafePlayerInteractEntityEvent event)
	{
		IPlayer player = event.getPlayer();
		String playerName = player.getName();

		if (interactTrack.containsKey(playerName))
		{
			RunsafeEntity entity = event.getRightClicked();
			if (entity.getEntityType() == LivingEntity.Villager)
			{
				String tradeID = interactTrack.get(playerName);
				if (handler.isTrader(entity))
					handler.getTradeNode(entity).setTestValue(tradeID);
				else
					handler.setTradeNode(entity, new TradeNode(tradeID));

				player.sendColouredMessage("Villager node: " + handler.getTradeNode(entity).getTestValue());
			}
		}
	}

	private final HashMap<String, String> interactTrack = new HashMap<String, String>(0);
	private final TradingHandler handler;
}
