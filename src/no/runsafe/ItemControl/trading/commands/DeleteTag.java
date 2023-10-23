package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.ItemTagIDRepository;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class DeleteTag  extends PlayerCommand
{
	public DeleteTag(TradingHandler handler)
	{
		super(
			"deleteTag",
			"Deletes an item tag.",
			"runsafe.traders.tag.delete",
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

		handler.deleteTag(tag);
		return "&aTag information cleared for:" + tag;
	}

	private final TradingHandler handler;
}
