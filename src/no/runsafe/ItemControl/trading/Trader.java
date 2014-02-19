package no.runsafe.ItemControl.trading;

import net.minecraft.server.v1_7_R1.*;
import no.runsafe.ItemControl.trading.commands.EditTrader;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import org.bukkit.craftbukkit.v1_7_R1.inventory.CraftInventory;

public class Trader extends EntityVillager
{
	public Trader(ILocation location, RunsafeInventory inventory, IScheduler scheduler, TradingHandler handler)
	{
		super(ObjectUnwrapper.getMinecraft(location.getWorld()));
		setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		world.addEntity(this);
		this.inventory = inventory;
		this.location = location;
		this.scheduler = scheduler;
		this.handler = handler;
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entityAgeable)
	{
		return null; // 100% effective birth-control.
	}

	@Override
	public boolean a(EntityHuman human)
	{
		final String playerName = human.getName();
		if (EditTrader.playerIsTracked(playerName))
		{
			human.openContainer(((CraftInventory) inventory.getRaw()).getInventory()); // Open the inventory.
			runningTimer = scheduler.startSyncRepeatingTask(new Runnable()
			{
				@Override
				public void run()
				{
					if (inventory.getViewers().size() == 0)
					{
						handler.saveTraderData(new TraderData(location, inventory, getCustomName()));
						cancelTimer();
						EditTrader.stopTrackingPlayer(playerName);
					}
				}
			}, 5, 5);

			return false;
		}
		else
		{
			getList().clear(); // Wipe all existing trades.

			for (int col = 0; col < 10; col++)
			{
				RunsafeMeta firstItem = inventory.getItemInSlot(col);
				if (firstItem == null)
					continue;

				RunsafeMeta result = inventory.getItemInSlot(col + 18);
				if (result == null)
					continue;

				RunsafeMeta secondItem = inventory.getItemInSlot(col + 9);
				if (secondItem != null)
					addTrade(firstItem, secondItem, result);
				else
					addTrade(firstItem, result);
			}

			a_(human);
			human.openTrade(this, getCustomName());
			return true;
		}
	}

	private void cancelTimer()
	{
		scheduler.cancelTask(runningTimer);
	}

	private void addTrade(RunsafeMeta firstItem, RunsafeMeta result)
	{
		addRecipe(new MerchantRecipe(unwrapStack(firstItem), unwrapStack(result)));
	}

	private void addTrade(RunsafeMeta firstItem, RunsafeMeta secondItem, RunsafeMeta result)
	{
		addRecipe(new MerchantRecipe(unwrapStack(firstItem), unwrapStack(secondItem), unwrapStack(result)));
	}

	private ItemStack unwrapStack(RunsafeMeta itemStack)
	{
		return ObjectUnwrapper.getMinecraft(itemStack);
	}

	private void addRecipe(MerchantRecipe recipe)
	{
		recipe.a(9999999); // Prevent the trade from expiring.
		getList().a(recipe); // Add the recipe to the merchant.
	}

	private MerchantRecipeList getList()
	{
		return getOffers(null);
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f)
	{
		return false; // We don't want these taking damage.
	}

	@Override
	protected void bp()
	{
		// Do nothing.
	}

	@Override
	public void move(double d0, double d1, double d2)
	{
		// Do nothing, we don't want this entity to move.
	}

	private final RunsafeInventory inventory;
	private final ILocation location;
	private final IScheduler scheduler;
	private int runningTimer;
	private final TradingHandler handler;
}
