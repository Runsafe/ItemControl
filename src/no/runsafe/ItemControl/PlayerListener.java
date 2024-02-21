package no.runsafe.ItemControl;

import no.runsafe.ItemControl.spawners.SpawnerHandler;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.inventory.ICraftItem;
import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
import no.runsafe.framework.api.event.player.IPlayerInteractEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.inventory.RunsafeCraftItemEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerInteractEvent;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
import no.runsafe.framework.minecraft.item.meta.RunsafeSpawnEgg;
import no.runsafe.worldguardbridge.IRegionControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerListener implements IPlayerInteractEvent, IPlayerDeathEvent, ICraftItem, IConfigurationChanged
{
	public PlayerListener(IRegionControl worldGuardInterface, SpawnerHandler spawnerHandler)
	{
		this.worldGuardInterface = worldGuardInterface;
		this.spawnerHandler = spawnerHandler;
	}

	@Override
	public void OnPlayerInteractEvent(RunsafePlayerInteractEvent event)
	{
		if (event.isNotRightClick())
			return;

		IPlayer player = event.getPlayer();
		IWorld world = player.getWorld();
		RunsafeMeta usingItem = player.getItemInMainHand();

		if (usingItem == null || world == null)
			return;

		if (Globals.itemIsDisabled(world, usingItem))
		{
			if (Globals.blockedItemShouldBeRemoved())
				player.removeItem(usingItem.getItemType());

			event.cancel();
		}

		IBlock targetBlock = event.getBlock();
		if (player.cannotBuild() || targetBlock == null)
			return;

		if (!(usingItem instanceof RunsafeSpawnEgg) || spawnerHandler.spawnerIsNotHarvestable(world))
			return;

		// If the block has an interface or is interact block, don't let them place a spawner
		if (event.isCancelled() || targetBlock.hasInterface() || targetBlock.isInteractBlock())
			return;

		if (spawnerHandler.createSpawner(player, event.getTargetBlock(), (RunsafeSpawnEgg) usingItem))
			player.removeItem(usingItem.getItemType(), 1);

		event.cancel();
	}

	@Override
	public void OnCraftItem(RunsafeCraftItemEvent event)
	{
		IPlayer crafter = event.getWhoClicked();
		if (crafter == null)
		{
			ItemControl.Debugger.debugFine("Invalid player attempted to craft item.");
			return;
		}
		ItemControl.Debugger.debugFine("player: %s attempting to craft item: %s",
			crafter.getName(), event.getRecipe().getResult().getNormalName());

		if (Globals.itemIsCraftable(crafter.getWorld(), event.getRecipe().getResult().getItem()))
			return;

		event.cancel();
		crafter.sendColouredMessage(Globals.getCraftDenyMessage());
	}

	@Override
	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
	{
		IPlayer player = event.getEntity();
		String currentWorld = player.getWorldName();
		boolean stopItems = false;

		if (!noDeathItemsWorlds.contains(currentWorld))
		{
			List<String> regions = worldGuardInterface.getApplicableRegions(player);

			if (regions != null && noDeathItemsRegions.containsKey(currentWorld))
				for (String region : regions)
					if (noDeathItemsRegions.get(currentWorld).contains(region))
						stopItems = true;
		}
		else
		{
			stopItems = true;
		}

		if (!stopItems)
			return;

		event.setDrops(new ArrayList<>());
		event.setDroppedXP(0);
		event.setNewLevelAmount(0);
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		noDeathItemsRegions.clear();
		noDeathItemsWorlds.clear();

		List<String> nodes = configuration.getConfigValueAsList("preventDeathItemDrops");

		for (String node : nodes)
		{
			if (!node.contains("."))
			{
				noDeathItemsWorlds.add(node);
				continue;
			}

			String[] parts = node.split("\\.");
			if (!noDeathItemsRegions.containsKey(parts[0]))
				noDeathItemsRegions.put(parts[0], new ArrayList<>());

			noDeathItemsRegions.get(parts[0]).add(parts[1]);
		}
	}

	private final HashMap<String, List<String>> noDeathItemsRegions = new HashMap<>();
	private final List<String> noDeathItemsWorlds = new ArrayList<>();
	private final IRegionControl worldGuardInterface;

	private final SpawnerHandler spawnerHandler;
}
