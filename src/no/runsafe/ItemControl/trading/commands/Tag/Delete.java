package no.runsafe.ItemControl.trading.commands.Tag;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;

public class Delete extends ExecutableCommand
{
	public Delete(TradingHandler handler)
	{
		super(
			"delete",
			"Deletes an item tag.",
			"runsafe.traders.tag.delete",
			new TagArgument(TAG_NAME, handler).require()
		);
		this.handler = handler;
	}

	private static final String TAG_NAME = "tagName";

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		String tag = parameters.getRequired(TAG_NAME);

		if (!handler.getAllTags().contains(tag))
			return "&cInvalid item tag.";

		handler.deleteTag(tag);
		return "&aTag information cleared for:" + tag;
	}

	private final TradingHandler handler;
}
