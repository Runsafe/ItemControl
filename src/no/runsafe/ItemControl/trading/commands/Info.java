package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.Globals;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class Info extends PlayerCommand
{
	public Info(TradingHandler handler)
	{
		super("info", "Gets information about a shop", "runsafe.traders.info");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.getDebuggingPlayers().add(executor);
		return Globals.getCommandsShopInfoMessage();
	}

	private final TradingHandler handler;
}