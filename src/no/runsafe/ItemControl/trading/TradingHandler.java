package no.runsafe.ItemControl.trading;

import no.runsafe.ItemControl.Globals;
import no.runsafe.ItemControl.ItemControl;
import no.runsafe.framework.api.*;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.block.ISign;
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
	public TradingHandler(TradingRepository tradingRepository, PlayerTransactionRepository playerTransactionRepository, ItemTagIDRepository tagRepository, IScheduler scheduler, IServer server)
	{
		this.tradingRepository = tradingRepository;
		this.playerTransactionRepository = playerTransactionRepository;
		this.tagRepository = tagRepository;
		this.scheduler = scheduler;
		this.server = server;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		reloadTraderData();

		shopScoreSignList.clear();
		shopScoreSignList.putAll(Globals.getShopScoreboardList());
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

	public Map<IPlayer, String> getTagAddingPlayers()
	{
		return tagAddingPlayers;
	}

	public List<IPlayer> getTagRemovingPlayers()
	{
		return tagRemovingPlayers;
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
		playerTransactionRepository.deleteTagRecords(tag);
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

		ILocation targetBlockLoc = targetBlock.getLocation();
		if (handleTraderDeletion(player, targetBlockLoc)
			|| handleTraderDebugging(player, targetBlockLoc)
			|| handleTraderTagAssign(player, targetBlockLoc)
			|| handleTraderTagRemoval(player, targetBlockLoc)
		)
		{
			targetBlockLoc.playSound(Sound.Redstone.ComparatorClick, 2F, 0F);
			return false;
		}

		boolean isEditing = creatingPlayers.containsKey(player);
		ItemControl.Debugger.debugFine(isEditing ? "Player is editing shop" : "Player not editing shop");

		PurchaseData purchaseData = creatingPlayers.get(player);

		String worldName = player.getWorldName();

		TraderData targetedTrader = getTraderFromLocation(targetBlockLoc);
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
				targetBlockLoc.playSound(Sound.Redstone.ComparatorClick, 2F, 0F);
				return false;
			}

			String shopTag = targetedTrader.getTag();
			if (shopTag != null)
			{
				updateSigns(shopTag);
				playerTransactionRepository.recordPurchase(player, shopTag);
				return targetedTrader.getPurchaseValidator().purchase(player, shopTag, tagRepository);
			}
			return targetedTrader.getPurchaseValidator().purchase(player, null, null);
		}

		// We're editing but nothing is linked to this button.
		if (!isEditing)
			return true;

		ItemControl.Debugger.debugFine("Shop does not exist, creating new.");
		RunsafeInventory inventory = server.createInventory(null, 27);
		TraderData newData = new TraderData(targetBlockLoc, inventory, purchaseData.getTag(),
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
		targetBlockLoc.playSound(Sound.Redstone.ComparatorClick, 2F, 0F);

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

	private boolean handleTraderTagAssign(IPlayer player, ILocation location)
	{
		if (!tagAddingPlayers.containsKey(player))
			return false;

		String tag = tagAddingPlayers.get(player);
		tagAddingPlayers.remove(player);

		TraderData targetedTrader = getTraderFromLocation(location);
		if (targetedTrader == null)
			return true;

		targetedTrader.setTag(tag);
		tradingRepository.updateTrader(targetedTrader);
		reloadTraderData();

		return true;
	}

	private boolean handleTraderTagRemoval(IPlayer player, ILocation location)
	{
		if (!tagRemovingPlayers.contains(player))
			return false;

		tagRemovingPlayers.remove(player);

		TraderData targetedTrader = getTraderFromLocation(location);
		if (targetedTrader == null)
			return true;

		targetedTrader.setTag(null);
		tradingRepository.updateTrader(targetedTrader);
		reloadTraderData();

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

	private void updateSigns(String tag)
	{
		if (shopScoreSignList.isEmpty() || !shopScoreSignList.containsKey(tag))
			return;

		List<ILocation> shopSigns = shopScoreSignList.get(tag);

		if (shopSigns.size() != 2)
		{
			ItemControl.Debugger.debugFine("Failed to update shop signs for %s, wrong number of sign locations.", tag);
			return;
		}

		Map<IPlayer, Integer> playerList = playerTransactionRepository.getTopPlayers(tag);

		IBlock signBlockUsernames = shopSigns.get(0).getBlock();
		IBlock signBlockScores = shopSigns.get(1).getBlock();

		if (!(signBlockUsernames instanceof ISign) || !(signBlockScores instanceof ISign))
		{
			ItemControl.Debugger.debugFine("Failed to update shop signs for %s, block locatinos are not signs.", tag);
			return;
		}

		if (playerList.isEmpty())
		{
			((ISign) signBlockUsernames).setLine( 0, "");
			((ISign) signBlockUsernames).setLine(1, "N/A");
			((ISign) signBlockUsernames).setLine(2, "");
			((ISign) signBlockUsernames).setLine(3, "");

			((ISign) signBlockScores).setLine(0, "");
			((ISign) signBlockScores).setLine(1, "N/A");
			((ISign) signBlockScores).setLine(2, "");
			((ISign) signBlockScores).setLine(3, "");

			return;
		}

		int line = 0;
		for (Map.Entry<IPlayer, Integer> node : playerList.entrySet())
		{
			String username = node.getKey().getName();
			int score = node.getValue();

			((ISign) signBlockUsernames).setLine(line, username);
			((ISign) signBlockScores).setLine(line, score + " " + tag);

			line++;
			if (line == 4)
				return;
		}

		// make sure remaining lines are blank if sign score has been reset
		if (line < 2)
		{
			((ISign) signBlockUsernames).setLine(1, "");
			((ISign) signBlockScores).setLine(1, "");
		}
		if (line < 3)
		{
			((ISign) signBlockUsernames).setLine(2, "");
			((ISign) signBlockScores).setLine(2, "");
		}
		if (line < 4)
		{
			((ISign) signBlockUsernames).setLine(3, "");
			((ISign) signBlockScores).setLine(3, "");
		}
	}

	private final ConcurrentHashMap<String, List<TraderData>> data = new ConcurrentHashMap<>(0);
	private final Map<IPlayer, PurchaseData> creatingPlayers = new HashMap<>(0);
	private final List<IPlayer> deletingPlayers = new ArrayList<>(0);
	private final List<IPlayer> debuggingPlayers = new ArrayList<>(0);
	private final Map<IPlayer, String> tagAddingPlayers = new HashMap<>(0);
	private final List<IPlayer> tagRemovingPlayers = new ArrayList<>(0);
	private final Map<String, List<ILocation>> shopScoreSignList = new HashMap<>(0);
	private final TradingRepository tradingRepository;
	private final PlayerTransactionRepository playerTransactionRepository;
	private final ItemTagIDRepository tagRepository;
	private final IScheduler scheduler;
	private final IServer server;
	private int timerID = -1;
}
