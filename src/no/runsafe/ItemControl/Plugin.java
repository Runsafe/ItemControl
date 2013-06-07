package no.runsafe.ItemControl;

import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.configuration.IConfigurationFile;
import no.runsafe.worldguardbridge.WorldGuardInterface;

public class Plugin extends RunsafeConfigurablePlugin implements IConfigurationFile
{
	@Override
	protected void PluginSetup()
	{
		addComponent(Instances.get("RunsafeWorldGuardBridge").getComponent(WorldGuardInterface.class));
		this.addComponent(Globals.class);
		this.addComponent(PlayerListener.class);
		this.addComponent(BlockListener.class);
		this.addComponent(EntityListener.class);
	}
}
