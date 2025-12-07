package no.runsafe.ItemControl.trading;

import javax.annotation.Nullable;

public class PurchaseData
{
	public PurchaseData(@Nullable String tag,
		boolean compareName, boolean compareDurability, boolean compareLore, boolean compareEnchants, boolean shouldPrintTagID
	)
	{
		this.tag = tag;
		this.compareName = compareName;
		this.compareDurability = compareDurability;
		this.compareLore = compareLore;
		this.compareEnchants = compareEnchants;
		this.shouldPrintTagID = shouldPrintTagID;
	}

	public String getTag()
	{
		return tag;
	}

	public boolean shouldCompareName()
	{
		return compareName;
	}

	public boolean shouldCompareDurability()
	{
		return compareDurability;
	}

	public boolean shouldCompareLore()
	{
		return compareLore;
	}

	public boolean shouldCompareEnchants()
	{
		return compareEnchants;
	}

	public boolean shouldPrintTagID()
	{
		return shouldPrintTagID;
	}

	private final String tag;
	private final boolean compareName;
	private final boolean compareDurability;
	private final boolean compareLore;
	private final boolean compareEnchants;
	private final boolean shouldPrintTagID;
}
