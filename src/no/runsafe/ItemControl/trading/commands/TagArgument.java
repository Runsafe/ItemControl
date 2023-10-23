package no.runsafe.ItemControl.trading.commands;

import no.runsafe.ItemControl.trading.ItemTagIDRepository;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.ITabComplete;
import no.runsafe.framework.api.command.argument.IValueExpander;
import no.runsafe.framework.api.command.argument.OptionalArgument;
import no.runsafe.framework.api.player.IPlayer;

import javax.annotation.Nullable;
import java.util.List;

public class TagArgument extends OptionalArgument implements ITabComplete, IValueExpander
{
	public TagArgument(ItemTagIDRepository repository)
	{
		super("itemTag");
		this.repository = repository;
	}

	public TagArgument(String name, ItemTagIDRepository repository)
	{
		super(name);
		this.repository = repository;
	}

	@Override
	public List<String> getAlternatives(IPlayer executor, String partial)
	{
		return repository.getTags();
	}

	@Nullable
	@Override
	public String expand(ICommandExecutor context, @Nullable String value)
	{
		if (value == null)
			return null;
		for (String alternative : repository.getTags())
			if (alternative.toUpperCase().startsWith(value.toUpperCase()))
				return alternative;

		return null;
	}

	private final ItemTagIDRepository repository;
}
