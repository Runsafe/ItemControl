package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import javax.annotation.Nullable;

public class TraderData
{
	public TraderData(ILocation location, RunsafeInventory inventory, @Nullable String tag)
	{
		this.location = location;
		this.inventory = inventory;
		this.tag = tag;
		refresh();
	}

	public ILocation getLocation()
	{
		return location;
	}

	public RunsafeInventory getInventory()
	{
		return inventory;
	}

	public String getTag()
	{
		return tag;
	}

	public boolean isSaved()
	{
		return isSaved;
	}

	public void setSaved(boolean saved)
	{
		isSaved = saved;
	}

	public void refresh()
	{
		purchaseValidator = new PurchaseValidator();

		for (int i = 0; i < 9; i++)
		{
			RunsafeMeta item = inventory.getItemInSlot(i);

			if (item != null)
				purchaseValidator.addRequiredItem(item);
		}

		for (int i = 18; i < 27; i++)
		{
			RunsafeMeta item = inventory.getItemInSlot(i);

			if (item != null)
				purchaseValidator.addPurchaseItem(item);
		}
	}

	public PurchaseValidator getPurchaseValidator()
	{
		return purchaseValidator;
	}

	private final String tag;
	private final ILocation location;
	private final RunsafeInventory inventory;
	private boolean isSaved = true;
	private PurchaseValidator purchaseValidator;
}
