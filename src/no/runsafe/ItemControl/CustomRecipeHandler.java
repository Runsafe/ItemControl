package no.runsafe.ItemControl;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.item.ICustomRecipe;
import no.runsafe.framework.api.log.IConsole;

import java.util.ArrayList;
import java.util.List;

public class CustomRecipeHandler implements IServerReady
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

	private final IConsole console;
	private final List<ICustomRecipe> recipes = new ArrayList<ICustomRecipe>(0);
}
