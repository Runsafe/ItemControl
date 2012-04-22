package no.runsafe.ItemControl;

import no.runsafe.framework.event.player.IPlayerInteractEvent;
import no.runsafe.framework.server.RunsafeWorld;
import no.runsafe.framework.server.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.server.player.RunsafePlayer;

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

        int itemID = thePlayer.getItemInHand().getItemId();
        if(globals.itemIsDisabled(theWorld, itemID))
            event.setCancelled(true);
    }
	
    private Globals globals;
}
