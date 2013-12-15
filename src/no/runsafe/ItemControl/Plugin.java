package no.runsafe.ItemControl;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.features.Events;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		addComponent(Events.class);
		this.addComponent(Globals.class);
		this.addComponent(PlayerListener.class);
		this.addComponent(BlockListener.class);
		this.addComponent(EntityListener.class);
	}
}
