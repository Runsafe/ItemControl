package no.runsafe.ItemControl;

import no.runsafe.framework.event.player.IPlayerInteractEvent;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.server.item.RunsafeItemStack;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.bukkit.Material;

public class PlayerListener implements IPlayerInteractEvent
{
	public PlayerListener(Globals globals)
	{
		this.globals = globals;
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
			if (globals.createSpawner(event.getBlock().getWorld(), event.getTargetBlock(), item))
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

	private final Globals globals;
}
