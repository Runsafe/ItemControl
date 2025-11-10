package no.runsafe.ItemControl.trading;

import no.runsafe.ItemControl.ItemControl;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.player.IPlayerRightClickBlock;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.Sound;
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
		reloadTraderData();
	}

	private void reloadTraderData()
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
			data.put(worldName, new ArrayList<>(1));

		data.get(worldName).add(node);
	}

	public Map<IPlayer, PurchaseData> getCreatingPlayers()
	{
		return creatingPlayers;
	}

	public List<IPlayer> getDeletingPlayers()
	{
		return deletingPlayers;
	}

    public List<IPlayer> getDebuggingPlayers()
    {
        return debuggingPlayers;
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

		if (handleTraderDeletion(player, targetBlock.getLocation()))
			return false;

		if (handleTraderDebugging(player, targetBlock.getLocation()))
			return false;

		boolean isEditing = creatingPlayers.containsKey(player);
		ItemControl.Debugger.debugFine(isEditing ? "Player is editing shop" : "Player not editing shop");

		PurchaseData purchaseData = creatingPlayers.get(player);

		String worldName = player.getWorldName();

		TraderData targetedTrader = getTraderFromLocation(targetBlock.getLocation());
		if (targetedTrader != null)
		{
			if (isEditing)
			{
				targetedTrader.setTag(purchaseData.getTag());
				targetedTrader.setCompareName(purchaseData.shouldCompareName());
				targetedTrader.setCompareDurability(purchaseData.shouldCompareDurability());
				targetedTrader.setCompareLore(purchaseData.shouldCompareLore());
				targetedTrader.setCompareEnchants(purchaseData.shouldCompareEnchants());
				editShop(player, targetedTrader);
				return false;
			}

			String shopTag = targetedTrader.getTag();
			if (shopTag != null)
				return targetedTrader.getPurchaseValidator().purchase(player, shopTag, tagRepository);
			return targetedTrader.getPurchaseValidator().purchase(player, null, null);
		}

		// We're editing but nothing is linked to this button.
		if (!isEditing)
			return true;

		ItemControl.Debugger.debugFine("Shop does not exist, creating new.");
		RunsafeInventory inventory = server.createInventory(null, 27);
		TraderData newData = new TraderData(targetBlock.getLocation(), inventory, purchaseData.getTag(),
			purchaseData.shouldCompareName(), purchaseData.shouldCompareDurability(),
			purchaseData.shouldCompareLore(), purchaseData.shouldCompareEnchants()
		);
		tradingRepository.persistTrader(newData);

		if (!data.containsKey(worldName))
		{
			ItemControl.Debugger.debugFine("World does not have holder, creating new: " + worldName);
			data.put(worldName, new ArrayList<>(1));
		}

		data.get(worldName).add(newData);
		editShop(player, newData);

		return false;
	}

	private TraderData getTraderFromLocation(ILocation location)
	{
		String worldName = location.getWorld().getName();
		if (!data.containsKey(worldName))
			return null;

		ItemControl.Debugger.debugFine("Traders exist for this world: " + worldName);
		List<TraderData> nodes = data.get(worldName);
		for (TraderData node : nodes)
		{
			if (!node.getLocation().getWorld().isWorld(location.getWorld()))
			{
				ItemControl.Debugger.debugFine(
						"Location is in wrong world." +
								" Shop: " + node.getLocation().getWorld().getName() +
								" TargetLocation: " + location.getWorld().getName()
				);
				continue;
			}

			ItemControl.Debugger.debugFine("Distance checking shop at : " + node.getLocation().toString());
			if (!(node.getLocation().distance(location) < 1))
			{
				ItemControl.Debugger.debugFine("Location is greater or equal to 1");
				continue;
			}

			ItemControl.Debugger.debugFine("Location is less than 1");
			return node;
		}

		return null;
	}

	private boolean handleTraderDeletion(IPlayer player, ILocation location)
	{
		if (!deletingPlayers.contains(player))
			return false;

		deletingPlayers.remove(player);
		ItemControl.Debugger.debugFine(
			"Player %s is attempting to delete a shop at %s",
			player.getName(), location.toString()
		);
		tradingRepository.deleteTrader(location);
		reloadTraderData();
		location.playSound(Sound.Redstone.ComparatorClick, 2F, 0F);
		return true;
	}

	private boolean handleTraderDebugging(IPlayer player, ILocation location)
	{
		if (!debuggingPlayers.contains(player))
			return false;

		debuggingPlayers.remove(player);

		TraderData shop = getTraderFromLocation(location);
		player.sendColouredMessage("&5Getting information for trader.&r");
		player.sendColouredMessage("&9Location: &r" + shop.getLocation().toString());
		player.sendColouredMessage("&9Compare Item Name: &r" + (shop.shouldCompareName() ? "True" : "False"));
		player.sendColouredMessage("&9Compare Durability: &r" + (shop.shouldCompareDurability() ? "True" : "False"));
		player.sendColouredMessage("&9Compare Lore: &r" + (shop.shouldCompareLore() ? "True" : "False"));
		player.sendColouredMessage("&9Compare Enchants: &r" + (shop.shouldCompareEnchants() ? "True" : "False"));
		if (shop.getTag() != null)
			player.sendColouredMessage("&9 Using the shop tag: &r" + shop.getTag());


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
		timerID = scheduler.startAsyncRepeatingTask(() ->
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
						continue;
					}

					ItemControl.Debugger.debugFine("Viewers found, skipping!");
					unsavedRemaining = true;
				}
			}

			if (!unsavedRemaining)
			{
				ItemControl.Debugger.debugFine("No unsaved remaining, cancelling timer!");
				scheduler.cancelTask(timerID);
				timerID = -1;
			}
		}, 10, 10);
	}

	private final ConcurrentHashMap<String, List<TraderData>> data = new ConcurrentHashMap<>(0);
	private final Map<IPlayer, PurchaseData> creatingPlayers = new HashMap<>(0);
	private final List<IPlayer> deletingPlayers = new ArrayList<>(0);
	private final List<IPlayer> debuggingPlayers = new ArrayList<>(0);
	private final TradingRepository tradingRepository;
	private final ItemTagIDRepository tagRepository;
	private final IScheduler scheduler;
	private final IServer server;
	private int timerID = -1;
}
