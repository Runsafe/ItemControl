package no.runsafe.ItemControl.trading;

import no.runsafe.ItemControl.ItemControl;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Sound;
import no.runsafe.framework.minecraft.enchantment.RunsafeEnchantment;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class PurchaseValidator
{
	public void addPurchaseItem(RunsafeMeta item)
	{
		purchaseItems.add(item);
	}

	public void addRequiredItem(RunsafeMeta item)
	{
		for (Map.Entry<RunsafeMeta, Integer> itemNode : requiredItems.entrySet())
		{
			RunsafeMeta requiredItem = itemNode.getKey();
			if (strictItemMatch(requiredItem, item))
			{
				requiredItems.put(requiredItem, itemNode.getValue() + item.getAmount());
				return;
			}
		}
		requiredItems.put(item, item.getAmount());
	}

	private boolean playerCanPurchase(IPlayer player)
	{
		ConcurrentHashMap<RunsafeMeta, Integer> checklist = new ConcurrentHashMap<>(requiredItems.size());
		checklist.putAll(requiredItems);

		ItemControl.Debugger.debugFine(
			"Player %s attempting to make a purchase that requires payment of: %s",
			player.getName(), serialize(checklist)
		);

		for (RunsafeMeta item : player.getInventory().getContents())
		{
			for (Map.Entry<RunsafeMeta, Integer> checkNode : checklist.entrySet())
			{
				int checkItemAmount = checkNode.getValue();
				RunsafeMeta checkItem = checkNode.getKey();

				ItemControl.Debugger.debugFiner("Comparing %s against %s", item.serialize(), checkItem.serialize());

				if (strictItemMatch(item, checkItem))
				{
					if (item.getAmount() >= checkItemAmount)
						checklist.remove(checkItem);
					else
						checklist.put(checkItem, checkItemAmount - item.getAmount());

					ItemControl.Debugger.debugFiner("Passed: %s is %s", item.serialize(), checkItem.serialize());
				}
				else
					ItemControl.Debugger.debugFiner("Failed: %s is not %s", item.serialize(), checkItem.serialize());
			}
		}

		if (!checklist.isEmpty())
			ItemControl.Debugger.debugFine(
				"Player %s does not have required payment. missing: %s",
				player.getName(), serialize(checklist)
			);
		else
			ItemControl.Debugger.debugFine("Player %s has required items to make purchase", player.getName());

		return checklist.isEmpty();
	}

	private String serialize(Map<RunsafeMeta, Integer> itemList)
	{
		List<String> items = new ArrayList<>();

		for (Map.Entry<RunsafeMeta, Integer> itemEntry : itemList.entrySet())
		{
			RunsafeMeta item = itemEntry.getKey();
			int numberOfItems = itemEntry.getValue();
			items.add("(" + item.serialize() + " Count: " + numberOfItems + " )");
		}

		return StringUtils.join(items, ", ");
	}

	public void purchase(IPlayer player, String tag, ItemTagIDRepository tagRepository)
	{//
		if (!playerCanPurchase(player))
		{
			player.sendColouredMessage("&cYou don't have enough to buy that!");
			return;
		}

		RunsafeInventory playerInventory = player.getInventory();
		List<RunsafeMeta> inventoryItems = playerInventory.getContents();

		for (Map.Entry<RunsafeMeta, Integer> items : requiredItems.entrySet())
		{
			for (RunsafeMeta inventoryItem : inventoryItems)
			{
				if (strictItemMatch(inventoryItem, items.getKey()))
				{
					playerInventory.removeExact(inventoryItem, items.getValue());
					break;
				}
			}
		}

		for (RunsafeMeta item : purchaseItems)
		{
			RunsafeMeta newItem = item.clone();
			if (tag != null)
				newItem.addLore("ID: " + tag + "_" + tagRepository.incrementID(tag));
			player.give(newItem);
		}

		ILocation location = player.getLocation();

		if (location != null)
			location.playSound(Sound.Item.PickUp, 2F, 0F);

		player.sendColouredMessage("&aPurchase complete!");
	}

	private boolean strictItemMatch(RunsafeMeta item, RunsafeMeta check)
	{
		// Check the item is the same.
		if (!item.is(check.getItemType()))
			return false;

		// Check the durability matches
		if (item.getDurability() != check.getDurability())
			return false;

		String displayName = item.getDisplayName();
		String checkName = check.getDisplayName();

		// Check the names are either both null, or both not null.
		if ((displayName == null && checkName != null) || (displayName != null && checkName == null))
			return false;

		// Check the names match.
		if (displayName != null && !checkName.equals(displayName))
			return false;

		// Check the lore.
		List<String> itemLore = item.getLore();
		List<String> checkLore = item.getLore();

		// Check the lore lists are either both null, or both not null.
		if ((itemLore == null && checkLore != null) || (itemLore != null && checkLore == null))
			return false;

		// Make sure both the lore lists are the same (order-sensitive).
		if (itemLore != null && !itemLore.equals(checkLore))
			return false;

		// Enchant checking
		Map<RunsafeEnchantment, Integer> enchants = item.getEnchantments();

		// Check both enchantment maps are the same size, otherwise they don't match.
		if (enchants.size() != check.getEnchantments().size())
			return false;

		// Check all the enchants match.
		for (Map.Entry<RunsafeEnchantment, Integer> enchantNode : enchants.entrySet())
		{
			RunsafeEnchantment enchant = enchantNode.getKey();

			// Make sure the enchant exists on the item and has the same level.
			if (!check.hasEnchant(enchant) || check.getEnchantLevel(enchant) != enchantNode.getValue())
				return false;
		}
		return true;
	}

	private final List<RunsafeMeta> purchaseItems = new ArrayList<>(0);
	private final ConcurrentHashMap<RunsafeMeta, Integer> requiredItems = new ConcurrentHashMap<>(0);
}
