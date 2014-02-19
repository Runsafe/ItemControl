package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.chunk.IChunk;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.event.world.IChunkLoad;
import no.runsafe.framework.tools.nms.EntityRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TradingHandler implements IChunkLoad, IConfigurationChanged, IPluginEnabled
{
	public TradingHandler(TradingRepository repository, IServer server)
	{
		this.repository = repository;
		this.server = server;
	}

	@Override
	public void OnChunkLoad(IChunk chunk)
	{
		String worldName = chunk.getWorld().getName();

		// Check if we have any merchants for this world.
		if (data.containsKey(worldName))
		{
			// Loop all merchants we have in this world.
			for (TraderData node : data.get(worldName))
			{
				// Check the merchant should be spawned inside the chunk.
				ILocation location = node.getLocation();
				if (chunk.locationIsInChunk(location))
					spawnTrader(node); // Spawn the merchant!
			}
		}
	}

	private Trader spawnTrader(TraderData data)
	{
		Trader trader = new Trader(data.getLocation(), data.getInventory());
		if (data.getName() != null)
			trader.setCustomName(data.getName());

		return trader;
	}

	public void createTrader(ILocation location, String name)
	{
		TraderData data = new TraderData(location, server.createInventory(null, 36), name);
		spawnTrader(data); // Spawn the trader.
		repository.persistTrader(data); // Persist the trader in the database.
		storeTraderData(data); // Store in our cache.
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		data.clear(); // Clear existing data.
		List<TraderData> rawData = repository.getTraders(); // Grab trader data from the database.

		// Populate our cache with trader data!
		for (TraderData node : rawData)
			storeTraderData(node);
	}

	private void storeTraderData(TraderData node)
	{
		String worldName = node.getLocation().getWorld().getName();
		if (!data.containsKey(worldName))
			data.put(worldName, new ArrayList<TraderData>(1));

		data.get(worldName).add(node);
	}

	@Override
	public void OnPluginEnabled()
	{
		EntityRegister.registerEntity(Trader.class, "Merchant", 120);
	}

	private HashMap<String, List<TraderData>> data = new HashMap<String, List<TraderData>>(0);
	private final TradingRepository repository;
	private final IServer server;
}
