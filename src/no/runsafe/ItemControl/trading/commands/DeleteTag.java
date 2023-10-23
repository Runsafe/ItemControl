package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.ItemTagIDRepository;
import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class DeleteTag  extends PlayerCommand
{
	public DeleteTag(TradingHandler handler, ItemTagIDRepository tagRepository)
	{
		super(
			"deleteTag",
			"Deletes an item tag.",
			"runsafe.traders.tag.delete",
			new TagArgument(TAG_NAME, tagRepository).require()
		);
		this.handler = handler;
		this.tagRepository = tagRepository;
	}

	private static final String TAG_NAME = "tagName";

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		String tag = parameters.getRequired(TAG_NAME);

		if (!tagRepository.getTags().contains(tag))
			return "&cInvalid item tag.";

		handler.deleteTag(tag);
		return "&aTag information cleared for:" + tag;
	}

	private final TradingHandler handler;
	private final ItemTagIDRepository tagRepository;
}
