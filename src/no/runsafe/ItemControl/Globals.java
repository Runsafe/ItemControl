package no.runsafe.ItemControl;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Globals implements IConfigurationChanged
{
	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		disabledItemIDs.clear();
		disabledItemNames.clear();
		disabledCraftableItems.clear();
		disabledItemIDs.putAll(config.getConfigSectionsAsIntegerList("disabledItems"));
		disabledItemNames.putAll(config.getConfigSectionsAsList("disabledItemNames"));
		disabledCraftableItems.putAll(config.getConfigSectionsAsList("disabledCraftableItems"));
		removeBlocked = config.getConfigValueAsBoolean("remove.disabledItems");

		craftDenyMessage = config.getConfigValueAsString("message.craftDeny");

		enchantContainerLevelsStoredMessage = config.getConfigValueAsString("message.enchantContainer.levelsStored");
		enchantContainerInventoryFullMessage = config.getConfigValueAsString("message.enchantContainer.inventoryFull");
		enchantContainerNoLevelsMessage = config.getConfigValueAsString("message.enchantContainer.noLevels");
		enchantContainerUsedBottleMessage = config.getConfigValueAsString("message.enchantContainer.usedBottle");

		tradersPurchaceCompleteMessage = config.getConfigValueAsString("message.traders.purchaceComplete");
		tradersLowFundsMessage = config.getConfigValueAsString("message.traders.lowFunds");

		commandsShopCreateMessage = config.getConfigValueAsString("message.commands.shopCreate");
		commandsShopCreateTagMessage = config.getConfigValueAsString("message.commands.shopCreateTag");
	}

	public static Boolean itemIsDisabled(IWorld world, RunsafeMeta item)
	{
		String worldName = world.getName();
		return ((disabledItemIDs.containsKey("*") // Check item IDs
				&& disabledItemIDs.get("*").contains(item.getItemId()))
			|| (disabledItemIDs.containsKey(worldName)
				&& disabledItemIDs.get(worldName).contains(item.getItemId())))
			|| ((disabledItemNames.containsKey("*") // Check item names
				&& disabledItemNames.get("*").contains(item.getNormalName()))
			|| (disabledItemNames.containsKey(worldName)
				&& disabledItemNames.get(worldName).contains(item.getNormalName())));
	}

	public static Boolean itemIsCraftable(IWorld world, RunsafeMeta item)
	{
		if (world == null)
			return false;

		String worldName = world.getName();
		return !((disabledCraftableItems.containsKey("*")
				&& disabledCraftableItems.get("*").contains(item.getNormalName()))
			|| (disabledCraftableItems.containsKey(worldName)
				&& disabledCraftableItems.get(worldName).contains(item.getNormalName()))
		);
	}

	public static boolean blockedItemShouldBeRemoved()
	{
		return removeBlocked;
	}

	public static String getCraftDenyMessage()
	{
		return craftDenyMessage;
	}

	public static String getEnchantContainerLevelsStoredMessage()
	{
		return enchantContainerLevelsStoredMessage;
	}

	public static String getEnchantContainerInventoryFullMessage()
	{
		return enchantContainerInventoryFullMessage;
	}

	public static String getEnchantContainerNoLevelsMessage()
	{
		return enchantContainerNoLevelsMessage;
	}

	public static String getEnchantContainerUsedBottleMessage()
	{
		return enchantContainerUsedBottleMessage;
	}

	public static String getTradersPurchaceCompleteMessage()
	{
		return tradersPurchaceCompleteMessage;
	}

	public static String getTradersLowFundsMessage()
	{
		return tradersLowFundsMessage;
	}

	public static String getCommandsShopCreateMessage()
	{
		return commandsShopCreateMessage;
	}

	public static String getCommandsShopCreateTagMessage()
	{
		return commandsShopCreateTagMessage;
	}

	private static final Map<String, List<Integer>> disabledItemIDs = new HashMap<>();
	private static final Map<String, List<String>> disabledItemNames = new HashMap<>();
	private static final Map<String, List<String>> disabledCraftableItems = new HashMap<>();
	private static String craftDenyMessage;
	private static String enchantContainerLevelsStoredMessage;
	private static String enchantContainerInventoryFullMessage;
	private static String enchantContainerNoLevelsMessage;
	private static String enchantContainerUsedBottleMessage;
	private static String tradersPurchaceCompleteMessage;
	private static String tradersLowFundsMessage;
	private static String commandsShopCreateMessage;
	private static String commandsShopCreateTagMessage;
	private static boolean removeBlocked;
}
