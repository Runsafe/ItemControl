package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;

public class TraderData
{
	public TraderData(ILocation location, RunsafeInventory inventory)
	{
		this.location = location;
		this.inventory = inventory;
	}

	public ILocation getLocation()
	{
		return location;
	}

	public RunsafeInventory getInventory()
	{
		return inventory;
	}

	public boolean isSaved()
	{
		return isSaved;
	}

	public void setSaved(boolean saved)
	{
		isSaved = saved;
	}

	private final ILocation location;
	private final RunsafeInventory inventory;
	private boolean isSaved = true;
}
