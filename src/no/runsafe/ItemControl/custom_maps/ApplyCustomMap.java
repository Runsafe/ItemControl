package no.runsafe.ItemControl.custom_maps;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import org.bukkit.map.MapView;

public class ApplyCustomMap extends PlayerCommand
{
	public ApplyCustomMap(IServer server)
	{
		super("applycustommap", "Apply custom map image to the item held", "runsafe.maps.apply");
		this.server = server;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		RunsafeMeta item = executor.getItemInMainHand();
		if (item == null)
			return "&cInvalid item.";

		short mapID = item.getDurability();
		MapView mapView = server.getMap(mapID);
		mapView.getRenderers().clear();
		mapView.addRenderer(new CustomRenderer());
		return "&eDone!";
	}

	private final IServer server;
}
