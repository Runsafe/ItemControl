package no.runsafe.ItemControl.trading.commands;

import no.runsafe.framework.api.event.player.IPlayerInteractEntityEvent;
import no.runsafe.framework.api.player.IPlayer;

import java.util.ArrayList;
import java.util.List;

public abstract class Monitor implements IPlayerInteractEntityEvent
{
	public void trackPlayer(IPlayer player)
	{
		players.add(player.getName());
	}

	public boolean isTrackingPlayer(IPlayer player)
	{
		return players.contains(player.getName());
	}

	public void stopTracking(IPlayer player)
	{
		players.remove(player.getName());
	}

	private List<String> players = new ArrayList<String>(0);
}
