package no.runsafe.ItemControl;

import java.io.InputStream;

import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.interfaces.IConfigurationDefaults;
import no.runsafe.framework.interfaces.IConfigurationFile;

public class ItemControl extends RunsafePlugin implements IConfigurationFile, IConfigurationDefaults {

	public ItemControl()
	{
		super();
	}
	
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
