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
		disabledItems.clear();
		disabledItems.putAll(config.getConfigSectionsAsIntegerList("disabledItems"));
		removeBlocked = config.getConfigValueAsBoolean("remove.disabledItems");
	}

	public Boolean itemIsDisabled(IWorld world, RunsafeMeta item)
	{
		return (disabledItems.containsKey("*") && disabledItems.get("*").contains(item.getItemId()))
			|| (disabledItems.containsKey(world.getName()) && disabledItems.get(world.getName()).contains(item.getItemId()));
	}

	public boolean blockedItemShouldBeRemoved()
	{
		return removeBlocked;
	}


	private final Map<String, List<Integer>> disabledItems = new HashMap<String, List<Integer>>();
	private boolean removeBlocked;
}
