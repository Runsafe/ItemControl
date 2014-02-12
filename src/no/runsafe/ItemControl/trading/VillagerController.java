package no.runsafe.ItemControl.trading;

import net.minecraft.server.v1_7_R1.EntityVillager;
import net.minecraft.server.v1_7_R1.ItemStack;
import net.minecraft.server.v1_7_R1.MerchantRecipe;
import net.minecraft.server.v1_7_R1.MerchantRecipeList;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.framework.tools.reflection.ReflectionHelper;
import org.bukkit.craftbukkit.v1_7_R1.entity.CraftVillager;

import java.lang.reflect.Field;

public class VillagerController
{
	public VillagerController(RunsafeEntity entity)
	{
		villager = ((CraftVillager) entity.getRaw()).getHandle();
	}

	private MerchantRecipeList getList()
	{
		return villager.getOffers(null);
	}

	public void clearTrades()
	{
		getList().clear();
	}

	public void addTrade(RunsafeMeta item, RunsafeMeta result)
	{
		MerchantRecipe recipe = new MerchantRecipe(toMinecraft(item), toMinecraft(result));
		addRecipe(recipe);
	}

	public void addTrade(RunsafeMeta item, RunsafeMeta item2, RunsafeMeta result)
	{
		MerchantRecipe recipe = new MerchantRecipe(toMinecraft(item), toMinecraft(item2), toMinecraft(result));
		addRecipe(recipe);
	}

	private void addRecipe(MerchantRecipe recipe)
	{
		recipe.a(999999999); // make it so the trade "never" runs out of uses
		getList().add(recipe);
	}

	public void setOpenTrades(float number)
	{
		ReflectionHelper.setField(villager, "bA", number);
	}

	private ItemStack toMinecraft(RunsafeMeta item)
	{
		return ObjectUnwrapper.getMinecraft(item);
	}

	private final EntityVillager villager;
}
