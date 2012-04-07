package no.runsafe.ItemControl;

import java.util.HashMap;

import no.runsafe.framework.interfaces.IConfiguration;
import no.runsafe.framework.interfaces.IPluginEnabled;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener, IPluginEnabled
{
	
	private HashMap<Integer, String> disabledItems = new HashMap<Integer, String>();
	private IConfiguration config;

	public PlayerListener(IConfiguration config)
	{
		this.config = config;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player thePlayer = event.getPlayer();
		World theWorld = thePlayer.getWorld();
		
		int itemID = thePlayer.getItemInHand().getTypeId();
		String itemWorld = this.disabledItems.get(itemID);
		
		if (itemWorld != null && (itemWorld.equals(theWorld.getName()) || itemWorld.equals("all")))
		{
			event.setCancelled(true);
		}
		
	}

	@Override
	public void OnPluginEnabled()
	{
		this.loadConfig();
	}
	
	private void loadConfig()
	{
		String disabledItems = this.config.getConfigValueAsString("disabledItems");
		String[] itemList = disabledItems.split(",");
		
		for (int i = 0; i < itemList.length; i++)
		{
			String[] itemSplit = itemList[i].split("@");
			this.disabledItems.put(Integer.parseInt(itemSplit[0]), itemSplit[1]);
		}
	}
	
}
