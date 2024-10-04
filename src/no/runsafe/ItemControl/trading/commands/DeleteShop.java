package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.Globals;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class DeleteShop extends PlayerCommand
{
	public DeleteShop(TradingHandler handler)
	{
		super("delete", "Deletes a shop", "runsafe.traders.delete");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.getDeletingPlayers().add(executor);
		return Globals.getCommandsShopDeleteMessage();
	}

	private final TradingHandler handler;
}
