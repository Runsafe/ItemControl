package no.runsafe.ItemControl;

import no.runsafe.framework.event.player.IPlayerInteractEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Map;

public class PlayerListener implements IPlayerInteractEvent
{
	public PlayerListener(Globals globals, IOutput output)
	{
		this.globals = globals;
		this.console = output;
	}

	@Override
	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		RunsafePlayer thePlayer = event.getPlayer();
		RunsafeWorld theWorld = thePlayer.getWorld();

		RunsafeItemStack item = thePlayer.getItemInHand();
		if (event.getBlock() != null
			&& item.getItemId() == Material.MONSTER_EGG.getId()
			&& !globals.itemIsDisabled(theWorld, Material.MOB_SPAWNER.getId())
			&& this.globals.blockShouldDrop(thePlayer.getWorld(), Material.MOB_SPAWNER.getId()))
		{
			dump(item.getRaw());
			if (globals.createSpawner(event.getPlayer(), event.getBlock().getWorld(), event.getTargetBlock(), item))
			{
				if (item.getAmount() > 1)
					item.remove(1);
				else
					event.removeItemStack();
			}
			event.setCancelled(true);
			return;
		}
		if (globals.itemIsDisabled(theWorld, item.getItemId()))
			event.setCancelled(true);
	}

	private void dump(ConfigurationSerializable raw)
	{
		console.fine(String.format("Dumping instance of %s", raw.getClass().getCanonicalName()));
		Map<String, Object> values = raw.serialize();
		for (String key : values.keySet())
			console.fine(String.format(" - %s: %s", key, values.get(key)));
	}

	private final Globals globals;
	private final IOutput console;
}
