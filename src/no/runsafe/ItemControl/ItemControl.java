package no.runsafe.ItemControl;

import java.io.InputStream;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.configuration.IConfigurationDefaults;
import no.runsafe.framework.configuration.IConfigurationFile;

public class ItemControl extends RunsafePlugin implements IConfigurationFile, IConfigurationDefaults
{

	@Override
	protected void PluginSetup()
	{
		this.addComponent(PlayerListener.class);
		this.addComponent(BlockListener.class);
	}
	
	@Override
	public InputStream getDefaultConfiguration()
	{
		return getResource(Constants.defaultConfigurationFile);
	}
	
	@Override
	public String getConfigurationPath() 
	{
		return Constants.configurationFile;
	}
	
}
