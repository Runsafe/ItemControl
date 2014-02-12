package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.event.plugin.IPluginDisabled;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;

import java.util.HashMap;
import java.util.Map;

public class TradingHandler implements IServerReady, IPluginDisabled
{
	public TradingHandler(TradingRepository repository, IServer server)
	{
		this.repository = repository;
		this.server = server;
	}

	public boolean isTrader(RunsafeEntity entity)
	{
		return traders.containsKey(entity.getUniqueId().toString());
	}

	public void makeTrader(RunsafeEntity entity)
	{
		traders.put(entity.getUniqueId().toString(), createTraderInventory());
	}

	private RunsafeInventory createTraderInventory()
	{
		return server.createInventory(null, 27, "Trader Editor");
	}

	public void openTraderEditor(IPlayer viewer, RunsafeEntity trader)
	{
		String traderID = trader.getUniqueId().toString();
		if (traders.containsKey(traderID))
			viewer.openInventory(traders.get(traderID));
	}

	@Override
	public void OnServerReady()
	{
		// Load all traders from the database.
		HashMap<String, String> rawTraders = repository.getTraders();
		for (Map.Entry<String, String> rawTrader : rawTraders.entrySet())
		{
			RunsafeInventory inventory = createTraderInventory();
			inventory.unserialize(rawTrader.getValue());
			traders.put(rawTrader.getKey(), inventory);
		}
	}

	@Override
	public void OnPluginDisabled()
	{
		repository.persistTraders(traders); // Persist all traders when we shut down.
	}

	private HashMap<String, RunsafeInventory> traders = new HashMap<String, RunsafeInventory>(0);
	private final TradingRepository repository;
	private final IServer server;
}
