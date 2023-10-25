package no.runsafe.ItemControl.trading.commands.Tag;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;

public class Create extends ExecutableCommand
{
	protected Create(TradingHandler handler)
	{
		super(
			"create",
			"Creates a new item tag.",
			"runsafe.traders.tag.create",
			new RequiredArgument(TAG_NAME)
		);
		this.handler = handler;
	}

	private static final String TAG_NAME = "tagName";
	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		String tag = parameters.getRequired(TAG_NAME);

		if (handler.createTag(tag))
			return "&aTag created!";

		return "&cTag could not be created.";
	}

	private final TradingHandler handler;
}
