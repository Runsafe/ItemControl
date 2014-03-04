package no.runsafe.ItemControl.custom_maps;

import no.runsafe.framework.api.event.IMapInitializeEvent;
import org.bukkit.map.MapView;

public class MapHandler implements IMapInitializeEvent
{
	@Override
	public void OnMapInitialize(MapView mapView)
	{
		mapView.getRenderers().clear();
		mapView.addRenderer(new CustomRenderer());
	}
}
