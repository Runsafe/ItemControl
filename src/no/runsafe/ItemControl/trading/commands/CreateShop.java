package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.ItemTagIDRepository;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class CreateShop extends PlayerCommand
{
	public CreateShop(TradingHandler handler)
	{
		super(
			"create",
			"Create a shop",
			"runsafe.traders.create",
			new TagArgument(TAG_NAME, handler)
		);
		this.handler = handler;
	}

	private static final String TAG_NAME = "tagName";

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		String tag = parameters.getValue(TAG_NAME);
		handler.getCreatingPlayers().put(executor, tag);
		return "&eClick a button to turn it into a shop!";
	}

	private final TradingHandler handler;
}
