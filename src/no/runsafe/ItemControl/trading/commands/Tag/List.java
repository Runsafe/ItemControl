package no.runsafe.ItemControl.trading.commands.Tag;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;

import java.util.Map;

public class List extends ExecutableCommand
{
	public List(TradingHandler handler)
	{
		super(
			"list",
			"Lists all tags.",
			"runsafe.traders.tag.list"
		);
		this.handler = handler;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		Map<String, Integer> tags = handler.getAllTagInfo();
		if (tags == null || tags.isEmpty())
			return "&cNo tags found.";

		StringBuilder info = new StringBuilder();
		info.append("&eCurrently used tags:&a\n");

		for(String tag : tags.keySet())
			info.append("(" + tag + " ID: " + tags.get(tag) + ") ");

		return info.toString();
	}

	private final TradingHandler handler;
}
