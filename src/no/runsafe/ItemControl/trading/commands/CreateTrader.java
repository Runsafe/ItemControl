package no.runsafe.ItemControl.trading.commands;

import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class CreateTrader extends PlayerCommand
{
	public CreateTrader(CreateMonitor monitor)
	{
		super("create", "Create a trader", "runsafe.traders.create");
		this.monitor = monitor;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		monitor.trackPlayer(executor);
		return "&eRight-click on a villager to make it a trader!";
	}
	private final CreateMonitor monitor;
}
