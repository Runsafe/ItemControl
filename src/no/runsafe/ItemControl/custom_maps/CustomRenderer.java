package no.runsafe.ItemControl.custom_maps;

import no.runsafe.ItemControl.ItemControl;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CustomRenderer extends MapRenderer
{
	@Override
	public void render(MapView mapView, MapCanvas mapCanvas, Player player)
	{
		try
		{
			BufferedImage image = ImageIO.read(ItemControl.customMapFile);
			mapCanvas.drawImage(0, 0, image);
		}
		catch (IOException e)
		{
			// Nope, sorry, nothing.
		}
	}
}
