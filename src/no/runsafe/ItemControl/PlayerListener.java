package no.runsafe.ItemControl;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import no.runsafe.framework.interfaces.IConfiguration;
import no.runsafe.framework.interfaces.IOutput;
import no.runsafe.framework.interfaces.IPluginEnabled;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener, IPluginEnabled
{	
	private HashMap<String, List<Integer>> disabledItems = new HashMap<String, List<Integer>>();
	private IConfiguration config;
	private IOutput debug;

	public PlayerListener(IConfiguration config, IOutput debug)
	{
		this.config = config;
		this.debug = debug;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player thePlayer = event.getPlayer();
		World theWorld = thePlayer.getWorld();
		
		int itemID = thePlayer.getItemInHand().getTypeId();
		if(itemIsDisabled(theWorld, itemID))
			event.setCancelled(true);
	}

	@Override
	public void OnPluginEnabled()
	{
		this.loadConfig();
	}
	
	private Boolean itemIsDisabled(World world, int itemID)
	{
		if(this.disabledItems.containsKey("*") && this.disabledItems.get("*").contains(itemID))
			return true;
		
		if(this.disabledItems.containsKey(world.getName()) && this.disabledItems.get(world.getName()).contains(itemID))
			return true;
		
		return false;
	}
	
	private void loadConfig()
	{
		ConfigurationSection disabledItems = this.config.getSection("disabledItems");
		if(disabledItems == null)
			return;
		
		Set<String> keys = disabledItems.getKeys(true);
		if(keys == null)
			return;
		
		for(String key : keys)
		{
			debug.outputDebugToConsole(String.format("key: '%s'", key), Level.INFO);
			if(!this.disabledItems.containsKey(key))
				this.disabledItems.put(key, disabledItems.getIntegerList(key));
		}
	}	
}
