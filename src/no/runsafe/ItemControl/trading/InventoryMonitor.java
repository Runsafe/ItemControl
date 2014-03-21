package no.runsafe.ItemControl.trading;

import net.minecraft.server.v1_7_R1.*;
import net.minecraft.server.v1_7_R1.ItemStack;
import no.runsafe.ItemControl.ItemControl;
import no.runsafe.framework.api.event.inventory.IInventoryClick;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.enchantment.RunsafeEnchantment;
import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.framework.tools.reflection.ReflectionHelper;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;
import no.runsafe.framework.minecraft.Item;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InventoryMonitor implements IInventoryClick
{
	@Override
	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
	{
		RunsafeInventory inventory = event.getInventory();
		if (inventory.getType() == RunsafeInventoryType.MERCHANT && event.getSlot() == 2)
		{
			RunsafeInventory top = event.getView().getTopInventory();
			InventoryMerchant raw = (InventoryMerchant) ReflectionHelper.getObjectField(top.getRaw(), "inventory");
			int currentTrade = (Integer) ReflectionHelper.getObjectField(raw, "e");

			EntityVillager merchant = (EntityVillager) ReflectionHelper.getObjectField(raw, "merchant");
			if (merchant != null)
			{
				boolean cancel = false;
				RunsafeMeta firstSlot = inventory.getItemInSlot(0);
				RunsafeMeta secondSlot = inventory.getItemInSlot(1);

				MerchantRecipeList list = merchant.getOffers(null);

				if (list.size() > currentTrade)
				{
					MerchantRecipe recipe = (MerchantRecipe) list.get(currentTrade);
					RunsafeMeta firstItem = convertFromMinecraft(recipe.getBuyItem1());
					RunsafeMeta secondItem = convertFromMinecraft(recipe.getBuyItem2());

					if (firstItem != null)
						if (firstSlot == null || (!firstItem.is(Item.Special.Crafted.WrittenBook) && !strictMatch(firstSlot,  firstItem)))
							cancel = true;

					if (secondItem != null)
						if (secondSlot == null || (!secondItem.is(Item.Special.Crafted.WrittenBook) && !strictMatch(secondSlot, secondItem)))
							cancel = true;
				}

				if (cancel)
				{
					IPlayer player = event.getWhoClicked();
					player.sendColouredMessage("&cYou cannot trade with invalid items!");
					player.closeInventory();
					event.cancel();
				}
			}
		}
	}

	private RunsafeMeta convertFromMinecraft(ItemStack raw)
	{
		return raw == null ? null : new RunsafeMeta(CraftItemStack.asBukkitCopy(raw));
	}

	private boolean strictMatch(RunsafeMeta first, RunsafeMeta second)
	{
		if (!first.is(second.getItemType()))
		{
			ItemControl.Debugger.debugFine("Item type mis-match");
			return false;
		}

		if (first.getDurability() != second.getDurability())
		{
			ItemControl.Debugger.debugFine("Durability mis-match!");
			return false;
		}

		String firstName = first.getDisplayName(); // null
		String secondName = second.getDisplayName(); // null

		if (firstName == null)
			firstName = "";

		if (secondName == null)
			secondName = "";

		if (!firstName.equals(secondName))
		{
			ItemControl.Debugger.debugFine("Name mis-match");
			return false;
		}

		List<String> firstLore = first.getLore();
		List<String> secondLore = second.getLore();

		if (firstLore == null)
			firstLore = Collections.emptyList();

		if (secondLore == null)
			secondLore = Collections.emptyList();

		if (firstLore.size() != secondLore.size())
		{
			ItemControl.Debugger.debugFine("Lore size mis-match");
			return false;
		}

		int index = 0;
		for (String firstLoreString : firstLore)
		{
			if (!firstLoreString.equals(secondLore.get(index)))
			{
				ItemControl.Debugger.debugFine("Lore index mis-match: " + index);
				return false;
			}

			index++;
		}

		Map<RunsafeEnchantment, Integer> firstEnchants = first.getEnchantments();
		Map<RunsafeEnchantment, Integer> secondEnchants = second.getEnchantments();

		if (firstEnchants.size() != secondEnchants.size())
		{
			ItemControl.Debugger.debugFine("Enchant size mis-match");
			return false;
		}

		for (Map.Entry<RunsafeEnchantment, Integer> firstEnchant : firstEnchants.entrySet())
		{
			RunsafeEnchantment enchantment = firstEnchant.getKey();
			if (!second.containsEnchantment(enchantment))
			{
				ItemControl.Debugger.debugFine("Enchant type mis-match");
				return false;
			}

			if (!firstEnchant.getValue().equals(second.getEnchantLevel(enchantment)))
			{
				ItemControl.Debugger.debugFine("Enchant value mis-match");
				return false;
			}
		}

		return true;
	}
}
