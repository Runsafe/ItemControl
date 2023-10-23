package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.ItemTagIDRepository;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class CreateShop extends PlayerCommand
{
	public CreateShop(TradingHandler handler, ItemTagIDRepository tagRepository)
	{
		super(
			"create",
			"Create a shop",
			"runsafe.traders.create",
			new TagArgument(TAG_NAME, tagRepository)
		);
		this.handler = handler;
	}

	private static final String TAG_NAME = "tagName";

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.getCreatingPlayers().put(executor, parameters.<String>getValue(TAG_NAME));
		return "&eClick a button to turn it into a shop!";
	}

	private final TradingHandler handler;
}
