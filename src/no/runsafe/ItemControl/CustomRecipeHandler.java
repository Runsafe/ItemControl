package no.runsafe.ItemControl;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.event.inventory.IInventoryClick;
import no.runsafe.framework.api.item.ICustomRecipe;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.event.inventory.RunsafeInventoryClickEvent;
import no.runsafe.framework.minecraft.inventory.RunsafeCraftingInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.inventory.RunsafeInventoryType;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.*;

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
		RunsafeInventory rawInventory = event.getInventory();
		if (rawInventory.getType() == RunsafeInventoryType.WORKBENCH)
		{
			RunsafeCraftingInventory inventory = (RunsafeCraftingInventory) rawInventory;
			List<RunsafeMeta> items = inventory.getMatrix();
			for (ICustomRecipe recipe : recipes)
			{
				boolean failed = false;
				Map<Integer, RunsafeMeta> recipeDesign = recipe.getRecipe();
				int slot = 1;
				for (RunsafeMeta item : items)
				{
					if ((item == null || item.is(Item.Unavailable.Air)) && !recipeDesign.containsKey(slot))
						continue;

					if (!recipeDesign.containsKey(slot) || !matches(recipeDesign.get(slot), item))
					{
						failed = true;
						break;
					}
					slot++;
				}

				if (!failed)
					event.getView().setItem(0, recipe.getResult());
			}
		}
	}

	private boolean matches(RunsafeMeta item, RunsafeMeta check)
	{
		if (!item.getItemType().equals(check.getItemType()))
		{
			console.logInformation("Mis-matched item?");
			return false;
		}

		String displayName = item.getDisplayName();
		String checkName = check.getDisplayName();

		if (displayName == null)
		{
			if (checkName != null)
			{
				console.logInformation("Name mis-match");
				return false;
			}
		}
		else
		{
			if (checkName == null || !displayName.equals(checkName))
			{
				console.logInformation("Name mis-match 2");
				return false;
			}
		}

		List<String> lore = item.getLore();
		List<String> checkLore = check.getLore();

		if (lore == null || lore.isEmpty())
		{
			if (checkLore != null && !checkLore.isEmpty())
			{
				console.logInformation("Lore mis-match.");
				return false;
			}
		}
		else
		{
			if (checkLore == null || checkLore.isEmpty())
			{
				console.logInformation("Lore mis-match 2");
				return false;
			}

			if (checkLore.size() != lore.size())
			{
				console.logInformation("Lore size mis-match");
				return false;
			}

			int index = 0;
			for (String loreString : lore)
			{
				if (!checkLore.get(index).equals(loreString))
				{
					console.logInformation("Lore mis-match: %s / %s", loreString, checkLore.get(index));
					return false;
				}

				index++;
			}
		}
		return true;
	}

	private final IConsole console;
	private final List<ICustomRecipe> recipes = new ArrayList<ICustomRecipe>(0);
}
