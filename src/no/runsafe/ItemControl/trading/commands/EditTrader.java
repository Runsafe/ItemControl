package no.runsafe.ItemControl.trading.commands;

import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

import java.util.ArrayList;
import java.util.List;

public class EditTrader extends PlayerCommand
{
	public EditTrader()
	{
		super("edit", "Edit a trader", "runsafe.traders.edit");
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		editingPlayers.add(executor.getName());
		return "&eRight-click a merchant to edit it!";
	}

	public static boolean playerIsTracked(String playerName)
	{
		return editingPlayers.contains(playerName);
	}

	public static void stopTrackingPlayer(String playerName)
	{
		editingPlayers.remove(playerName);
	}

	private final static List<String> editingPlayers = new ArrayList<String>(0);
}
