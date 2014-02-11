package no.runsafe.ItemControl;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.event.inventory.IInventoryClick;
import no.runsafe.framework.api.item.ICustomRecipe;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeCraftingInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
import no.runsafe.framework.minecraft.inventory.RunsafeInventoryView;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRecipeHandler implements IServerReady, IInventoryClick
{
	public CustomRecipeHandler(IConsole console)
	{
		this.console = console;
	}

	@Override
	public void OnServerReady()
	{
		recipes.addAll(RunsafePlugin.getPluginAPI(ICustomRecipe.class));
		console.logInformation("Loaded " + recipes.size() + " custom recipes");
	}

	@Override
	public void OnInventoryClickEvent(RunsafeInventoryClickEvent event)
	{
		RunsafeInventory inventory = event.getInventory();
		if (inventory.getType() == RunsafeInventoryType.WORKBENCH)
		{
			HashMap<Integer, RunsafeMeta> items = new HashMap<Integer, RunsafeMeta>(0);
			for (int i = 1; i < inventory.getSize(); i++)
			{
				RunsafeMeta slotItem = inventory.getItemInSlot(i);
				if (slotItem != null)
					items.put(i, slotItem);
			}

			RunsafeMeta cursorItem = event.getCurrentItem();
			if (cursorItem != null && !cursorItem.is(Item.Unavailable.Air))
				items.put(event.getSlot(), cursorItem);

			checkRecipes(items, (RunsafeCraftingInventory) inventory, event.getView());
		}
	}

	private void checkRecipes(HashMap<Integer, RunsafeMeta> workbench, RunsafeCraftingInventory inventory, RunsafeInventoryView view)
	{
		for (ICustomRecipe recipe : recipes)
		{
			boolean failed = false;
			for (Map.Entry<Integer, RunsafeMeta> node : recipe.getRecipe().entrySet())
			{
				RunsafeMeta workbenchItem = workbench.get(node.getKey());
				if (workbenchItem == null || !matches(workbenchItem, node.getValue()))
				{
					console.logInformation("Invalid match in slot %s. Expected %s got %s", node.getKey(), node.getValue().getNormalName(), workbenchItem == null ? "Null" : workbenchItem.getNormalName());
					failed = true;
					break;
				}
			}

			if (!failed)
			{
				//inventory.setItemInSlot(recipe.getResult(), 0);
				//view.setItem(0, recipe.getResult());
				inventory.setResult(recipe.getResult());
				break;
			}
		}
	}

	private boolean matches(RunsafeMeta item, RunsafeMeta check)
	{
		if (!item.is(check.getItemType()))
			return false;

		String displayName = item.getDisplayName();
		String checkName = check.getDisplayName();

		if (displayName == null)
		{
			if (checkName != null)
				return false;
		}
		else
		{
			if (checkName == null || !displayName.equals(checkName))
				return false;
		}

		List<String> lore = item.getLore();
		List<String> checkLore = check.getLore();

		if (lore == null || lore.isEmpty())
		{
			if (checkLore != null && !checkLore.isEmpty())
				return false;
		}
		else
		{
			if (checkLore == null || checkLore.isEmpty())
				return false;

			if (checkLore.size() != lore.size())
				return false;

			int index = 0;
			for (String loreString : lore)
			{
				if (!checkLore.get(index).equals(loreString))
					return false;

				index++;
			}
		}
		return true;
	}

	private final IConsole console;
	private final List<ICustomRecipe> recipes = new ArrayList<ICustomRecipe>(0);
}
