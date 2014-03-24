package no.runsafe.ItemControl.trading;

import no.runsafe.ItemControl.ItemControl;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.framework.tools.ItemCompacter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TradingHandler implements IConfigurationChanged, IPlayerRightClickBlock
{
	public TradingHandler(TradingRepository repository, IScheduler scheduler, IServer server)
	{
		this.repository = repository;
		this.scheduler = scheduler;
		this.server = server;
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

	public List<String> getCreatingPlayers()
	{
		return creatingPlayers;
	}

	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
	{
		if (targetBlock.is(Item.Redstone.Button.Wood) || targetBlock.is(Item.Redstone.Button.Stone))
		{
			boolean isEditing = creatingPlayers.contains(player.getName());
			ItemControl.Debugger.debugFine(isEditing ? "Player is editing shop" : "Player not editing shop");

			String worldName = player.getWorldName();
			if (data.containsKey(worldName))
			{
				ItemControl.Debugger.debugFine("Traders exist for this world: " + worldName);
				List<TraderData> nodes = data.get(worldName);
				for (TraderData node : nodes)
				{
					ItemControl.Debugger.debugFine("Distance checking shop at : " + node.getLocation().toString());
					if (node.getLocation().distance(targetBlock.getLocation()) < 1)
					{
						ItemControl.Debugger.debugFine("Location is less than 1");
						if (isEditing)
							editShop(player, node);
						else
							handlePurchase(player, node.getInventory());

						return true;
					}
					ItemControl.Debugger.debugFine("Location is greater or equal to 1");
				}
			}

			// We're editing but nothing is linked to this button.
			if (isEditing)
			{
				ItemControl.Debugger.debugFine("Shop does not exist, creating new.");
				RunsafeInventory inventory = server.createInventory(null, 27);
				TraderData newData = new TraderData(targetBlock.getLocation(), inventory);
				repository.persistTrader(newData);

				if (!data.containsKey(worldName))
				{
					ItemControl.Debugger.debugFine("World does not have holder, creating new: " + worldName);
					data.put(worldName, new ArrayList<TraderData>(1));
				}

				data.get(worldName).add(newData);
				editShop(player, newData);
			}
		}
		return true;
	}

	private void handlePurchase(IPlayer player, RunsafeInventory inventory)
	{
		ItemControl.Debugger.debugFine("-- HANDLING PURCHASE --");
		List<RunsafeMeta> remove = new ArrayList<RunsafeMeta>(0);
		HashMap<String, Integer> requiredAmounts = new HashMap<String, Integer>(0);

		// Calculate what we need.
		for (int i = 0; i < 9; i++)
		{
			RunsafeMeta item = inventory.getItemInSlot(i);
			if (item == null)
				continue;

			int requiredAmount = item.getAmount();
			ItemControl.Debugger.debugFine("Item stack amount: " + requiredAmount);

			// Make a clone item to compact to prevent item count blurring the result
			RunsafeMeta clone = item.clone();
			clone.setAmount(1);
			String itemString = ItemCompacter.convertToString(clone);
			ItemControl.Debugger.debugFine("Item String: " + itemString);

			if (requiredAmounts.containsKey(itemString))
			{
				ItemControl.Debugger.debugFine("Item already has amount, increasing!");
				requiredAmount += requiredAmounts.get(itemString);
			}

			ItemControl.Debugger.debugFine("Updated amount: " + requiredAmount);
			requiredAmounts.put(itemString, requiredAmount);
			remove.add(item);
		}

		ItemControl.Debugger.debugFine("Checking players inventory.");
		RunsafeInventory playerInventory = player.getInventory();
		for (RunsafeMeta item : playerInventory.getContents())
		{
			RunsafeMeta clone = item.clone();
			clone.setAmount(1);
			String itemString = ItemCompacter.convertToString(clone);
			ItemControl.Debugger.debugFine("Item in inventory: " + itemString);

			if (requiredAmounts.containsKey(itemString))
			{
				ItemControl.Debugger.debugFine("Required Amounts contains this item!");

				int amountLeft = requiredAmounts.get(itemString);
				ItemControl.Debugger.debugFine("Amount left: " + amountLeft);

				if (item.getAmount() >= amountLeft)
				{
					ItemControl.Debugger.debugFine("Stack has more/equal than left, removing requirement");
					requiredAmounts.remove(itemString);
				}
				else
				{
					ItemControl.Debugger.debugFine("Stack has less than needed, reducing amount");
					requiredAmounts.put(itemString, amountLeft - item.getAmount());
				}
			}
		}

		if (requiredAmounts.isEmpty())
		{
			for (RunsafeMeta removeItem : remove)
				playerInventory.removeExact(removeItem, removeItem.getAmount());

			for (int i = 18; i < 27; i++)
			{
				RunsafeMeta item = inventory.getItemInSlot(i);

				if (item != null)
					player.give(item);
			}

			player.getLocation().playSound(Sound.Item.PickUp, 2F, 0F);
			player.sendColouredMessage("&aPurchase complete!");
		}
		else
		{
			player.sendColouredMessage("&cYou don't have enough to buy that!");
		}
		ItemControl.Debugger.debugFine("-- PURCHASE HANDLED --");
	}

	private void editShop(IPlayer player, TraderData traderData)
	{
		creatingPlayers.remove(player.getName()); // Remove the player from the tracking list.
		player.openInventory(traderData.getInventory());
		traderData.setSaved(false);

		if (timerID == -1) // Only start a timer if we don't have one already.
		{
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
							if (!data.isSaved())
							{
								ItemControl.Debugger.debugFine("Found unsaved node..");
								if (data.getInventory().getViewers().isEmpty())
								{
									ItemControl.Debugger.debugFine("No viewers in the node, saving and persisting in DB");
									repository.updateTrader(data);
									data.setSaved(true);
								}
								else
								{
									ItemControl.Debugger.debugFine("Viewers found, skipping!");
									unsavedRemaining = true;
								}
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
	}

	private ConcurrentHashMap<String, List<TraderData>> data = new ConcurrentHashMap<String, List<TraderData>>(0);
	private final List<String> creatingPlayers = new ArrayList<String>();
	private final TradingRepository repository;
	private final IScheduler scheduler;
	private final IServer server;
	private int timerID = -1;
}
