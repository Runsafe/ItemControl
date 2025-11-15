package no.runsafe.ItemControl.trading.commands.Tag;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class Assign extends PlayerCommand
{
	public Assign(TradingHandler handler)
	{
		super(
			"assign",
			"Assigns a tag to a shop.",
			"runsafe.traders.tag.assign",
			new TagArgument(TAG_NAME, handler).require()
		);
		this.handler = handler;
	}

	private static final String TAG_NAME = "tagName";

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		String tag = parameters.getRequired(TAG_NAME);

		if (!handler.getAllTags().contains(tag))
			return "&cInvalid item tag.";

		handler.getTagAddingPlayers().put(executor, tag);
		return "&aRight click a shop button to add the tag: " + tag;
	}

	private final TradingHandler handler;
}
