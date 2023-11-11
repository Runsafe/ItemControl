package no.runsafe.ItemControl.trading;

import javax.annotation.Nullable;

public class PurchaseData
{
	public PurchaseData(@Nullable String tag,
		boolean compareName, boolean compareDurability, boolean compareLore, boolean compareEnchants)
	{
		this.tag = tag;
		this.compareName = compareName;
		this.compareDurability = compareDurability;
		this.compareLore = compareLore;
		this.compareEnchants = compareEnchants;
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

	private final String tag;
	private final boolean compareName;
	private final boolean compareDurability;
	private final boolean compareLore;
	private final boolean compareEnchants;
}
