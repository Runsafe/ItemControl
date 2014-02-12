package no.runsafe.ItemControl.trading;

import net.minecraft.server.v1_7_R1.*;
import net.minecraft.server.v1_7_R1.ItemStack;
import no.runsafe.framework.api.event.inventory.IInventoryClick;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.enchantment.RunsafeEnchantment;
import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.framework.tools.reflection.ReflectionHelper;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InventoryMonitor implements IInventoryClick
{
	@Override
	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
	{
		RunsafeInventory inventory = event.getInventory();
		if (inventory.getType() == RunsafeInventoryType.MERCHANT)
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

					if (firstSlot != null && firstItem != null && !strictMatch(firstSlot, firstItem))
						cancel = true;

					if (secondSlot != null && secondItem != null && !strictMatch(secondSlot, secondItem))
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
			return false;

		String firstName = first.getDisplayName();
		String secondName = second.getDisplayName();

		if ((firstName == null && secondName != null) || (secondName == null || !firstName.equals(secondName)))
			return false;

		List<String> firstLore = first.getLore();
		List<String> secondLore = second.getLore();

		if (firstLore == null)
			firstLore = Collections.emptyList();

		if (secondLore == null)
			secondLore = Collections.emptyList();

		if (firstLore.size() != secondLore.size())
			return false;

		int index = 0;
		for (String firstLoreString : firstLore)
		{
			if (!firstLoreString.equals(secondLore.get(index)))
				return false;

			index++;
		}

		Map<RunsafeEnchantment, Integer> firstEnchants = first.getEnchantments();
		Map<RunsafeEnchantment, Integer> secondEnchants = second.getEnchantments();

		if (firstEnchants.size() != secondEnchants.size())
			return false;

		for (Map.Entry<RunsafeEnchantment, Integer> firstEnchant : firstEnchants.entrySet())
		{
			RunsafeEnchantment enchantment = firstEnchant.getKey();
			if (!secondEnchants.containsKey(enchantment))
				return false;

			if (!secondEnchants.get(enchantment).equals(firstEnchant.getValue()))
				return false;
		}

		return true;
	}
}
