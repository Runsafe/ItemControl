package no.runsafe.ItemControl.trading.commands;

import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class EditTrader extends PlayerCommand
{
	public EditTrader(EditMonitor monitor)
	{
		super("edit", "Edit a trader", "runsafe.traders.edit");
		this.monitor = monitor;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		monitor.trackPlayer(executor);
		return "&eRight-click on a trader to edit it.";
	}

	private final EditMonitor monitor;
}
