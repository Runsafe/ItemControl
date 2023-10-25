package no.runsafe.ItemControl.trading;

import no.runsafe.ItemControl.ItemControl;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TradingHandler implements IConfigurationChanged, IPlayerRightClickBlock
{
	public TradingHandler(TradingRepository tradingRepository, ItemTagIDRepository tagRepository, IScheduler scheduler, IServer server)
	{
		this.tradingRepository = tradingRepository;
		this.tagRepository = tagRepository;
		this.scheduler = scheduler;
		this.server = server;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		data.clear(); // Clear existing data.
		List<TraderData> rawData = tradingRepository.getTraders(); // Grab trader data from the database.

		// Populate our cache with trader data!
		for (TraderData node : rawData)
			storeTraderData(node);
	}

	private void storeTraderData(TraderData node)
	{
		ILocation location = node.getLocation();

		if (location == null)
			return;

		IWorld world = location.getWorld();

		if (world == null)
			return;

		String worldName = world.getName();
		if (!data.containsKey(worldName))
			data.put(worldName, new ArrayList<TraderData>(1));

		data.get(worldName).add(node);
	}

	public Map<IPlayer, String> getCreatingPlayers()
	{
		return creatingPlayers;
	}

	public boolean createTag(String tag)
	{
		if (tag == null || tagRepository.getTags().contains(tag))
			return false;

		tagRepository.createNewTag(tag);
		return true;
	}

	public void deleteTag(String tag)
	{
		tradingRepository.deleteTag(tag);
		tagRepository.deleteTag(tag);
	}

	public List<String> getAllTags()
	{
		return tagRepository.getTags();
	}

	public Map<String, Integer> getAllTagInfo()
	{
		return tagRepository.getTagInfo();
	}

	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
	{
		if (!targetBlock.is(Item.Redstone.Button.Wood) && !targetBlock.is(Item.Redstone.Button.Stone))
			return true;

		boolean isEditing = creatingPlayers.containsKey(player);
		ItemControl.Debugger.debugFine(isEditing ? "Player is editing shop" : "Player not editing shop");

		String tag = creatingPlayers.get(player);

		String worldName = player.getWorldName();
		if (data.containsKey(worldName))
		{
			ItemControl.Debugger.debugFine("Traders exist for this world: " + worldName);
			List<TraderData> nodes = data.get(worldName);
			for (TraderData node : nodes)
			{
				if (node.getLocation().getWorld() != targetBlock.getWorld())
				{
					ItemControl.Debugger.debugFine("Location is in wrong world.");
					continue;
				}

				ItemControl.Debugger.debugFine("Distance checking shop at : " + node.getLocation().toString());
				if (node.getLocation().distance(targetBlock.getLocation()) < 1)
				{
					ItemControl.Debugger.debugFine("Location is less than 1");
					if (isEditing)
					{
						node.setTag(tag);
						editShop(player, node);
					}
					else
					{
						if (tag != null)
							node.getPurchaseValidator().purchase(player, "ID: " + tag + "_" + tagRepository.incrementID(tag));
						else
							node.getPurchaseValidator().purchase(player, null);
					}

					return true;
				}
				ItemControl.Debugger.debugFine("Location is greater or equal to 1");
			}
		}

		// We're editing but nothing is linked to this button.
		if (!isEditing)
			return true;

		ItemControl.Debugger.debugFine("Shop does not exist, creating new.");
		RunsafeInventory inventory = server.createInventory(null, 27);
		TraderData newData = new TraderData(targetBlock.getLocation(), inventory, tag);
		tradingRepository.persistTrader(newData);

		if (!data.containsKey(worldName))
		{
			ItemControl.Debugger.debugFine("World does not have holder, creating new: " + worldName);
			data.put(worldName, new ArrayList<TraderData>(1));
		}

		data.get(worldName).add(newData);
		editShop(player, newData);


		return true;
	}

	private void editShop(IPlayer player, TraderData traderData)
	{
		creatingPlayers.remove(player); // Remove the player from the tracking list.
		player.openInventory(traderData.getInventory());
		traderData.setSaved(false);

		if (timerID != -1) // Only start a timer if we don't have one already.
			return;

		ItemControl.Debugger.debugFine("Timer does not exist, starting new.");
		timerID = scheduler.startAsyncRepeatingTask(new Runnable()
		{
			@Override
			public void run()
			{
				boolean unsavedRemaining = false;
				for (Map.Entry<String, List<TraderData>> node : data.entrySet())
				{
					for (TraderData data : node.getValue())
					{
						if (data.isSaved())
							continue;

						ItemControl.Debugger.debugFine("Found unsaved node..");
						if (data.getInventory().getViewers().isEmpty())
						{
							ItemControl.Debugger.debugFine("No viewers in the node, saving and persisting in DB");
							tradingRepository.updateTrader(data);
							data.setSaved(true);
							data.refresh();
						}
						else
						{
							ItemControl.Debugger.debugFine("Viewers found, skipping!");
							unsavedRemaining = true;
						}
					}
				}

				if (!unsavedRemaining)
				{
					ItemControl.Debugger.debugFine("No unsaved remaining, cancelling timer!");
					scheduler.cancelTask(timerID);
					timerID = -1;
				}
			}
		}, 10, 10);
	}

	private final ConcurrentHashMap<String, List<TraderData>> data = new ConcurrentHashMap<String, List<TraderData>>(0);
	private final Map<IPlayer, String> creatingPlayers = new HashMap<IPlayer, String>(0);
	private final TradingRepository tradingRepository;
	private final ItemTagIDRepository tagRepository;
	private final IScheduler scheduler;
	private final IServer server;
	private int timerID = -1;
}
