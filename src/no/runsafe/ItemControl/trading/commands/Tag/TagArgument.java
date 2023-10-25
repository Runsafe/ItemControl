package no.runsafe.ItemControl.trading.commands.Tag;

import no.runsafe.ItemControl.trading.TradingHandler;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.ITabComplete;
import no.runsafe.framework.api.command.argument.IValueExpander;
import no.runsafe.framework.api.command.argument.OptionalArgument;
import no.runsafe.framework.api.player.IPlayer;

import javax.annotation.Nullable;
import java.util.List;

public class TagArgument extends OptionalArgument implements ITabComplete, IValueExpander
{
	public TagArgument(TradingHandler handler)
	{
		super("itemTag");
		this.handler = handler;
	}

	public TagArgument(String name, TradingHandler handler)
	{
		super(name);
		this.handler = handler;
	}

	@Override
	public List<String> getAlternatives(IPlayer executor, String partial)
	{
		return handler.getAllTags();
	}

	@Nullable
	@Override
	public String expand(ICommandExecutor context, @Nullable String value)
	{
		if (value == null)
			return null;
		for (String alternative : handler.getAllTags())
			if (alternative.toUpperCase().startsWith(value.toUpperCase()))
				return alternative;

		return null;
	}

	private final TradingHandler handler;
}
