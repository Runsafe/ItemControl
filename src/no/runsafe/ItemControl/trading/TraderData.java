package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;

public class TraderData
{
	public TraderData(ILocation location, RunsafeInventory inventory, String name)
	{
		this.location = location;
		this.inventory = inventory;
		this.name = name;
	}

	public ILocation getLocation()
	{
		return location;
	}

	public RunsafeInventory getInventory()
	{
		return inventory;
	}

	public String getName()
	{
		return name;
	}

	private final ILocation location;
	private final RunsafeInventory inventory;
	private final String name;
}
