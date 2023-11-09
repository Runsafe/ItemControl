package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import javax.annotation.Nullable;

public class TraderData
{
	public TraderData(ILocation location, RunsafeInventory inventory, @Nullable String tag,
		boolean compareName, boolean compareDurability, boolean compareLore, boolean compareEnchants
	)
	{
		this.location = location;
		this.inventory = inventory;
		this.tag = tag;
		this.compareName = compareName;
		this.compareDurability = compareDurability;
		this.compareLore = compareLore;
		this.compareEnchants = compareEnchants;
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

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public boolean isSaved()
	{
		return isSaved;
	}

	public void setSaved(boolean saved)
	{
		isSaved = saved;
	}

	public boolean shouldCompareName()
	{
		return compareName;
	}

	public void setCompareName(boolean value)
	{
		compareName = value;
	}

	public boolean shouldCompareDurability()
	{
		return compareDurability;
	}

	public void setCompareDurability(boolean value)
	{
		compareDurability = value;
	}

	public boolean shouldCompareLore()
	{
		return compareLore;
	}

	public void setCompareLore(boolean value)
	{
		compareLore = value;
	}

	public boolean shouldCompareEnchants()
	{
		return compareEnchants;
	}

	public void setCompareEnchants(boolean value)
	{
		compareEnchants = value;
	}

	public void refresh()
	{
		purchaseValidator = new PurchaseValidator(compareName, compareDurability, compareLore, compareEnchants);

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

	private String tag;
	private boolean compareName;
	private boolean compareDurability;
	private boolean compareLore;
	private boolean compareEnchants;
	private final ILocation location;
	private final RunsafeInventory inventory;
	private boolean isSaved = true;
	private PurchaseValidator purchaseValidator;
}
