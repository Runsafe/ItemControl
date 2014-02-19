package no.runsafe.ItemControl.trading;

import net.minecraft.server.v1_7_R1.DamageSource;
import net.minecraft.server.v1_7_R1.EntityAgeable;
import net.minecraft.server.v1_7_R1.EntityHuman;
import net.minecraft.server.v1_7_R1.EntityVillager;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;

public class Trader extends EntityVillager
{
	public Trader(ILocation location, RunsafeInventory inventory)
	{
		super(ObjectUnwrapper.getMinecraft(location.getWorld()));
		setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		world.addEntity(this);
		this.inventory = inventory;
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entityAgeable)
	{
		return null; // 100% effective birth-control.
	}

	@Override
	public boolean a(EntityHuman human)
	{
		return false;
	}

	@Override
	public boolean damageEntity(DamageSource damagesource, float f)
	{
		return false; // We don't want these taking damage.
	}

	private final RunsafeInventory inventory;
}
