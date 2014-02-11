package no.runsafe.ItemControl;

import no.runsafe.framework.api.event.player.IPlayerJoinEvent;
import no.runsafe.framework.api.item.ICustomRecipe;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerJoinEvent;

public class CustomRecipeHandler implements IPlayerJoinEvent
{
	public CustomRecipeHandler(ICustomRecipe[] recipes, IConsole console)
	{
		this.recipes = recipes;
		this.console = console;
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		for (ICustomRecipe recipe : recipes)
			console.logInformation(recipe.getResult().getNormalName());
	}

	private final IConsole console;
	private final ICustomRecipe[] recipes;
}
