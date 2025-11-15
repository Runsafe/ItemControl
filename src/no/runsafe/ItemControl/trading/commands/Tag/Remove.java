package no.runsafe.ItemControl.trading.commands.Tag;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class Remove extends PlayerCommand
{
	public Remove(TradingHandler handler)
	{
		super(
			"remove",
			"Remove a tag from a shop without deleting it.",
			"runsafe.traders.tag.remove"
		);
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		handler.getTagRemovingPlayers().add(executor);
		return "&aRight click a shop button to remove its tag.";
	}

	private final TradingHandler handler;
}
