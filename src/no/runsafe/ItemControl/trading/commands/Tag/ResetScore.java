package no.runsafe.ItemControl.trading.commands.Tag;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;

public class ResetScore extends ExecutableCommand
{
	public ResetScore(TradingHandler handler)
	{
		super(
			"resetscore",
			"Resets player scores for this tag.",
			"runsafe.traders.tag.resetscore",
			new RequiredArgument(TAG_NAME)
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

		handler.deleteTagPlayerScore(tag);
		return "&aTag player score information cleared for:" + tag;
	}

	private final TradingHandler handler;
}