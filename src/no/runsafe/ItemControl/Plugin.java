package no.runsafe.ItemControl;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.features.Events;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void pluginSetup()
	{
		addComponent(Events.class);
		addComponent(Globals.class);
		addComponent(PlayerListener.class);
		addComponent(BlockListener.class);
		addComponent(EntityListener.class);
	}
}
