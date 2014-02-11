package no.runsafe.ItemControl;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.event.inventory.IPrepareCraftItem;
import no.runsafe.framework.api.item.ICustomRecipe;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.inventory.RunsafePrepareItemCraftEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeCraftingInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomRecipeHandler implements IServerReady, IPrepareCraftItem
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
	public void OnPrepareCraftItem(RunsafePrepareItemCraftEvent event)
	{
		RunsafeCraftingInventory inventory = event.getInventory();
		if (inventory.getType() == RunsafeInventoryType.WORKBENCH)
		{
			List<RunsafeMeta> items = inventory.getMatrix();
			for (ICustomRecipe recipe : recipes)
			{
				Map<Integer, RunsafeMeta> recipeDesign = recipe.getRecipe();
				int slot = 1;
				for (RunsafeMeta item : items)
				{
					if ((item == null || item.is(Item.Unavailable.Air)) && !recipeDesign.containsKey(slot))
						continue;

					if (recipeDesign.containsKey(slot) && matches(recipeDesign.get(slot), item))
					{
						inventory.setResult(recipe.getResult());
						break;
					}
					else
					{
						if (recipeDesign.containsKey(slot))
						{
							console.logInformation("Mis-match in slot %s. Expected %s got %s.", slot, recipeDesign.get(slot).getNormalName(), item == null ? "Null" : item.getNormalName());
						}
						else
						{
							console.logInformation("Mis-match in slot %s. Nothing in recipe slot.", slot);
						}
					}
					slot++;
				}
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
