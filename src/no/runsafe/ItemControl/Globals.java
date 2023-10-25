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
		disabledItemIDs.putAll(config.getConfigSectionsAsIntegerList("disabledItems"));
		disabledItemNames.putAll(config.getConfigSectionsAsList("disabledItemNames"));
		removeBlocked = config.getConfigValueAsBoolean("remove.disabledItems");
	}

	public Boolean itemIsDisabled(IWorld world, RunsafeMeta item)
	{
		String worldName = world.getName();
		return ((disabledItemIDs.containsKey("*") && disabledItemIDs.get("*").contains(item.getItemId())) // Check item IDs
			|| (disabledItemIDs.containsKey(worldName) && disabledItemIDs.get(worldName).contains(item.getItemId())))
			|| ((disabledItemNames.containsKey("*") && disabledItemNames.get("*").contains(item.getNormalName())) // Check item names
			|| (disabledItemNames.containsKey(worldName) && disabledItemNames.get(worldName).contains(item.getNormalName())));
	}

	public boolean blockedItemShouldBeRemoved()
	{
		return removeBlocked;
	}


	private final Map<String, List<Integer>> disabledItemIDs = new HashMap<>();
	private final Map<String, List<String>> disabledItemNames = new HashMap<>();
	private boolean removeBlocked;
}
