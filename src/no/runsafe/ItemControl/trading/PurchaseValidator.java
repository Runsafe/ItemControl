package no.runsafe.ItemControl.trading;

import no.runsafe.ItemControl.Globals;
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

public class PurchaseValidator
{
	public PurchaseValidator(boolean compareName, boolean compareDurability, boolean compareLore, boolean compareEnchants)
	{
		this.compareName = compareName;
		this.compareDurability = compareDurability;
		this.compareLore = compareLore;
		this.compareEnchants = compareEnchants;
	}

	public void addPurchaseItem(RunsafeMeta item)
	{
		purchaseItems.add(item);
	}

	public void addRequiredItem(RunsafeMeta item)
	{
		for (Map.Entry<RunsafeMeta, Integer> itemNode : requiredItems.entrySet())
		{
			RunsafeMeta requiredItem = itemNode.getKey();
			if (!strictItemMatch(requiredItem, item))
				continue;

			requiredItems.put(requiredItem, itemNode.getValue() + item.getAmount());
			return;
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

				if (!strictItemMatch(item, checkItem))
					continue;

				if (item.getAmount() >= checkItemAmount)
					checklist.remove(checkItem);
				else
					checklist.put(checkItem, checkItemAmount - item.getAmount());
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

	public boolean purchase(IPlayer player, String tag, ItemTagIDRepository tagRepository)
	{
		if (!playerCanPurchase(player))
		{
			player.sendColouredMessage(Globals.getTradersLowFundsMessage());
			return false;
		}

		RunsafeInventory playerInventory = player.getInventory();
		List<RunsafeMeta> inventoryItems = playerInventory.getContents();

		for (Map.Entry<RunsafeMeta, Integer> items : requiredItems.entrySet())
		{
			for (RunsafeMeta inventoryItem : inventoryItems)
			{
				if (strictItemMatch(inventoryItem, items.getKey()))
				{
					removeExact(playerInventory, inventoryItem, items.getValue());
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

		player.sendColouredMessage(Globals.getTradersPurchaceCompleteMessage());
		return true;
	}

	private boolean strictItemMatch(RunsafeMeta item, RunsafeMeta check)
	{
		ItemControl.Debugger.debugFiner("Comparing %s against %s", item.serialize(), check.serialize());

		// Check the item is the same.
		if (item.getType() != check.getType())
		{
			ItemControl.Debugger.debugFiner("Failed: item is not of same type.");
			return false;
		}

		// Check the durability matches
		if (compareDurability && item.getDurability() != check.getDurability())
		{
			ItemControl.Debugger.debugFiner("Failed: different durability values..");
			return false;
		}

		String displayName = item.getDisplayName();
		String checkName = check.getDisplayName();

		// Check the names are either both null, or both not null.
		if (compareName && ((displayName == null && checkName != null) || (displayName != null && checkName == null)))
		{
			ItemControl.Debugger.debugFiner("Failed: different names.");
			return false;
		}

		// Check the names match.
		if (compareName && displayName != null && !checkName.equals(displayName))
		{
			ItemControl.Debugger.debugFiner("Failed: different names.");
			return false;
		}

		// Check the lore.
		List<String> itemLore = item.getLore();
		List<String> checkLore = check.getLore();

		// Check the lore lists are either both null, or both not null.
		if (compareLore && ((itemLore == null && checkLore != null) || (itemLore != null && checkLore == null)))
		{
			ItemControl.Debugger.debugFiner("Failed: different lore.");
			return false;
		}

		// Make sure both the lore lists are the same (order-sensitive).
		if (compareLore && itemLore != null && !itemLore.equals(checkLore))
		{
			ItemControl.Debugger.debugFiner("Failed: different lore.");
			return false;
		}

		// Enchant checking
		Map<RunsafeEnchantment, Integer> enchants = item.getEnchantments();

		// Check both enchantment maps are the same size, otherwise they don't match.
		if (compareEnchants && enchants.size() != check.getEnchantments().size())
		{
			ItemControl.Debugger.debugFiner("Failed: different enchants.");
			return false;
		}

		// Check all the enchants match.
		if (compareEnchants)
		{
			for (Map.Entry<RunsafeEnchantment, Integer> enchantNode : enchants.entrySet())
			{
				RunsafeEnchantment enchant = enchantNode.getKey();

				// Make sure the enchant exists on the item and has the same level.
				if (!check.hasEnchant(enchant) || check.getEnchantLevel(enchant) != enchantNode.getValue())
				{
					ItemControl.Debugger.debugFiner("Failed: different enchants.");
					return false;
				}
			}
		}

		ItemControl.Debugger.debugFiner("Item Comparison Passed");
		return true;
	}

	private void removeExact(RunsafeInventory inventory, RunsafeMeta meta, int amount)
	{
		int needed = amount;
		for (int slot = 0; slot < inventory.getSize(); slot++)
		{
			RunsafeMeta itemStack = inventory.getItemInSlot(slot);

			if (itemStack == null)
				continue;

			RunsafeMeta cloneStack = itemStack.clone();
			cloneStack.setAmount(meta.getAmount());

			if (!strictItemMatch(cloneStack, meta))
				continue;

			if (itemStack.getAmount() > needed)
			{
				itemStack.setAmount(itemStack.getAmount() - needed);
				break;
			}

			needed -= itemStack.getAmount();
			inventory.removeItemInSlot(slot);

			if (needed == 0)
				break;
		}
	}

	private final boolean compareName;
	private final boolean compareDurability;
	private final boolean compareLore;
	private final boolean compareEnchants;
	private final List<RunsafeMeta> purchaseItems = new ArrayList<>(0);
	private final ConcurrentHashMap<RunsafeMeta, Integer> requiredItems = new ConcurrentHashMap<>(0);
}
